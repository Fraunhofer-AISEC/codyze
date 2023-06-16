/*
 * Copyright (c) 2022, Fraunhofer AISEC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fraunhofer.aisec.codyze.backends.cpg.coko.evaluators

import de.fraunhofer.aisec.codyze.backends.cpg.coko.CokoCpgBackend
import de.fraunhofer.aisec.codyze.backends.cpg.coko.CpgFinding
import de.fraunhofer.aisec.codyze.backends.cpg.coko.Nodes
import de.fraunhofer.aisec.codyze.backends.cpg.coko.dsl.cpgGetAllNodes
import de.fraunhofer.aisec.codyze.backends.cpg.coko.dsl.cpgGetNodes
import de.fraunhofer.aisec.codyze.backends.cpg.coko.ordering.CodyzeDfaOrderEvaluator
import de.fraunhofer.aisec.codyze.backends.cpg.coko.ordering.toNfa
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.*
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.ConstructorOp
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.FunctionOp
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.Op
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.Order
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.ordering.*
import de.fraunhofer.aisec.cpg.graph.*
import de.fraunhofer.aisec.cpg.graph.declarations.VariableDeclaration
import de.fraunhofer.aisec.cpg.graph.statements.expressions.DeclaredReferenceExpression
import mu.KotlinLogging
import kotlin.reflect.full.createType
import kotlin.reflect.full.declaredMemberFunctions

private val logger = KotlinLogging.logger {}

context(CokoCpgBackend)
/**
 * CPG [Evaluator] to evaluate Coko order expressions.
 */
class OrderEvaluator(val baseNodes: Collection<Node>, val order: Order) : Evaluator {
    /**
     * Filter the [OrderNode] by the given type. This also works for nested [OrderNode]s using
     * depth first search (DFS)
     */
    private inline fun <reified T> OrderNode.filterIsInstanceToList(): List<T> {
        val result = mutableListOf<T>()
        applyToAll {
            when (this) {
                is T -> result.add(this)
                else -> {}
            }
        }
        return result
    }

    /**
     * Perform DFS through all previous EOG edges until we find a root node with no previous EOG edges.
     * This node is then a MethodDeclaration/FunctionDeclaration or similar.
     * DFS is necessary to not get stuck in loops or similar.
     */
    private fun Node.followPrevEOGUntilEnd(): Node? {
        val stack = ArrayDeque<Node>()
        stack.addLast(this)
        while (stack.isNotEmpty()) {
            val node = stack.removeLast()
            if (node.prevEOGEdges.isEmpty()) {
                return node
            } else {
                for (prevNode in node.prevEOGEdges.map { it.start }) {
                    stack.addLast(prevNode)
                }
            }
        }
        return null
    }

    /**
     * Registers [op] and its [nodes] in the two maps.
     * [nodesToOp] maps each Node in [nodes] to the `hashCode` of [op] which is used for the order evaluation.
     * [hashToMethod] maps the `hashCode` of [op] to the String of [op] which is used for the findings.
     */
    private fun registerOpAndNodes(
        op: Op,
        nodes: Nodes,
        hashToMethod: MutableMap<String, String>,
        nodesToOp: MutableMap<Node, MutableSet<String>>
    ) {
        val hash = op.hashCode().toString()
        hashToMethod.putIfAbsent(hash, op.toString())
        for (node in nodes) {
            val setOfNames = nodesToOp.getOrPut(node) { mutableSetOf() }
            setOfNames.add(hash)
        }
    }

    /**
     * Filter positive findings that also appear as negative findings.
     * This happens e.g., if there are multiple possible execution order flows (if-statement etc.) and
     * the order is correct in one branch, but incorrect in another
     */
    private fun filteredFindings(findings: Set<CpgFinding>): Set<CpgFinding> {
        val passFindings = findings.filter { it.kind == Finding.Kind.Pass }.map { it.node to it }.toSet()
        val failFindings = findings.filter { it.kind == Finding.Kind.Fail }.map { it.node }.toSet()
        val positiveFindingsToRemove = passFindings.filter { passPair -> passPair.first in failFindings }
        return findings.minus(positiveFindingsToRemove.map { it.second }.toSet())
    }

    @Suppress("UnsafeCallOnNullableType", "ReturnCount")
    override fun evaluate(context: EvaluationContext): Set<CpgFinding> {
        // first check whether it is an order rule that we can evaluate
        val syntaxTree = order.toNode()
        val usedBases =
            syntaxTree.filterIsInstanceToList<TerminalOrderNode>().map { it.baseName }.toSet()
        if (usedBases.size > 1) {
            logger.warn("Order statement contains more than one base. Not supported.")
            return emptySet()
        }

        // get the [DFA] from the given order statement
        val dfa = syntaxTree.toNfa().toDfa()

        // in order to get the nodes corresponding to the OPs defined in the order statement
        // we need the object they are contained in
        val implementation = context.parameterMap.values.single()

        // use reflection to get all functions returning OPs in the implementation
        val opsInConcept =
            implementation::class.declaredMemberFunctions.filter {
                it.returnType in
                    listOf(FunctionOp::class.createType(), ConstructorOp::class.createType())
            }

        // TODO: add nodesToOp to context (the cache) for other evaluations on the same implementation?
        val nodesToOp = mutableMapOf<Node, MutableSet<String>>()
        val hashToMethod = mutableMapOf<String, String>()

        // now call each of those functions to get the nodes corresponding to the OP
        // this will detect ALL calls to the specified methods/functions regardless of the defined signatures
        // this is the set of all nodes the [CodyzeDfaOrderEvaluator] considers during evaluation
        for (orderToken in opsInConcept) {
            val op = (
                orderToken.call(implementation, *(List(orderToken.parameters.size - 1) { null }.toTypedArray()))
                ) as Op
            val nodes = op.cpgGetAllNodes()
            registerOpAndNodes(op, nodes, hashToMethod, nodesToOp)
        }

        // the more specific nodes respecting the signature here
        // the nodes from +testObj.start(123) <- userDefined Ops are used
        // only allow the start nodes that take '123' as argument
        for ((_, op) in order.userDefinedOps.entries) {
            val nodes = op.cpgGetNodes()
            registerOpAndNodes(op, nodes, hashToMethod, nodesToOp)
        }

        // create the order evaluator for this order rule
        val dfaEvaluator = CodyzeDfaOrderEvaluator(
            dfa = dfa,
            hashToMethod = hashToMethod,
            nodeToRelevantMethod = nodesToOp,
            consideredBases = baseNodes.flatMap { node ->
                node.followNextDFGEdgesUntilHit { next ->
                    next is VariableDeclaration || next is DeclaredReferenceExpression
                }.fulfilled.mapNotNull { path ->
                    path.lastOrNull()
                }
            }.toSet(),
            consideredResetNodes = baseNodes.toSet(),
            context = context,
        )

        // this should be a set of MethodDeclarations or similar top level statements
        val topLevelCompoundStatement = baseNodes.mapNotNull { node ->
            node.followPrevEOGUntilEnd()
        }.toSet()

        var isOrderValid = true
        // evaluate the order for every MethodDeclaration/FunctionDeclaration/init block etc. which contains
        // a node of the [baseNodes]
        for (node in topLevelCompoundStatement) {
            // evaluates the order for all bases in this one method
            // this also creates the findings and add them to the [CodyzeDfaOrderEvaluator.findings] collection
            isOrderValid = dfaEvaluator.evaluateOrder(
                node
            ) && isOrderValid
        }

        // filter positive findings that also appear as negative findings
        return filteredFindings(dfaEvaluator.findings)
    }
}
