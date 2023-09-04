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

import de.fraunhofer.aisec.codyze.backends.cpg.coko.Nodes
import de.fraunhofer.aisec.codyze.backends.cpg.coko.dsl.cpgGetNodes
import de.fraunhofer.aisec.codyze.backends.cpg.coko.modelling.toBackendDataItem
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.EvaluationContext
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.Finding
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.WheneverEvaluator
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.modelling.*
import de.fraunhofer.aisec.cpg.graph.Node

context(de.fraunhofer.aisec.codyze.backends.cpg.coko.CokoCpgBackend)
class CpgWheneverEvaluator(premise: ConditionComponent) : WheneverEvaluator(premise) {
    override fun evaluate(context: EvaluationContext): Collection<Finding> {
        val (premiseNodes, premiseProblems) = evaluateConditionNodeToCpgNodes(premise)




        TODO("Not yet implemented")
    }

    private fun evaluateConditionNodeToCpgNodes(conditionNode: ConditionNode): Pair<Nodes, Problems> {
       return  when (conditionNode) {
            is ConditionComponent -> evaluateConditionComponentToCpgNodes(conditionNode)
            is ArgumentItem<*> -> TODO()
            is ReturnValueItem<*> -> TODO()
            is Value<*> -> evaluateValue(conditionNode)
        }

    }

    private fun evaluateValue(value: Value<*>): Pair<Nodes, Problems> {
        return listOf(DummyNode()) to Problems()
    }

    private fun evaluateConditionComponentToCpgNodes(conditionComponent: ConditionComponent): Pair<Nodes, Problems> {
        return when (conditionComponent) {
            is BinaryLogicalConditionComponent -> evaluateBinaryLogicalConditionComponent(conditionComponent)
            is ComparisonConditionComponent -> evaluateComparisonConditionComponent(conditionComponent)
            is UnaryConditionComponent -> TODO()
            is CallConditionComponent -> evaluateCallConditionComponent(conditionComponent)
            is ContainsConditionComponent<*, *> -> TODO()
        }
    }

    private fun evaluateCallConditionComponent(callConditionComponent: CallConditionComponent): Pair<Nodes, Problems> {
        return callConditionComponent.op.cpgGetNodes() to Problems()
    }

    private fun evaluateBinaryLogicalConditionComponent(
        binaryLogicalConditionComponent: BinaryLogicalConditionComponent
    ): Pair<Nodes, Problems> {
        val (leftNodes, leftProblems) = evaluateConditionComponentToCpgNodes(binaryLogicalConditionComponent.left)
        val (rightNodes, rightProblems) = evaluateConditionComponentToCpgNodes(binaryLogicalConditionComponent.right)
        return when (binaryLogicalConditionComponent.operator) {
            BinaryLogicalOperatorName.AND -> (leftNodes intersect rightNodes) to (leftProblems + rightProblems)
            BinaryLogicalOperatorName.OR -> (leftNodes union rightNodes) to (leftProblems + rightProblems)
        }
    }

    private fun evaluateComparisonConditionComponent(
        comparisonConditionComponent: ComparisonConditionComponent
    ): Pair<Nodes, Problems> {
        val (leftNodes, leftProblems) = evaluateConditionNodeToCpgNodes(comparisonConditionComponent.left)
        val (rightNodes, rightProblems) = evaluateConditionNodeToCpgNodes(comparisonConditionComponent.right)

        val leftNodesToValues = leftNodes.associateWith {
            comparisonConditionComponent.left.transformation(
                it.toBackendDataItem()
            )
        }
        val rightNodesToValues = rightNodes.associateWith {
            comparisonConditionComponent.right.transformation(
                it.toBackendDataItem()
            )
        }

        return when (comparisonConditionComponent.operator) {
            ComparisonOperatorName.GEQ -> TODO()
            ComparisonOperatorName.GT -> TODO()
            ComparisonOperatorName.LEQ -> TODO()
            ComparisonOperatorName.LT -> TODO()
            ComparisonOperatorName.EQ ->
                (leftNodesToValues
                    .filter { (_, value) -> rightNodesToValues.containsValue(value) }
                    .keys) to (leftProblems + rightProblems)
            ComparisonOperatorName.NEQ ->
                (leftNodesToValues
                    .filterNot { (_, value) -> rightNodesToValues.containsValue(value) }
                    .keys) to (leftProblems + rightProblems)
        }
    }

    class Problems {
        val map: MutableMap<Any, String> = mutableMapOf()

        fun add(reason: String, value: Any) {
            map[value] = reason
        }

        operator fun plus(other: Problems): Problems {
            map.putAll(other.map)
            return this
        }
    }

    class DummyNode: Node()
}
