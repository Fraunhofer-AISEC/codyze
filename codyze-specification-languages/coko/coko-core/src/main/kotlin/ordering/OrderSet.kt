@file:Suppress("UNUSED")

package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.ordering

import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.dsl.set

/**
 * Represents a regex set. Its [toNode] method converts the set into a group with OR expressions to
 * simplify the resulting binary tree
 */
class OrderSet(private var negate: Boolean) : OrderBuilder() {
    //    TODO: implement. How should this be represented in the NFA?
    //    /** Negates the current set (`[^abcedfu]`), making it match any character *not* in the
    // set. */
    //    operator fun not() = apply { negate = !negate }

    /** Represent this [OrderSet] ([OrderFragment]) as a binary syntax tree. */
    override fun toNode(): OrderNode {
        var currentNode =
            when (orderNodes.size) {
                0 ->
                    throw IllegalArgumentException(
                        "Groups and sets must have at least one element."
                    )
                1 -> return orderNodes.removeFirst()
                else ->
                    AlternativeOrderNode(
                        left = orderNodes.removeFirst(),
                        right = orderNodes.removeFirst()
                    )
            }

        while (orderNodes.size > 0) {
            currentNode = AlternativeOrderNode(left = currentNode, right = orderNodes.removeFirst())
        }
        return currentNode
    }
}

/** Allows the syntactic sugar to create a set with the 'get' operator. */
class OrderSetGetOperator {
    context(OrderBuilder)
    operator fun get(vararg tokens: OrderToken) = set { tokens.forEach { +it } }
}
