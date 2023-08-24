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
        ContainsConditionComponent(value(this), collection)

    infix fun DataItem.gt(other: DataItem) =
        BinaryConditionComponent(this, other, BinaryOperatorName.GT)

    infix fun Any.gt(other: Any) = value(this) gt value(other)

    infix fun DataItem.geq(other: DataItem) =
        BinaryConditionComponent(this, other, BinaryOperatorName.GEQ)

    infix fun Any.geq(other: Any) = value(this) geq value(other)

    infix fun DataItem.lt(other: DataItem) =
        BinaryConditionComponent(this, other, BinaryOperatorName.LT)

    infix fun Any.lt(other: Any) = value(this) lt value(other)

    infix fun DataItem.leq(other: DataItem) =
        BinaryConditionComponent(this, other, BinaryOperatorName.LEQ)

    infix fun Any.leq(other: Any) = value(this) leq value(other)

    infix fun DataItem.eq(other: DataItem) =
        BinaryConditionComponent(this, other, BinaryOperatorName.EQ)

    infix fun Any.eq(other: Any) = value(this) eq value(other)

    infix fun DataItem.neq(other: DataItem) =
        BinaryConditionComponent(this, other, BinaryOperatorName.NEQ)

    infix fun Any.neq(other: Any) = value(this) neq value(other)

    fun call(op: Op) = CallConditionComponent(op)

    infix fun ConditionComponent.and(other: ConditionComponent) =
        BinaryConditionComponent(this, other, BinaryOperatorName.AND)

    infix fun ConditionComponent.or(other: ConditionComponent) =
        BinaryConditionComponent(this, other, BinaryOperatorName.OR)

    fun not(conditionComponent: ConditionComponent) = UnaryConditionComponent(conditionComponent, UnaryOperatorName.NOT)
}
