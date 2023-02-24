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

import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.CokoMarker
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.Op
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.token

@CokoMarker
/**
 * Base class for [Order], [OrderGroup] and [OrderSet]. Creates a binary tree of the
 * given regex with its [toNode] method.
 */
open class OrderBuilder : OrderFragment {
    // the nodes for the [Op]s in this map must be retrieved using [Op.getNodes]
    val userDefinedOps = mutableMapOf<String, Op>()

    protected val orderNodes = mutableListOf<OrderNode>() // this is emptied in [toNode]

    /** Add an [OrderToken] to the [orderNodes] */
    operator fun OrderToken.unaryPlus() = add(this)

    /** Add an [OrderFragment] to the [orderNodes] */
    operator fun OrderFragment.unaryPlus() = add(this)

    /** Add an [OrderToken] to the [orderNodes] */
    fun add(token: OrderToken) = orderNodes.add(token.token)

    /**
     * Add an [OrderFragment] to the [orderNodes].
     * All instances of the [fragment] object are removed from the list before the OrderNode from [fragment] is added.
     *
     * The reason why all instances of [fragment] are removed is to ensure consistent behavior of
     * [de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.or].
     * [de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.or] might receive [OrderFragment]s as arguments.
     * [OrderFragment]s can only be built with Order DSL functions which will add their resulting [OrderFragment]
     * directly to the [orderNodes] list.
     * This means that [de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.or] must remove the
     * [OrderFragment]s from the [orderNodes] list to prevent them from appearing multiple times.
     * An example would be:
     * ```kt
     *  order {
     *      maybe(TestClass::a)
     *      maybe(TestClass::a) or some(TestClass::b)
     *  }
     * ```
     * The regex would be (a* (a* | b+)).
     * If the [OrderFragment]s from `maybe(TestClass::a)` and `some(TestClass::b)` were not removed from
     * the [orderNodes], the regex would be (a* a* b+ (a* | b+)) which is incorrect.
     *
     * However, problems arise if we consider a second example:
     * ```kt
     *  order {
     *      val maybeA = maybe(TestClass::a)
     *      maybeA or some(TestClass::b)
     *  }
     * ```
     * The desired regex would still be (a* (a* | b+)).
     * However, this is a problem for [de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.or].
     * It cannot differentiate if a [OrderFragment] was stored in a variable or not.
     * Therefore, the [OrderFragment] is always removed.
     * This means that the resulting regex is actually (a* | b+).
     *
     * To resolve this inconsistency, we decided to disallow the same [OrderFragment] object (object reference equality,
     * not functional equality) appearing multiple times in the [orderNodes] list.
     * Instead, the last appearance is used as position for the [OrderFragment] object.
     * This means, that for the example:
     * ```kt
     *  order {
     *      val maybeA = maybe(TestClass::a)
     *      add(maybeA)
     *      some(TestClass::b)
     *      add(maybeA)
     *  }
     * ```
     * the regex is (b+ a*).
     *
     * If the desired regex is `(a* a* b+ a*)`, please declare the `a*` with separate function calls like:
     * ```kt
     *  order {
     *      maybe(TestClass::a)
     *      maybe(TestClass::a)
     *      some(TestClass::b)
     *      maybe(TestClass::a)
     *  }
     * ```
     */
    fun add(fragment: OrderFragment): OrderNode =
        fragment.toNode().also {
            remove(fragment)
            remove(it)
            orderNodes.add(it)
        }

    /** Remove all instance of [fragment] from the [orderNodes] */
    fun remove(fragment: OrderFragment) {
        orderNodes.removeIf {
            it === fragment
        }
    }

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
