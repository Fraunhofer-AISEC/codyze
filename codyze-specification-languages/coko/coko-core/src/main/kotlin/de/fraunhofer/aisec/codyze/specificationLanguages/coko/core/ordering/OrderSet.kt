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
@file:Suppress("UNUSED")

package de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.ordering

import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.set

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
