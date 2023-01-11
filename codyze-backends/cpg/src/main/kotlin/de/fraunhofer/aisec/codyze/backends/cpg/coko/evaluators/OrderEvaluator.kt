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
import de.fraunhofer.aisec.codyze.backends.cpg.coko.CpgEvaluationResult
import de.fraunhofer.aisec.codyze.backends.cpg.coko.CpgFinding
import de.fraunhofer.aisec.codyze.backends.cpg.coko.dsl.getAllNodes
import de.fraunhofer.aisec.codyze.backends.cpg.coko.dsl.getNodes
import de.fraunhofer.aisec.codyze.backends.cpg.coko.ordering.CodyzeDfaOrderEvaluator
import de.fraunhofer.aisec.codyze.backends.cpg.coko.ordering.toNfa
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.*
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.ConstructorOp
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.FunctionOp
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.Op
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.Order
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.ordering.*
import de.fraunhofer.aisec.cpg.graph.AssignmentTarget
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.declarations.Declaration
import de.fraunhofer.aisec.cpg.graph.followPrevEOG
import de.fraunhofer.aisec.cpg.graph.followNextEOG
import mu.KotlinLogging
import kotlin.reflect.full.createType
import kotlin.reflect.full.declaredMemberFunctions

private val logger = KotlinLogging.logger {}

context(CokoCpgBackend)
class OrderEvaluator(val baseNodes: Collection<Node>?, val order: Order) : Evaluator {
    private fun findInstancesForEntities(rule: CokoRule): Collection<Node> =
        TODO("Use Codyze-v2 algorithm to find VariableDeclarations for Entities automatically ($rule)")

    /**
     * Filter the [OrderNode] by the given type. This also works for nested [OrderNodes]s using
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

    @Suppress("UnsafeCallOnNullableType")
    override fun evaluate(context: EvaluationContext): EvaluationResult<CpgFinding> {
        // first check whether it is an order rule that we can evaluate
        val syntaxTree = order.toNode()
        val usedBases =
            syntaxTree.filterIsInstanceToList<TerminalOrderNode>().map { it.baseName }.toSet()
        if (usedBases.size > 1) {
            logger.warn("Order statement contains more than one base. Not supported.")
        }
        if (usedBases.isEmpty()) {
            logger.warn("Order statement does not contain any OPs. Invalid order.")
        }
        // get the [DFA] from the given order statement
        val dfa = syntaxTree.toNfa().toDfa()

        // the places to start the order evaluation
        // preferably use the [baseNodes] specified by the user
        // alternatively fallback to searching possible start nodes codyze v2 style
        val orderStartNodes = baseNodes ?: findInstancesForEntities(context.rule)

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

        // now call each of those functions to get the nodes corresponding to the OP
        // this will detect ALL calls to the specified methods/functions regardless of the defined signatures
        // this is the set of all nodes the [CodyzeDfaOrderEvaluator] considers during evaluation
        for (op in opsInConcept){
            val nodes = ((op.call(implementation, *(List(op.parameters.size - 1) {null}.toTypedArray()))) as Op)
                .getAllNodes().filter { it !in orderStartNodes }
            for (node in nodes) {
                val setOfNames = nodesToOp.getOrPut(node) { mutableSetOf() }
                setOfNames.add(op.name)
            }
        }

        // the more specific nodes respecting the signature here
        //
        // the nodes from +testObj::start.use { } <- use makes them userDefined
        for ((opName, op) in order.userDefinedOps.entries){
            val nodes = op.getNodes()
            for (node in nodes) {
                val setOfNames = nodesToOp.getOrDefault(node, mutableSetOf())
                setOfNames.add(opName)
            }
        }

        // this list will be filled by [CodyzeDfaOrderEvaluator] using the lambda given as the 'createFinding' argument
        val findings = mutableListOf<CpgFinding>()

        var isOrderValid = true
        for (node in orderStartNodes) {
            val dfaEvaluator = CodyzeDfaOrderEvaluator(
                createFinding = { cpgNode, message -> findings.add(CpgFinding(node = cpgNode, message = message)) },
                consideredBases = setOf(node.followNextEOG { it.end is AssignmentTarget }!!.last().end),
                nodeToRelevantMethod = nodesToOp,
                // consideredResetNodes = orderStartNodes, // TODO: implement consideredResetNodes
            )
            isOrderValid = dfaEvaluator.evaluateOrder(
                dfa,
                node.followPrevEOG { it.start is Declaration }!!.last().start
            ) && isOrderValid
        }

        // TODO: add related nodes
        return CpgEvaluationResult(findings)
    }
}
