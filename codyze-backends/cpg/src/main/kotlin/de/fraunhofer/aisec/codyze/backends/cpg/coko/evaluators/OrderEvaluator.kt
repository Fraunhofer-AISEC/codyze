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
import de.fraunhofer.aisec.codyze.backends.cpg.coko.Finding
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
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.declarations.Declaration
import de.fraunhofer.aisec.cpg.graph.followPrevEOG
import de.fraunhofer.aisec.cpg.passes.followNextEOG
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
    override fun evaluate(context: EvaluationContext): EvaluationResult<Finding> {
        // first check whether it is an order rule that we can evaluate
        val syntaxTree = order.toNode()
        val usedBases =
            syntaxTree.filterIsInstanceToList<TerminalOrderNode>().map { it.baseName }.toSet()
        if (usedBases.size > 1) {
            logger.warn("Order statement contains more than one base. Not supported.")
        }
        if (usedBases.isEmpty()) {
            logger.warn("Order statement does not contain any ops. Invalid order.")
        }

        val dfa = syntaxTree.toNfa().toDfa()
        val orderStartNodes = baseNodes ?: findInstancesForEntities(context.rule)

        val implementation = context.parameterMap.values.single()
        val opsInConcept =
            implementation::class.declaredMemberFunctions.filter {
                it.returnType in
                    listOf(FunctionOp::class.createType(), ConstructorOp::class.createType())
            }

        val nodesToOp =
            opsInConcept
                .flatMap {
                    ((it.call(implementation)) as Op).getAllNodes().map {
                            node ->
                        node to it.name
                    }
                }
                .toMap()
                .toMutableMap()

        nodesToOp.putAll(
            order.userDefinedOps.entries
                .flatMap { (opName, op) ->
                    op.invoke().getNodes().map { it to opName }
                }
                .toMap()
        )

        // this list will be filled by [CodyzeDfaOrderEvaluator] using the lambda given as the 'taskOnAction' argument
        val findings = mutableListOf<Finding>()

        // TODO: for a node to know its method execute following pass: EdgeCachePass
        // TODO: activate [de.fraunhofer.aisec.cpg.passes.UnreachableEOGPass]
        var isOrderValid = true
        for (node in orderStartNodes) { // TODO: orderStartNodes should be variable declarations!
            val dfaEvaluator = CodyzeDfaOrderEvaluator(
                createFinding = { cpgNode, message -> findings.add(Finding(node = cpgNode, message = message)) },
                consideredBases = setOf(node.followNextEOG { it.end is Declaration }!!.last().end),
                nodeToRelevantMethod = nodesToOp
            )
            isOrderValid = dfaEvaluator.evaluateOrder(
                dfa,
                node.followPrevEOG { it.start is Declaration }!!.last().start
            ) && isOrderValid
        }
        // TODO: implement fine-grained results e.g., for each orderStartNode
        return CpgEvaluationResult(findings)
    }
}
