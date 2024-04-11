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

/** This class exists to restrict where the functions can be called */
@Suppress("complexity.TooManyFunctions")
class Condition {
    /** Can be used to build a list like `list[x,y,z]` */
    val list by lazy { ListBuilder() }

    /**
     * Builds a [ContainsConditionComponent] that specifies that the value of this [DataItem] should be
     * contained in [collection].
     */
    infix fun DataItem<*>.within(collection: Collection<*>) =
        ContainsConditionComponent(this, collection)

    /**
     * Builds a [ComparisonConditionComponent] that specifies that the value of this [DataItem] should be
     * greater than the value of [other].
     */
    infix fun DataItem<*>.gt(other: DataItem<*>) =
        ComparisonConditionComponent(this, other, ComparisonOperatorName.GT)

    /**
     * Builds a [ComparisonConditionComponent] that specifies that the value of this should be
     * greater than the value of [other].
     */
    // This function exists so users don't have to wrap the objects as Value themselves
    infix fun Any.gt(other: Any) = toValue(this) gt toValue(other)

    /**
     * Builds a [ComparisonConditionComponent] that specifies that the value of this [DataItem] should be
     * greater or equal to the value of [other].
     */
    infix fun DataItem<*>.geq(other: DataItem<*>) =
        ComparisonConditionComponent(this, other, ComparisonOperatorName.GEQ)

    /**
     * Builds a [ComparisonConditionComponent] that specifies that the value of this should be
     * greater or equal to the value of [other].
     */
    // This function exists so users don't have to wrap the objects as Value themselves
    infix fun Any.geq(other: Any) = toValue(this) geq toValue(other)

    /**
     * Builds a [ComparisonConditionComponent] that specifies that the value of this [DataItem] should be
     * less than the value of [other].
     */
    infix fun DataItem<*>.lt(other: DataItem<*>) =
        ComparisonConditionComponent(this, other, ComparisonOperatorName.LT)

    /**
     * Builds a [ComparisonConditionComponent] that specifies that the value of this should be
     * less than the value of [other].
     */
    // This function exists so users don't have to wrap the objects as Value themselves
    infix fun Any.lt(other: Any) = toValue(this) lt toValue(other)

    /**
     * Builds a [ComparisonConditionComponent] that specifies that the value of this [DataItem] should be
     * less or equal to the value of [other].
     */
    infix fun DataItem<*>.leq(other: DataItem<*>) =
        ComparisonConditionComponent(this, other, ComparisonOperatorName.LEQ)

    /**
     * Builds a [ComparisonConditionComponent] that specifies that the value of this should be
     * less or equal to the value of [other].
     */
    // This function exists so users don't have to wrap the objects as Value themselves
    infix fun Any.leq(other: Any) = toValue(this) leq toValue(other)

    /**
     * Builds a [ComparisonConditionComponent] that specifies that the value of this [DataItem] should be
     * equal to the value of [other].
     */
    infix fun DataItem<*>.eq(other: DataItem<*>) =
        ComparisonConditionComponent(this, other, ComparisonOperatorName.EQ)

    /**
     * Builds a [ComparisonConditionComponent] that specifies that the value of this should be
     * equal to the value of [other].
     */
    // This function exists so users don't have to wrap the objects as Value themselves
    infix fun Any.eq(other: Any) = toValue(this) eq toValue(other)

    /**
     * Builds a [ComparisonConditionComponent] that specifies that the value of this [DataItem] should not be
     * equal to the value of [other].
     */
    infix fun DataItem<*>.neq(other: DataItem<*>) =
        ComparisonConditionComponent(this, other, ComparisonOperatorName.NEQ)

    /**
     * Builds a [ComparisonConditionComponent] that specifies that the value of this should not be
     * equal to the value of [other].
     */
    // This function exists so users don't have to wrap the objects as Value themselves
    infix fun Any.neq(other: Any) = toValue(this) neq toValue(other)

    /**
     * Builds a [CallConditionComponent] that specifies that [op] should be called.
     */
    // TODO: Is this needed?
    fun call(op: Op) = CallConditionComponent(op)

    /**
     * Connects two [ConditionComponent]s with a logical and.
     */
    infix fun ConditionComponent.and(other: ConditionComponent) =
        BinaryLogicalConditionComponent(this, other, BinaryLogicalOperatorName.AND)

    /**
     * Connects two [ConditionComponent]s with a logical or.
     */
    infix fun ConditionComponent.or(other: ConditionComponent) =
        BinaryLogicalConditionComponent(this, other, BinaryLogicalOperatorName.OR)

    /**
     * Negates a [ConditionComponent]
     */
    fun not(conditionComponent: ConditionComponent) = UnaryConditionComponent(conditionComponent, UnaryOperatorName.NOT)

    private fun toValue(value: Any): DataItem<*> =
        when (value) {
            is DataItem<*> -> value
            else -> Value(value)
        }
}
