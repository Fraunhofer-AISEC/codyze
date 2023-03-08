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
@file:Suppress("UNUSED", "TooManyFunctions")

package de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl

import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.ordering.*

/** [OrderBuilder] subclass to hide some implementation details of [OrderBuilder] to coko users. */
class Order : OrderBuilder()

context(OrderBuilder)
/** Convert an [OrderToken] into a TerminalOrderNode */
internal val OrderToken.token: TerminalOrderNode
    get() = toTerminalOrderNode()

//
// groups
//
context(OrderBuilder)
/**
 * Add a group containing any valid OrderDsl provided as a lambda
 *
 * ```kt
 * order {
 *   group {
 *      - arg1::func1
 *      many(arg1::func2)
 *      option {
 *          - arg1::func2
 *          - arg1::func3
*       }
 *   }
 * }
 * ```
 */
inline fun group(
    block: OrderGroup.() -> Unit,
) = this@OrderBuilder.add(OrderGroup().apply(block))

context(OrderBuilder)
/** Adds a group with the [maybe] ('*') qualifier. See [group] */
inline fun maybe(
    block: OrderGroup.() -> Unit,
): OrderNode = this@OrderBuilder.add(
    QuantifierOrderNode(child = OrderGroup().apply(block).toNode(), type = OrderQuantifier.MAYBE)
)

context(OrderBuilder)
/** Adds a group with the [option] ('?') qualifier. See [group] */
inline fun option(
    block: OrderGroup.() -> Unit,
): OrderNode = this@OrderBuilder.add(
    QuantifierOrderNode(child = OrderGroup().apply(block).toNode(), type = OrderQuantifier.OPTION)
)

context(OrderBuilder)
/** Adds a group with the [some] ('+') qualifier. See [group] */
inline fun some(
    block: OrderGroup.() -> Unit,
): OrderNode = this@OrderBuilder.add(
    QuantifierOrderNode(child = OrderGroup().apply(block).toNode(), type = OrderQuantifier.ATLEAST, value = 1)
)

context(OrderBuilder)
/** Adds a group with the [count] qualifier. See [group] */
inline fun count(
    count: Int,
    block: OrderGroup.() -> Unit,
): OrderNode = this@OrderBuilder.add(
    QuantifierOrderNode(child = OrderGroup().apply(block).toNode(), type = OrderQuantifier.COUNT, value = count)
)

context(OrderBuilder)
/** Adds a group with the [between] qualifier. See [group] */
inline fun between(
    range: IntRange,
    block: OrderGroup.() -> Unit,
): OrderNode = this@OrderBuilder.add(
    QuantifierOrderNode(child = OrderGroup().apply(block).toNode(), type = OrderQuantifier.BETWEEN, value = range)
)

context(OrderBuilder)
/** Adds a group with the [atLeast] qualifier. See [group] */
inline fun atLeast(
    count: Int,
    block: OrderGroup.() -> Unit,
): OrderNode = this@OrderBuilder.add(
    QuantifierOrderNode(child = OrderGroup().apply(block).toNode(), type = OrderQuantifier.ATLEAST, value = count)
)

/**
 * Minimalist way to create a group with a function call. However, this minimalist [group]
 * constructor only works with [OrderToken]s
 * ```kt
 * order {
 *   group(arg1::func1, arg1::func2, arg1::func3)
 * }
 * ```
 */
fun OrderBuilder.group(vararg tokens: OrderToken): OrderNode = group { tokens.forEach { add(it) } }

context(OrderBuilder)
/** Minimalist way to create a group with the [maybe] ('*') qualifier. See [group]. */
fun maybe(vararg tokens: OrderToken): OrderNode = maybe { tokens.forEach { add(it) } }

context(OrderBuilder)
/** Minimalist way to create a group with the [some] ('+') qualifier. See [group]. */
fun some(vararg tokens: OrderToken): OrderNode = some { tokens.forEach { add(it) } }

context(OrderBuilder)
/** Minimalist way to create a group with the [option] ('?') qualifier. See [group]. */
fun option(vararg tokens: OrderToken): OrderNode = option { tokens.forEach { add(it) } }

context(OrderBuilder)
/** Minimalist way to create a group with the [count] qualifier. See [group]. */
fun count(count: Int, vararg tokens: OrderToken): OrderNode = count(count) { tokens.forEach { add(it) } }

context(OrderBuilder)
/** Minimalist way to create a group with the [between] qualifier. See [group]. */
fun between(range: IntRange, vararg tokens: OrderToken): OrderNode = between(range) { tokens.forEach { add(it) } }

context(OrderBuilder)
/** Minimalist way to create a group with the [atLeast] qualifier. See [group]. */
fun atLeast(min: Int, vararg tokens: OrderToken): OrderNode = atLeast(min) { tokens.forEach { add(it) } }

//
// sets
//
context(OrderBuilder)
/**
 * Use this to create a set with the [OrderSetGetOperator.get] operator.
 *
 * Like the minimalist [group] constructor, this also only works with [OrderToken]s
 *
 * ```kt
 * order {
 *   set[arg1::func1, arg1::func2, arg1::func4]
 * }
 * ```
 */
val set by lazy { OrderSetGetOperator() }

context(OrderBuilder)
/**
 * Add a set to the [Order] containing any valid OrderDsl provided by a lambda (see [group]).
 *
 * Match any [OrderToken] in the set.
 */
inline fun set(block: OrderSet.() -> Unit) = OrderSet(false).apply(block).apply { this@OrderBuilder.add(this.toNode()) }

// TODO: negating an [OrderSet] is currently not implemented -> combine with above function
// /**
// *  * Add a set to the [Order] containing any valid OrderDsl provided by a lambda (see [group]).
// *
// * > Match any [OrderToken] in the set.
// *
// * @param negate when set to true, the set will be negated (`[^arg1::func1, arg1::func2]`) and
// match
// * any [OrderToken]] not in the set.
// * @see [OrderSet.not]
// */
// inline fun set(negate: Boolean = false, block: OrderSet.() -> Unit) =
// OrderSet(negate).apply(block)

//
// OR stuff
//
context(OrderBuilder)
/**
 * Adds an alternation token (`|`) between the current [OrderFragment] and [other].
 * All [OrderToken] are converted into [OrderFragment]s.
 *
 * Acts like a boolean OR. Matches the expression before or after the |.
 *
 * It can operate within a group, or on a whole expression. The patterns will be tested in order.
 */
infix fun OrderFragment.or(other: OrderFragment): OrderFragment =
    this@OrderBuilder.add(AlternativeOrderNode(left = this@OrderFragment.toNode(), right = other.toNode())).also {
        this@OrderBuilder.remove(this@OrderFragment)
        this@OrderBuilder.remove(other)
    }

context(OrderBuilder)
/**
 * Adds an alternation token (`|`) between the current [OrderToken] and [other].
 * All [OrderToken] are converted into [OrderFragment]s.
 *
 * Acts like a boolean OR. Matches the expression before or after the |.
 *
 * It can operate within a group, or on a whole expression. The patterns will be tested in order.
 */
infix fun OrderToken.or(other: OrderFragment): OrderFragment = this@OrderToken.token or other

context(OrderBuilder)
/**
 * Adds an alternation token (`|`) between the current [OrderToken] and [other].
 * All [OrderToken] are converted into [OrderFragment]s.
 *
 * Acts like a boolean OR. Matches the expression before or after the |.
 *
 * It can operate within a group, or on a whole expression. The patterns will be tested in order.
 */
infix fun OrderToken.or(other: OrderToken): OrderFragment = this@OrderToken.token or other.token

context(OrderBuilder)
/**
 * Adds an alternation token (`|`) between the current [OrderFragment] and [other].
 * All [OrderToken] are converted into [OrderFragment]s.
 *
 * Acts like a boolean OR. Matches the expression before or after the |.
 *
 * It can operate within a group, or on a whole expression. The patterns will be tested in order.
 */
infix fun OrderFragment.or(other: OrderToken): OrderFragment = this@OrderFragment or other.token
