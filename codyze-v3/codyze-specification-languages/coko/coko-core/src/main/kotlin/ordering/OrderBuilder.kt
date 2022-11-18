@file:Suppress("UNUSED")

package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.ordering

import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.CokoMarker
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.dsl.Op
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.dsl.token

@CokoMarker
/**
 * Base class for [Order], [OrderGroup] and [OrderSet]. Uses a deque to create a binary tree of the
 * given regex with its [toNode] method.
 */
abstract class OrderBuilder : OrderFragment {
    // the nodes for the [Op]s in this map must be retrieved using [Op.getNodes]
    val userDefinedOps = mutableMapOf<String, () -> Op>()

    protected val orderNodes = mutableListOf<OrderNode>() // this is emptied in [toNode]

    /** Add an [OrderToken] to the [orderNodes] */
    operator fun OrderToken.unaryPlus() = add(this)

    /** Add an [OrderFragment] to the [orderNodes] */
    operator fun OrderFragment.unaryPlus() = add(this)

    private fun add(token: OrderToken) = orderNodes.add(token.token)

    private fun add(fragment: OrderFragment) = orderNodes.add(fragment.toNode())

    /** Represent this [OrderFragment] as a binary syntax tree. */
    override fun toNode(): OrderNode {
        var currentNode =
            when (orderNodes.size) {
                0 ->
                    throw IllegalArgumentException(
                        "Groups and sets must have at least one element."
                    )
                1 -> return orderNodes.removeFirst()
                else ->
                    SequenceOrderNode(
                        left = orderNodes.removeFirst(),
                        right = orderNodes.removeFirst()
                    )
            }

        while (orderNodes.size > 0) {
            currentNode = SequenceOrderNode(left = currentNode, right = orderNodes.removeFirst())
        }
        return currentNode
    }
}
