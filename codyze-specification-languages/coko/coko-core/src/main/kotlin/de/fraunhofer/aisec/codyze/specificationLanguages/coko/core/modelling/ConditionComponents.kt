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

sealed interface ConditionNode
sealed interface ConditionComponent : ConditionNode
sealed interface ConditionLeaf : ConditionNode

sealed interface BinaryConditionComponent : ConditionComponent {
    val left: ConditionNode
    val right: ConditionNode
    val operator: BinaryOperatorName
}

class ComparisonConditionComponent(
    override val left: DataItem<*>,
    override val right: DataItem<*>,
    override val operator: ComparisonOperatorName
) : BinaryConditionComponent

class BinaryLogicalConditionComponent(
    override val left: ConditionComponent,
    override val right: ConditionComponent,
    override val operator: BinaryLogicalOperatorName
) : BinaryConditionComponent

class UnaryConditionComponent(
    val conditionNode: ConditionNode,
    val operator: UnaryOperatorName
) : ConditionComponent

class CallConditionComponent(val op: Op) : ConditionComponent

class ContainsConditionComponent<E, T>(val item: DataItem<E>, val collection: Collection<T>) : ConditionComponent

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
