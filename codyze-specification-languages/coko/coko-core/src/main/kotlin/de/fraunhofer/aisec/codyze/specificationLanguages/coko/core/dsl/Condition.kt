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

package de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl

import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.ListBuilder
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.modelling.*

fun condition(block: Condition.() -> ConditionComponent) = Condition().run(block)

class Condition {
    val list by lazy { ListBuilder() }

    infix fun Any.within(collection: Collection<*>) =
        ContainsConditionComponent(toValue(this), collection)

    infix fun DataItem<*>.gt(other: DataItem<*>) =
        ComparisonConditionComponent(this, other, ComparisonOperatorName.GT)

    infix fun Any.gt(other: Any) = toValue(this) gt toValue(other)

    infix fun DataItem<*>.geq(other: DataItem<*>) =
        ComparisonConditionComponent(this, other, ComparisonOperatorName.GEQ)

    infix fun Any.geq(other: Any) = toValue(this) geq toValue(other)

    infix fun DataItem<*>.lt(other: DataItem<*>) =
        ComparisonConditionComponent(this, other, ComparisonOperatorName.LT)

    infix fun Any.lt(other: Any) = toValue(this) lt toValue(other)

    infix fun DataItem<*>.leq(other: DataItem<*>) =
        ComparisonConditionComponent(this, other, ComparisonOperatorName.LEQ)

    infix fun Any.leq(other: Any) = toValue(this) leq toValue(other)

    infix fun DataItem<*>.eq(other: DataItem<*>) =
        ComparisonConditionComponent(this, other, ComparisonOperatorName.EQ)

    infix fun Any.eq(other: Any) = toValue(this) eq toValue(other)

    infix fun DataItem<*>.neq(other: DataItem<*>) =
        ComparisonConditionComponent(this, other, ComparisonOperatorName.NEQ)

    infix fun Any.neq(other: Any) = toValue(this) neq toValue(other)

    fun call(op: Op) = CallConditionComponent(op)

    infix fun ConditionComponent.and(other: ConditionComponent) =
        BinaryLogicalConditionComponent(this, other, BinaryLogicalOperatorName.AND)

    infix fun ConditionComponent.or(other: ConditionComponent) =
        BinaryLogicalConditionComponent(this, other, BinaryLogicalOperatorName.OR)

    fun not(conditionComponent: ConditionComponent) = UnaryConditionComponent(conditionComponent, UnaryOperatorName.NOT)

    private fun toValue(value: Any): DataItem<*> =
        when(value) {
            is DataItem<*> -> value
            else -> Value(value)
        }
}
