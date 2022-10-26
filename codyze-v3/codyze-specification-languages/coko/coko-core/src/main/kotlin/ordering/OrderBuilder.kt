@file:Suppress("UNUSED")

package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.ordering

import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.CokoBackend
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.CokoMarker
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.dsl.token

/** [OrderBuilder] subclass to hide some implementation details of [OrderBuilder] to coko users. */
class Order : OrderBuilder()

@CokoMarker
/**
 * Base class for [Order], [OrderGroup] and [OrderSet]. Uses a deque to create a binary tree of the
 * given regex with its [toNode] method.
 */
abstract class OrderBuilder : OrderFragment {
    protected val orderNodeDeque = ArrayDeque<OrderNode>()

    context(CokoBackend)
    /** Add an [OrderToken] to the [orderNodeDeque] */
    operator fun OrderToken.unaryPlus() = this@OrderBuilder.add(this)

    /** Add an [OrderFragment] to the [orderNodeDeque] */
    operator fun OrderFragment.unaryPlus() = this@OrderBuilder.add(this)

    context(CokoBackend)
    private fun add(token: OrderToken) {
        this@OrderBuilder.orderNodeDeque.add(token.token)
    }

    private fun add(fragment: OrderFragment) {
        orderNodeDeque.add(fragment.toNode())
    }

    /** Represent this [OrderFragment] as a binary syntax tree. */
    override fun toNode(): OrderNode {
        var currentNode =
            when (orderNodeDeque.size) {
                0 ->
                    throw IllegalArgumentException(
                        "Groups and sets must have at least one element."
                    )
                1 -> return orderNodeDeque.removeFirst()
                else ->
                    SequenceOrderNode(
                        left = orderNodeDeque.removeFirst(),
                        right = orderNodeDeque.removeFirst()
                    )
            }

        while (orderNodeDeque.size > 0) {
            currentNode =
                SequenceOrderNode(left = currentNode, right = orderNodeDeque.removeFirst())
        }
        return currentNode
    }
}
