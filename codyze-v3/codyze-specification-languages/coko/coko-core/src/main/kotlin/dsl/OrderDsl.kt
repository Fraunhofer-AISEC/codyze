@file:Suppress("UNUSED")

package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.dsl

import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.CokoBackend
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.ordering.*

/** [OrderBuilder] subclass to hide some implementation details of [OrderBuilder] to coko users. */
class Order : OrderBuilder()

//
// token conversion
//
context(OrderBuilder, CokoBackend)
/**
 * Convert a [OrderToken] into a TerminalOrderNode and specify the arguments passed to the
 * [OrderToken] when evaluating the order
 */
fun OrderToken.use(block: () -> Op): OrderFragment =
    TerminalOrderNode(this) { with(this@CokoBackend) { block().getNodes() } }

context(OrderBuilder, CokoBackend)
/** Convert an [OrderToken] into a TerminalOrderNode */
internal val OrderToken.token: TerminalOrderNode
    get() = TerminalOrderNode(this) { with(this@CokoBackend) { this@token.call().getAllNodes() } }

//
// groups
//
/**
 * Add a group containing any valid OrderDsl provided as a lambda
 *
 * ```kt
 * order {
 *   +group {
 *      +arg1::func1
 *      +arg1::func2.token.many()
 *      +group(arg1::func2, arg1::func3).option()
 *   }
 * }
 * ```
 */
inline fun OrderBuilder.group(
    block: OrderGroup.() -> Unit,
) = OrderGroup().apply(block)

/** Shortcut for creating a group with the [maybe] ('*') qualifier. See [group] */
inline fun OrderBuilder.maybe(
    block: OrderGroup.() -> Unit,
) = OrderGroup().apply(block).maybe()

/** Shortcut for creating a group with the [option] ('?') qualifier. See [group] */
inline fun OrderBuilder.option(
    block: OrderGroup.() -> Unit,
) = OrderGroup().apply(block).option()

/** Shortcut for creating a group with the [some] ('+') qualifier. See [group] */
inline fun OrderBuilder.some(
    block: OrderGroup.() -> Unit,
) = OrderGroup().apply(block).some()

/** Shortcut for creating a group with the [count] qualifier. See [group] */
inline fun OrderBuilder.count(
    count: Int,
    block: OrderGroup.() -> Unit,
) = OrderGroup().apply(block).count(count)

/** Shortcut for creating a group with the [between] qualifier. See [group] */
inline fun OrderBuilder.between(
    range: IntRange,
    block: OrderGroup.() -> Unit,
) = OrderGroup().apply(block).between(range)

/** Shortcut for creating a group with the [atLeast] qualifier. See [group] */
inline fun OrderBuilder.atLeast(
    count: Int,
    block: OrderGroup.() -> Unit,
) = OrderGroup().apply(block).atLeast(count)

context(CokoBackend)
/**
 * Minimalist way to create a group with a function call. However, this minimalist [group]
 * constructor only works with [OrderToken]s
 * ```kt
 * order {
 *   +group(arg1::func1, arg1::func2, arg1::func3)
 * }
 * ```
 */
fun OrderBuilder.group(vararg tokens: OrderToken) = group { tokens.forEach { +it } }

context(CokoBackend)
/** Minimalist way to create a group with the [maybe] ('*') qualifier. See [group]. */
fun OrderBuilder.maybe(vararg tokens: OrderToken) = maybe { tokens.forEach { +it } }

context(CokoBackend)
/** Minimalist way to create a group with the [some] ('+') qualifier. See [group]. */
fun OrderBuilder.some(vararg tokens: OrderToken) = some { tokens.forEach { +it } }

context(CokoBackend)
/** Minimalist way to create a group with the [option] ('?') qualifier. See [group]. */
fun OrderBuilder.option(vararg tokens: OrderToken) = option { tokens.forEach { +it } }

context(CokoBackend)
/** Minimalist way to create a group with the [count] qualifier. See [group]. */
fun OrderBuilder.count(count: Int, vararg tokens: OrderToken) = count(count) { tokens.forEach { +it } }

context(CokoBackend)
/** Minimalist way to create a group with the [between] qualifier. See [group]. */
fun OrderBuilder.between(range: IntRange, vararg tokens: OrderToken) = between(range) { tokens.forEach { +it } }

context(CokoBackend)
/** Minimalist way to create a group with the [atLeast] qualifier. See [group]. */
fun OrderBuilder.atLeast(count: Int, vararg tokens: OrderToken) = atLeast(count) { tokens.forEach { +it } }

//
// sets
//
context(CokoBackend)
/**
 * Use this to create a set with the [OrderSetGetOperator.get] operator.
 *
 * Like the minimalist [group] constructor, this also only works with [OrderToken]s
 *
 * ```kt
 * order {
 *   +set[arg1::func1, arg1::func2, arg1::func4]
 * }
 * ```
 */
val OrderBuilder.set by lazy { OrderSetGetOperator() }

/**
 * Add a set to the [Order] containing any valid OrderDsl provided by a lambda (see [group]).
 *
 * > Match any [OrderToken] in the set.
 */
inline fun OrderBuilder.set(block: OrderSet.() -> Unit) = OrderSet(false).apply(block)

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
// quantifiers
//
context(OrderBuilder)
/**
 * Appends an exact quantifier (`{3}`) to the current [OrderFragment].
 *
 * > Matches the specified quantity of the previous [OrderFragment]. > {3} will match exactly 3.
 */
infix fun OrderFragment.count(count: Int): OrderFragment =
    QuantifierOrderNode(child = token.toNode(), type = OrderQuantifier.COUNT, value = count)

context(OrderBuilder, CokoBackend)
/**
 * Converts the [OrderToken] to an [OrderFragment] and appends an exact quantifier (`{3}`) to the
 * current [OrderFragment].
 *
 * > Matches the specified quantity of the previous [OrderFragment]. > {3} will match exactly 3.
 */
infix fun OrderToken.count(count: Int): OrderFragment =
    QuantifierOrderNode(child = token.toNode(), type = OrderQuantifier.COUNT, value = count)

context(OrderBuilder)
/**
 * Appends a range quantifier (`{1,3}`) to the current [OrderFragment].
 *
 * > Matches the specified quantity of the previous [OrderFragment]. > {1,3} will match 1 to 3.
 */
infix fun OrderFragment.between(range: IntRange): OrderFragment =
    QuantifierOrderNode(child = token.toNode(), type = OrderQuantifier.BETWEEN, value = range)

context(OrderBuilder, CokoBackend)
/**
 * Converts the [OrderToken] to an [OrderFragment] and appends a range quantifier (`{1,3}`) to the
 * current [OrderFragment].
 *
 * > Matches the specified quantity of the previous [OrderFragment]. > {1,3} will match 1 to 3.
 */
infix fun OrderToken.between(range: IntRange): OrderFragment =
    QuantifierOrderNode(child = token.toNode(), type = OrderQuantifier.BETWEEN, value = range)

context(OrderBuilder)
/**
 * Appends a minimum quantifier (`{3,}`) to the current [OrderFragment].
 *
 * > Matches the specified quantity of the previous [OrderFragment]. > {3,} will match 3 or more.
 */
infix fun OrderFragment.atLeast(min: Int): OrderFragment =
    QuantifierOrderNode(child = token.toNode(), type = OrderQuantifier.ATLEAST, value = min)

context(OrderBuilder, CokoBackend)
/**
 * Converts the [OrderToken] to an [OrderFragment] and appends a minimum quantifier (`{3,}`) to the
 * current [OrderFragment].
 *
 * > Matches the specified quantity of the previous [OrderFragment]. > {3,} will match 3 or more.
 */
infix fun OrderToken.atLeast(min: Int): OrderFragment =
    QuantifierOrderNode(child = token.toNode(), type = OrderQuantifier.ATLEAST, value = min)

context(OrderBuilder)
/**
 * Appends a [some] quantifier (`+`) to the current [OrderFragment]. The + quantifier is
 * automatically converted into a 'ATLEAST(1)' quantifier.
 *
 * > Matches 1 or more of the preceding [OrderFragment].
 */
fun OrderFragment.some(): OrderFragment =
    QuantifierOrderNode(child = token.toNode(), type = OrderQuantifier.ATLEAST, value = 1)

context(OrderBuilder, CokoBackend)
/**
 * Converts the [OrderToken] to an [OrderFragment] and appends a [some] quantifier (`+`) to the
 * current [OrderFragment]. The + quantifier is automatically converted into a 'ATLEAST(1)'
 * quantifier.
 *
 * > Matches 1 or more of the preceding [OrderFragment].
 */
fun OrderToken.some(): OrderFragment =
    QuantifierOrderNode(child = token.toNode(), type = OrderQuantifier.ATLEAST, value = 1)

context(OrderBuilder)
/**
 * Appends a [maybe] quantifier (`*`) to the current [OrderFragment].
 *
 * > Matches 0 or more of the preceding [OrderFragment].
 */
fun OrderFragment.maybe(): OrderFragment =
    QuantifierOrderNode(child = token.toNode(), type = OrderQuantifier.MAYBE)

context(OrderBuilder, CokoBackend)
/**
 * Converts the [OrderToken] to an [OrderFragment] and appends a [maybe] quantifier (`*`) to the
 * current [OrderFragment].
 *
 * > Matches 0 or more of the preceding [OrderFragment].
 */
fun OrderToken.maybe(): OrderFragment =
    QuantifierOrderNode(child = token.toNode(), type = OrderQuantifier.MAYBE)

context(OrderBuilder)
/**
 * Appends an [option] quantifier (`?`) to the current [OrderFragment].
 *
 * > Matches 0 or 1 of the preceding [OrderFragment], effectively making it optional.
 */
fun OrderFragment.option(): OrderFragment =
    QuantifierOrderNode(child = token.toNode(), type = OrderQuantifier.OPTION)

context(OrderBuilder, CokoBackend)
/**
 * Converts the [OrderToken] to an [OrderFragment] and appends an [option] quantifier (`?`) to the
 * current [OrderFragment].
 *
 * > Matches 0 or 1 of the preceding [OrderFragment], effectively making it optional.
 */
fun OrderToken.option(): OrderFragment =
    QuantifierOrderNode(child = token.toNode(), type = OrderQuantifier.OPTION)

//
// OR stuff
//
context(OrderBuilder, CokoBackend)
/**
 * Appends an alternation token (`|`) to the current [OrderFragment].
 *
 * > Acts like a boolean OR. Matches the expression before or after the |. > It can operate within a
 * group, or on a whole expression. The patterns will be tested in order.
 */
infix fun OrderFragment.or(other: OrderFragment): OrderFragment =
    AlternativeOrderNode(left = toNode(), right = other.toNode())

context(OrderBuilder, CokoBackend)
/**
 * Converts the [OrderToken] to an [OrderFragment] and appends an alternation token (`|`) to the
 * current [OrderFragment].
 *
 * > Acts like a boolean OR. Matches the expression before or after the |. > It can operate within a
 * group, or on a whole expression. The patterns will be tested in order.
 */
infix fun OrderToken.or(other: OrderFragment): OrderFragment =
    AlternativeOrderNode(left = token.toNode(), right = other.toNode())

context(OrderBuilder, CokoBackend)
/**
 * Converts the [OrderToken] to an [OrderFragment] and appends an alternation token (`|`) to the
 * current [OrderFragment].
 *
 * > Acts like a boolean OR. Matches the expression before or after the |. > It can operate within a
 * group, or on a whole expression. The patterns will be tested in order.
 */
infix fun OrderToken.or(other: OrderToken): OrderFragment =
    AlternativeOrderNode(left = token.toNode(), right = other.token.toNode())

context(OrderBuilder, CokoBackend)
/**
 * Converts the [OrderToken] to an [OrderFragment] and appends an alternation token (`|`) to the
 * current [OrderFragment].
 *
 * > Acts like a boolean OR. Matches the expression before or after the |. > It can operate within a
 * group, or on a whole expression. The patterns will be tested in order.
 */
infix fun OrderFragment.or(other: OrderToken): OrderFragment =
    AlternativeOrderNode(left = toNode(), right = other.token.toNode())
