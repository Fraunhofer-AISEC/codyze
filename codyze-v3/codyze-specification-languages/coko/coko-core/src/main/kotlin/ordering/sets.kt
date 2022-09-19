@file:Suppress("UNUSED")

package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.dsl.ordering

import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.dsl.set

/**
 * Represents a regex set. Its [toNode] method converts the set into a group with OR expressions to
 * simplify the resulting binary tree
 */
class OrderSet(private var negate: Boolean) : OrderBuilder() {
    /** Negates the current set (`[^abcedfu]`), making it match any character *not* in the set. */
    operator fun not() = apply { negate = !negate }

    /**
     * Represent this [OrderSet] ([OrderFragment]) as a binary tree.
     */
    override fun toNode(): OrderFragment {
        var currentNode =
            AlternativeNode(left = nodeStack.removeFirst(), right = nodeStack.removeFirst())
        while (nodeStack.size > 0) {
            currentNode = AlternativeNode(left = currentNode, right = nodeStack.removeFirst())
        }
        return currentNode
    }
}

/** Allows the syntactic sugar to create a set with the 'get' operator. */
class OrderSetGetOperator {
    context(OrderBuilder)
    operator fun get(vararg tokens: OrderToken) = set { tokens.forEach { +it } }
}
