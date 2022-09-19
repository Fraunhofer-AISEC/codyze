@file:Suppress("UNUSED")

package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.dsl.ordering

import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.dsl.token

/** Subclasses [OrderBuilder] to provide a more simplistic experience for the DSL users */
class Order : OrderBuilder()

/**
 * Base class for [Order], [OrderGroup] and [OrderSet]. Uses a stack to create a binary tree of the
 * given regex.
 */
abstract class OrderBuilder : OrderFragment {
    protected val nodeStack = ArrayDeque<OrderFragment>()

    /** Add an [OrderToken] to the [nodeStack] */
    operator fun OrderToken.unaryPlus() = this@OrderBuilder.add(this)

    /** Add an [OrderFragment] to the [nodeStack] */
    operator fun OrderFragment.unaryPlus() = this@OrderBuilder.add(this)

    private fun add(token: OrderToken) {
        nodeStack.add(token.token)
    }

    private fun add(fragment: OrderFragment) {
        nodeStack.add(fragment.toNode())
    }

    /**
     * Represent this [OrderFragment] as a binary tree.
     */
    override fun toNode(): OrderFragment {
        var currentNode =
            SequenceNode(left = nodeStack.removeFirst(), right = nodeStack.removeFirst())
        while (nodeStack.size > 0) {
            currentNode = SequenceNode(left = currentNode, right = nodeStack.removeFirst())
        }
        return currentNode
    }
}
