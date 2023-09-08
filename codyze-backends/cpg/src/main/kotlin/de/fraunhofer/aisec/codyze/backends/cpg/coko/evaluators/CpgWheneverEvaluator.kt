/*
 * Copyright (c) 2023, Fraunhofer AISEC. All rights reserved.
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

import de.fraunhofer.aisec.codyze.backends.cpg.coko.CpgFinding
import de.fraunhofer.aisec.codyze.backends.cpg.coko.Nodes
import de.fraunhofer.aisec.codyze.backends.cpg.coko.dsl.cpgGetNodes
import de.fraunhofer.aisec.codyze.backends.cpg.coko.modelling.toBackendDataItem
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.*
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.Rule
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.TransformationResult
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.modelling.*
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.followNextEOG
import de.fraunhofer.aisec.cpg.graph.followPrevEOG
import de.fraunhofer.aisec.cpg.query.executionPath
import kotlin.reflect.full.findAnnotation

context(de.fraunhofer.aisec.codyze.backends.cpg.coko.CokoCpgBackend)
class CpgWheneverEvaluator(premise: ConditionComponent) : WheneverEvaluator(premise) {

    private val defaultFailMessage: String = ""

    private val defaultPassMessage: String = ""
    override fun evaluate(context: EvaluationContext): Collection<Finding> {
        val ruleAnnotation = context.rule.findAnnotation<Rule>()
        val failMessage = ruleAnnotation?.failMessage?.takeIf { it.isNotEmpty() } ?: defaultFailMessage
        val passMessage = ruleAnnotation?.passMessage?.takeIf { it.isNotEmpty() } ?: defaultPassMessage

        val findings = mutableListOf<Finding>()

        val (premiseNodes, premiseProblems) = evaluateConditionNodeToCpgNodes(premise)

        if (premiseNodes.isEmpty()) {
            if (premiseProblems.isEmpty()) {
                findings.add(
                    CpgFinding(
                        message = "$premise was not found in code.",
                        kind = Finding.Kind.NotApplicable,
                    )
                )
            } else {
                findings.add(
                    CpgFinding(
                        message = "$premise could not be resolved correctly. $premiseProblems",
                        kind = Finding.Kind.Open,

                    )
                )
            }
        } else {
            for (premiseNode in premiseNodes) {
                val ensuresToNodes = ensures.associateWith { ensure ->
                    evaluateConditionComponentToCpgNodes(
                        ensure,
                        premiseNode
                    ).removeDummyNodes()
                }
                val callAssertionsToNodes = callAssertions.associateWith { callAssertion ->
                    val nodes = callAssertion.op.cpgGetNodes()
                    val location = callAssertion.location
                    if (location != null) {
                        when (location.scope) {
                            Scope.Function -> {
                                when (location.direction) {
                                    Direction.afterwards -> TODO()
                                    Direction.before -> TODO()
                                    Direction.somewhere -> TODO()
                                }
                            }
                            Scope.Block -> TODO()
                        }
                    }
                    TODO()
                    EvaluationResult(nodes, emptyList(), Problems())
                }
                findings.addAll(
                    generateFindings(premiseNode, ensuresToNodes + callAssertionsToNodes, passMessage, failMessage)
                )
            }
        }

        return findings
    }

    private fun generateFindings(
        premiseNode: Node,
        conditionToNodes: Map<ConditionComponent, EvaluationResult>,
        passMessage: String,
        failMessage: String
    ): Collection<Finding> {
        val findings = mutableListOf<Finding>()

        val unfulfilledConditions = conditionToNodes.filterValues { (fulfilled, _, _) -> fulfilled.isEmpty() }

        if (unfulfilledConditions.isNotEmpty()) {
            for ((condition, nodesToProblems) in unfulfilledConditions) {
                val (_, unfulfilled, problems) = nodesToProblems
                if (problems.isNotEmpty()) {
                    findings.add(
                        CpgFinding(
                            message = "There were problems in resolving $condition. $problems",
                            kind = Finding.Kind.Open,
                            node = premiseNode,
                            relatedNodes = problems.getNodes()
                        )
                    )
                } else {
                    // TODO: Better message
                    findings.add(
                        CpgFinding(
                            message = "No $condition found around $premise. $failMessage",
                            kind = Finding.Kind.Fail,
                            node = premiseNode,
                            relatedNodes = unfulfilled
                        )
                    )
                }
            }
        } else {
            findings.add(
                CpgFinding(
                    message = passMessage,
                    kind = Finding.Kind.Pass,
                    node = premiseNode,
                    relatedNodes = conditionToNodes.values.flatMap { it.fulfillingNodes }
                )
            )

            val conditionToProblems = conditionToNodes.mapValues { (_, result) -> result.problems }
            for ((condition, problems) in conditionToProblems) {
                if (problems.isNotEmpty()) {
                    CpgFinding(
                        message = "There were problems in resolving $condition. $problems",
                        kind = Finding.Kind.Informational,
                        node = premiseNode,
                        relatedNodes = problems.getNodes()
                    )
                }
            }
        }

        return findings
    }

    private fun evaluateConditionNodeToCpgNodes(
        conditionNode: ConditionNode,
        premiseNode: Node? = null
    ): EvaluationResult {
        return when (conditionNode) {
            is ConditionComponent -> evaluateConditionComponentToCpgNodes(conditionNode, premiseNode)
            is ArgumentItem<*> -> evaluateArgumentItemToCpgNodes(conditionNode, premiseNode)
            is ReturnValueItem<*> -> evaluateReturnValueItemToCpgNodes(conditionNode, premiseNode)
            is Value<*> -> evaluateValueToCpgNodes(conditionNode, premiseNode)
        }
    }

    private fun evaluateValueToCpgNodes(value: Value<*>, premiseNode: Node? = null): EvaluationResult {
        return EvaluationResult(listOf(DummyNode()), emptyList(), Problems())
    }

    private fun evaluateArgumentItemToCpgNodes(
        argumentItem: ArgumentItem<*>,
        premiseNode: Node? = null
    ): EvaluationResult {
        val nodes = argumentItem.cpgGetNodes().filterWithDistanceToPremise(premiseNode)
        return EvaluationResult(nodes, emptyList(), Problems())
    }

    private fun evaluateReturnValueItemToCpgNodes(
        returnValueItem: ReturnValueItem<*>,
        premiseNode: Node? = null
    ): EvaluationResult {
        val nodes = returnValueItem.cpgGetNodes().filterWithDistanceToPremise(premiseNode)
        return EvaluationResult(nodes, emptyList(), Problems())
    }

    private fun evaluateConditionComponentToCpgNodes(
        conditionComponent: ConditionComponent,
        premiseNode: Node? = null
    ): EvaluationResult {
        return when (conditionComponent) {
            is BinaryLogicalConditionComponent ->
                evaluateBinaryLogicalConditionComponent(conditionComponent, premiseNode)
            is ComparisonConditionComponent -> evaluateComparisonConditionComponent(conditionComponent, premiseNode)
            is UnaryConditionComponent -> TODO()
            is CallConditionComponent -> evaluateCallConditionComponent(conditionComponent, premiseNode)
            is ContainsConditionComponent<*, *> -> evaluateContainsConditionComponent(conditionComponent, premiseNode)
        }
    }

    private fun evaluateBinaryLogicalConditionComponent(
        binaryLogicalConditionComponent: BinaryLogicalConditionComponent,
        premiseNode: Node? = null
    ): EvaluationResult {
        val (leftFulfillingNodes, leftUnfulfillingNodes, leftProblems) =
            evaluateConditionComponentToCpgNodes(binaryLogicalConditionComponent.left, premiseNode)
        val (rightFulfillingNodes, rightUnfulfillingNodes, rightProblems) =
            evaluateConditionComponentToCpgNodes(binaryLogicalConditionComponent.right, premiseNode)
        return when (binaryLogicalConditionComponent.operator) {
            BinaryLogicalOperatorName.AND ->
                EvaluationResult(
                    leftFulfillingNodes intersect rightFulfillingNodes.toSet(),
                    leftUnfulfillingNodes union rightUnfulfillingNodes,
                    leftProblems + rightProblems
                )
            BinaryLogicalOperatorName.OR ->
                EvaluationResult(
                    leftFulfillingNodes union rightFulfillingNodes,
                    leftUnfulfillingNodes union rightUnfulfillingNodes,
                    leftProblems + rightProblems
                )
        }
    }

    private fun evaluateComparisonConditionComponent(
        comparisonConditionComponent: ComparisonConditionComponent,
        premiseNode: Node? = null
    ): EvaluationResult {
        val (leftFulfillingNodes, leftUnfulfillingNodes, leftProblems) =
            evaluateConditionNodeToCpgNodes(comparisonConditionComponent.left, premiseNode)
        val (rightFulfillingNodes, rightUnfulfillingNodes, rightProblems) =
            evaluateConditionNodeToCpgNodes(comparisonConditionComponent.right, premiseNode)

        val newProblems = Problems()
        val newFulfillingNodes = mutableSetOf<Node>()
        val newUnfulfillingNodes = mutableSetOf<Node>()

        val leftNodesToValues =
            leftFulfillingNodes.toNodesToValues(newProblems, comparisonConditionComponent.left.transformation)
        val rightNodesToValues =
            rightFulfillingNodes.toNodesToValues(newProblems, comparisonConditionComponent.right.transformation)

        for ((leftNode, leftValue) in leftNodesToValues) {
//            for ((rightNode, rightValue) in rightNodesToValues)
            when (comparisonConditionComponent.operator) {
                ComparisonOperatorName.GEQ -> TODO()
                ComparisonOperatorName.GT -> TODO()
                ComparisonOperatorName.LEQ -> TODO()
                ComparisonOperatorName.LT -> TODO()
                ComparisonOperatorName.EQ -> {
                    val (fulfilling, unfulfilling) =
                        rightNodesToValues.toList().partition { (_, rightValue) -> leftValue == rightValue }
                    newFulfillingNodes.addAll(fulfilling.map { it.first })
                    newUnfulfillingNodes.addAll(unfulfilling.map { it.first })
                    if (fulfilling.isEmpty()) {
                        newUnfulfillingNodes.add(
                            leftNode
                        )
                    } else {
                        newFulfillingNodes.add(leftNode)
                    }
                }
                ComparisonOperatorName.NEQ -> TODO()
            }
        }

        return EvaluationResult(
            newFulfillingNodes,
            leftUnfulfillingNodes + rightUnfulfillingNodes + newUnfulfillingNodes,
            leftProblems + rightProblems + newProblems
        )
    }

    private fun evaluateCallConditionComponent(
        callConditionComponent: CallConditionComponent,
        premiseNode: Node? = null
    ): EvaluationResult {
        val callNodes = callConditionComponent.op.cpgGetNodes().filterWithDistanceToPremise(premiseNode)
        return EvaluationResult(callNodes, emptyList(), Problems())
    }

    private fun evaluateContainsConditionComponent(
        containsConditionComponent: ContainsConditionComponent<*, *>,
        premiseNode: Node?
    ): EvaluationResult {
        val problems = Problems()
        val nodes = containsConditionComponent.item.cpgGetNodes().filterWithDistanceToPremise(premiseNode)
        val (fulfilling, unfulfilling) = nodes.partition { node ->
            val backendDataItem = node.toBackendDataItem()
            val transformationResult = containsConditionComponent.item.transformation(backendDataItem)
            if (transformationResult.isFailure) {
                problems.add(
                    transformationResult.getFailureReasonOrNull() ?: "Transformation of $backendDataItem failed",
                    node
                )
                false
            } else {
                val value = transformationResult.getValueOrNull()
                containsConditionComponent.collection.contains(value)
            }
        }

        return EvaluationResult(fulfilling, unfulfilling, problems)
    }

    private fun Nodes.toNodesToValues(
        problems: Problems,
        transformation: (BackendDataItem) -> TransformationResult<out Any?, String>
    ): Map<Node, Any> {
        val result = mutableMapOf<Node, Any>()

        for (node in this) {
            val backendDataItem = node.toBackendDataItem()
            val transformationResult = transformation(backendDataItem)
            val value = transformationResult.getValueOrNull()
            if (value == null) {
                problems.add(transformationResult.getFailureReasonOrNull() ?: "Value was null", node)
            } else {
                result[node] = value
            }
        }

        return result
    }

    private fun Nodes.filterWithDistanceToPremise(premiseNode: Node?): Nodes {
        return if (premiseNode != null) {
            this.filter { executionPath(it, premiseNode).value }
        } else {
            this
        }
    }

    private fun Node.eogDistance(other: Node): Int {
        var to = 0
        this.followNextEOG {
            to++
            it.end == other
        }

        var from = 0
        this.followPrevEOG {
            from++
            it.end == other
        }

        return if (to < from) to else from
    }

    class Problems {
        private val map: MutableMap<Node, String> = mutableMapOf()

        fun add(reason: String, value: Node) {
            map[value] = reason
        }

        fun getNodes(): Nodes = map.keys

        fun isNotEmpty(): Boolean {
            return map.isNotEmpty()
        }

        operator fun plus(other: Problems): Problems {
            map.putAll(other.map)
            return this
        }

        override fun toString(): String {
            return map.mapKeys { (node, _) -> node.toBackendDataItem() }.toString()
        }
    }

    data class EvaluationResult(
        val fulfillingNodes: MutableList<Node> = mutableListOf(),
        val unfulfillingNodes: MutableList<Node> = mutableListOf(),
        val problems: Problems = Problems()
    ) {
        constructor(
            fulfillingNodes: Nodes,
            unfulfillingNodes: Nodes,
            problems: Problems
        ) : this(fulfillingNodes.toMutableList(), unfulfillingNodes.toMutableList(), problems)

        operator fun plus(other: EvaluationResult): EvaluationResult {
            fulfillingNodes.addAll(other.fulfillingNodes)
            unfulfillingNodes.addAll(other.unfulfillingNodes)
            problems + other.problems
            return this
        }

        fun removeDummyNodes(): EvaluationResult {
            fulfillingNodes.removeIf { it is DummyNode }
            unfulfillingNodes.removeIf { it is DummyNode }
            return this
        }
    }

    class DummyNode : Node()
}
