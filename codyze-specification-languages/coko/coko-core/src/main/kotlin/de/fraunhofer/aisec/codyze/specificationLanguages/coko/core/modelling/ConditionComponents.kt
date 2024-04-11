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
package de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.modelling

import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.Op

sealed interface ConditionNode {
    override fun toString(): String
}

/** [ConditionNode] that can have children */
sealed interface ConditionComponent : ConditionNode

/** [ConditionNode] that does not have children */
sealed interface ConditionLeaf : ConditionNode

/** A [ConditionComponent] that has two children and a [operator] */
sealed interface BinaryConditionComponent : ConditionComponent {
    val left: ConditionNode
    val right: ConditionNode
    val operator: BinaryOperatorName
}

/** A [BinaryConditionComponent] that compares [left] and [right] based on [operator] (e.g. equality or less than) */
class ComparisonConditionComponent(
    override val left: DataItem<*>,
    override val right: DataItem<*>,
    override val operator: ComparisonOperatorName
) : BinaryConditionComponent {
    override fun toString(): String = "$left ${operator.name} $right"
}

/** A [BinaryConditionComponent] that logically combines [left] and [right] based on [operator] (e.g. logical and) */
class BinaryLogicalConditionComponent(
    override val left: ConditionComponent,
    override val right: ConditionComponent,
    override val operator: BinaryLogicalOperatorName
) : BinaryConditionComponent {
    override fun toString(): String = "$left ${operator.name} $right"
}

/** A [ConditionComponent] that has one child, [conditionNode], and a [operator] (e.g. negation) */
class UnaryConditionComponent(
    val conditionNode: ConditionNode,
    val operator: UnaryOperatorName
) : ConditionComponent {
    override fun toString(): String = "${operator.name} $conditionNode"
}

/** A [ConditionComponent] that represents a function call */
// TODO: Is this necessary? And should it be a ConditionLeaf?
open class CallConditionComponent(val op: Op) : ConditionComponent {
    override fun toString(): String = op.toString()
}

/** A [ConditionComponent] that represents the condition that the value of [item] should be in [collection] */
class ContainsConditionComponent<E, T>(val item: DataItem<E>, val collection: Collection<T>) : ConditionComponent {
    override fun toString(): String = "$item within $collection"
}

sealed interface BinaryOperatorName {
    val operatorCodes: List<String>
}

enum class BinaryLogicalOperatorName(override val operatorCodes: List<String>) : BinaryOperatorName {
    AND(listOf("&&", "and")),
    OR(listOf("||", "or"))
}
enum class ComparisonOperatorName(override val operatorCodes: List<String>) : BinaryOperatorName {
    GEQ(listOf(">=")),
    GT(listOf(">")),
    LEQ(listOf("<=")),
    LT(listOf("<")),
    EQ(listOf("==")), // TODO: python `==` vs `is`
    NEQ(listOf("!=")),
}

enum class UnaryOperatorName(val operatorCodes: List<String>) {
    NOT(listOf("!", "not"))
}
