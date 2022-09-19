@file:Suppress("UNUSED")

package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.dsl

import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.dsl.ordering.*
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.ordering.OrderQuantifier
import kotlin.jvm.internal.CallableReference

//
// token conversion
//
context(OrderBuilder)
/** Convert a [OrderToken] into a TerminalNode */
inline val OrderToken.token: TerminalNode
    get() = TerminalNode((this as CallableReference).owner.toString(), name)

context(OrderBuilder)
/**
 * Convert a [OrderToken] into a TerminalNode and specify the arguments passed to the [OrderToken]
 * when evaluating the order
 */
// TODO: add implicit receiver to 'block' that provides a way to specify arguments
fun OrderToken.token(block: () -> Unit): OrderFragment =
    TerminalNode((this as CallableReference).owner.toString(), name, block)

//
// groups
//
context(OrderBuilder)
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
inline fun group(
    block: OrderGroup.() -> Unit,
) = OrderGroup().apply(block)

context(OrderBuilder)
/**
 * Minimalist way to create a group with a function call. However, this minimalist [group]
 * constructor only works with [OrderToken]s
 * ```kt
 * order {
 *   +group(arg1::func1, arg1::func2, arg1::func3)
 * }
 * ```
 */
fun group(vararg tokens: OrderFragment) = group { tokens.forEach { +it } }

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
 *   +set[arg1::func1, arg1::func2, arg1::func4]
 * }
 * ```
 */
val set by lazy { OrderSetGetOperator() }

context(OrderBuilder)
/**
 * Add a set to the [Order] containing any valid OrderDsl provided by a lambda (see [group]).
 *
 * > Match any [OrderToken] in the set.
 *
 * @param negate when set to true, the set will be negated (`[^arg1::func1, arg1::func2]`) and match
 * any [OrderToken]] not in the set.
 * @see [OrderSet.not]
 */
inline fun set(negate: Boolean = false, block: OrderSet.() -> Unit) = OrderSet(negate).apply(block)

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
    QuantifierNode(child = toNode(), type = OrderQuantifier.COUNT, value = count)

context(OrderBuilder)
/**
 * Appends a range quantifier (`{1,3}`) to the current [OrderFragment].
 *
 * > Matches the specified quantity of the previous [OrderFragment]. > {1,3} will match 1 to 3.
 */
infix fun OrderFragment.between(range: IntRange): OrderFragment =
    QuantifierNode(child = toNode(), type = OrderQuantifier.BETWEEN, value = range)

context(OrderBuilder)
/**
 * Appends a minimum quantifier (`{3,}`) to the current [OrderFragment].
 *
 * > Matches the specified quantity of the previous [OrderFragment]. > {3,} will match 3 or more.
 */
infix fun OrderFragment.atLeast(min: Int): OrderFragment =
    QuantifierNode(child = toNode(), type = OrderQuantifier.ATLEAST, value = min)

context(OrderBuilder)
/**
 * Appends a plus quantifier (`+`) to the current [OrderFragment].
 *
 * > Matches 1 or more of the preceding [OrderFragment].
 */
fun OrderFragment.some(): OrderFragment =
    QuantifierNode(child = toNode(), type = OrderQuantifier.SOME)

context(OrderBuilder)
/**
 * Appends a star quantifier (`*`) to the current [OrderFragment].
 *
 * > Matches 0 or more of the preceding [OrderFragment].
 */
fun OrderFragment.maybe(): OrderFragment =
    QuantifierNode(child = toNode(), type = OrderQuantifier.MAYBE)

context(OrderBuilder)
/**
 * Appends an optional quantifier (`?`) to the current [OrderFragment].
 *
 * > Matches 0 or 1 of the preceding [OrderFragment], effectively making it optional.
 */
fun OrderFragment.option(): OrderFragment =
    QuantifierNode(child = toNode(), type = OrderQuantifier.OPTION)

context(OrderBuilder)
/**
 * Appends an alternation token (`|`) to the current [OrderFragment].
 *
 * > Acts like a boolean OR. Matches the expression before or after the |. > It can operate within a
 * group, or on a whole expression. The patterns will be tested in order.
 */
infix fun OrderFragment.or(other: OrderFragment): OrderFragment =
    AlternativeNode(left = toNode(), right = other)
