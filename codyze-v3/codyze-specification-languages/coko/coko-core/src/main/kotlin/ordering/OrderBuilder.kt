@file:Suppress("UNUSED")

package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.ordering

import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.CokoMarker
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.Project
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.dsl.token

/**
 * Extends the [OrderBuilder] with the [toNfa] and [toDfa] methods, which represents the [Order] as
 * a [NFA] and [DFA] respectively
 */
class Order : OrderBuilder() {
    /**
     * Constructs a NFA using Thompson's construction algorithm
     * @see [YouTube](https://youtu.be/HLOAwCCYVxE)
     */
    fun toNfa() = toNode().toNfa()
}

@CokoMarker
/**
 * Base class for [Order], [OrderGroup] and [OrderSet]. Uses a deque to create a binary tree of the
 * given regex with its [toNode] method.
 */
abstract class OrderBuilder : OrderFragment {
    protected val orderNodeDeque = ArrayDeque<OrderNode>()

    context(Project)
    /** Add an [OrderToken] to the [orderNodeDeque] */
    operator fun OrderToken.unaryPlus() = this@OrderBuilder.add(this)

    /** Add an [OrderFragment] to the [orderNodeDeque] */
    operator fun OrderFragment.unaryPlus() = this@OrderBuilder.add(this)

    context(Project)
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
