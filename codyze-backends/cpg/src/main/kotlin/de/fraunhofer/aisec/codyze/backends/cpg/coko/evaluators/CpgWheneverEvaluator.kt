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
                    val (ensureNodes, problems) = evaluateConditionComponentToCpgNodes(ensure, premiseNode)
                    ensureNodes.filterNot { it is DummyNode } to problems
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
                    nodes.toList() to Problems()
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
        conditionToNodes: Map<ConditionComponent, Pair<List<Node>, Problems>>,
        passMessage: String,
        failMessage: String
    ): Collection<Finding> {
        val findings = mutableListOf<Finding>()

        val unfulfilledConditions = conditionToNodes.filterValues { (nodes, _) -> nodes.isEmpty() }

        if (unfulfilledConditions.isNotEmpty()) {
            for ((condition, nodesToProblems) in unfulfilledConditions) {
                val (_, problems) = nodesToProblems
                if (problems.isEmpty()) {
                    findings.add(
                        CpgFinding(
                            message = "No $condition found around $premise. $failMessage",
                            kind = Finding.Kind.Fail,
                            node = premiseNode
                        )
                    )
                    TODO("Better message")
                } else {
                    findings.add(
                        CpgFinding(
                            message = "There were problems in resolving $condition. $problems",
                            kind = Finding.Kind.Open,
                            node = premiseNode,
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
                    relatedNodes = conditionToNodes.values.flatMap { it.first }
                )
            )
        }

        return findings
    }

    private fun evaluateConditionNodeToCpgNodes(
        conditionNode: ConditionNode,
        premiseNode: Node? = null
    ): Pair<Nodes, Problems> {
        TODO("change to EvaluationResult")
        return when (conditionNode) {
            is ConditionComponent -> evaluateConditionComponentToCpgNodes(conditionNode, premiseNode)
            is ArgumentItem<*> -> evaluateArgumentItemToCpgNodes(conditionNode, premiseNode)
            is ReturnValueItem<*> -> evaluateReturnValueItemToCpgNodes(conditionNode, premiseNode)
            is Value<*> -> evaluateValueToCpgNodes(conditionNode, premiseNode)
        }
    }

    private fun evaluateValueToCpgNodes(value: Value<*>, premiseNode: Node? = null): Pair<Nodes, Problems> {
        return listOf(DummyNode()) to Problems()
    }

    private fun evaluateArgumentItemToCpgNodes(
        argumentItem: ArgumentItem<*>,
        premiseNode: Node? = null
    ): Pair<Nodes, Problems> {
        val nodes = argumentItem.cpgGetNodes().filterWithDistanceToPremise(premiseNode)
        return nodes to Problems()
    }

    private fun evaluateReturnValueItemToCpgNodes(
        returnValueItem: ReturnValueItem<*>,
        premiseNode: Node? = null
    ): Pair<Nodes, Problems> {
        val nodes = returnValueItem.cpgGetNodes().filterWithDistanceToPremise(premiseNode)
        return nodes to Problems()
    }

    private fun evaluateConditionComponentToCpgNodes(
        conditionComponent: ConditionComponent,
        premiseNode: Node? = null
    ): Pair<Nodes, Problems> {
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
    ): Pair<Nodes, Problems> {
        val (leftNodes, leftProblems) =
            evaluateConditionComponentToCpgNodes(binaryLogicalConditionComponent.left, premiseNode)
        val (rightNodes, rightProblems) =
            evaluateConditionComponentToCpgNodes(binaryLogicalConditionComponent.right, premiseNode)
        return when (binaryLogicalConditionComponent.operator) {
            BinaryLogicalOperatorName.AND -> (leftNodes intersect rightNodes) to (leftProblems + rightProblems)
            BinaryLogicalOperatorName.OR -> (leftNodes union rightNodes) to (leftProblems + rightProblems)
        }
    }

    private fun evaluateComparisonConditionComponent(
        comparisonConditionComponent: ComparisonConditionComponent,
        premiseNode: Node? = null
    ): Pair<Nodes, Problems> {
        val (leftNodes, leftProblems) =
            evaluateConditionNodeToCpgNodes(comparisonConditionComponent.left, premiseNode)
        val (rightNodes, rightProblems) =
            evaluateConditionNodeToCpgNodes(comparisonConditionComponent.right, premiseNode)

        val newProblems = Problems()
        val result = mutableSetOf<Node>()

        val leftNodesToValues =
            leftNodes.toNodesToValues(newProblems, comparisonConditionComponent.left.transformation)
        val rightNodesToValues =
            rightNodes.toNodesToValues(newProblems, comparisonConditionComponent.right.transformation)

        for ((leftNode, leftValue) in leftNodesToValues) {
            for ((rightNode, rightValue) in rightNodesToValues)
                when (comparisonConditionComponent.operator) {
                    ComparisonOperatorName.GEQ -> TODO()
                    ComparisonOperatorName.GT -> TODO()
                    ComparisonOperatorName.LEQ -> TODO()
                    ComparisonOperatorName.LT -> TODO()
                    ComparisonOperatorName.EQ ->
                        if (leftValue == rightValue) {
                            result.add(leftNode)
                            result.add(rightNode)
                        }
                    ComparisonOperatorName.NEQ -> TODO()
                }
        }

        return result to (leftProblems + rightProblems + newProblems)
    }

    private fun evaluateCallConditionComponent(
        callConditionComponent: CallConditionComponent,
        premiseNode: Node? = null
    ): Pair<Nodes, Problems> {
        val callNodes = callConditionComponent.op.cpgGetNodes().filterWithDistanceToPremise(premiseNode)
        return callNodes to Problems()
    }

    private fun evaluateContainsConditionComponent(
        containsConditionComponent: ContainsConditionComponent<*, *>,
        premiseNode: Node?
    ): Pair<Nodes, Problems> {
        val problems = Problems()
        val nodes = containsConditionComponent.item.cpgGetNodes().filterWithDistanceToPremise(premiseNode)
        val resultNodes = nodes.filter { node ->
            val backendDataItem = node.toBackendDataItem()
            val transformationResult = containsConditionComponent.item.transformation(backendDataItem)
            if (transformationResult.isFailure) {
                problems.add(
                    transformationResult.getFailureReasonOrNull() ?: "Transformation of $backendDataItem failed",
                    backendDataItem
                )
                false
            } else {
                val value = transformationResult.getValueOrNull()
                containsConditionComponent.collection.contains(value)
            }
        }

        return resultNodes to Problems()
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
                problems.add(transformationResult.getFailureReasonOrNull() ?: "Value was null", backendDataItem)
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
        private val map: MutableMap<BackendDataItem, String> = mutableMapOf()

        fun add(reason: String, value: BackendDataItem) {
            map[value] = reason
        }

        fun getProblems(): Map<BackendDataItem, String> = map

        fun isEmpty(): Boolean {
            return map.isEmpty()
        }

        operator fun plus(other: Problems): Problems {
            map.putAll(other.map)
            return this
        }

        override fun toString(): String {
            return map.toString()
        }
    }

    class EvaluationResult() {
        val fulfillingNodes: MutableList<Node> = mutableListOf()
        val unfulfillingNodes: MutableList<Node> = mutableListOf()
        val problems: Problems = Problems()

        constructor(fulfillingNodes: Nodes, unfulfillingNodes: Nodes, problems: Problems): this() {
            this.fulfillingNodes.addAll(fulfillingNodes)
            this.unfulfillingNodes.addAll(unfulfillingNodes)
            this.problems + problems
        }

    }

    class DummyNode : Node()
}
