@file:Suppress("UNUSED")

package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.dsl.ordering

import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.dsl.token

/** Subclasses [OrderBuilder] to provide a more simplistic experience for the DSL users */
class Order : OrderBuilder()

/**
 * Base class for [Order], [OrderGroup] and [OrderSet]. Uses a deque to create a binary tree of the
 * given regex.
 */
abstract class OrderBuilder : OrderFragment {
    protected val nodeDeque = ArrayDeque<OrderFragment>()

    /** Add an [OrderToken] to the [nodeDeque] */
    operator fun OrderToken.unaryPlus() = this@OrderBuilder.add(this)

    /** Add an [OrderFragment] to the [nodeDeque] */
    operator fun OrderFragment.unaryPlus() = this@OrderBuilder.add(this)

    private fun add(token: OrderToken) {
        nodeDeque.add(token.token)
    }

    private fun add(fragment: OrderFragment) {
        nodeDeque.add(fragment.toNode())
    }

    fun testThis() {
        println("test")
    }

    /**
     * Represent this [OrderFragment] as a binary syntax tree.
     */
    override fun toNode(): OrderFragment {
        var currentNode = when(nodeDeque.size) {
            0 -> throw IllegalArgumentException("Groups and sets must have at least one element.")
            1 -> return nodeDeque.removeFirst()
            else -> SequenceNode(left = nodeDeque.removeFirst(), right = nodeDeque.removeFirst())
        }

        while (nodeDeque.size > 0) {
            currentNode = SequenceNode(left = currentNode, right = nodeDeque.removeFirst())
        }
        return currentNode
    }
}
