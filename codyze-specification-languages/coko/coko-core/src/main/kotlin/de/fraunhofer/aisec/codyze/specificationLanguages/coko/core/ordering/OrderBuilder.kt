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
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.Order
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

    /** Add an [Op] to the [userDefinedOps] */
    operator fun Op.unaryMinus(): OrderNode = this@OrderBuilder.add(this)

    /** Add an [OrderToken] to the [orderNodes] */
    operator fun OrderToken.unaryMinus(): OrderNode = add(this)

    /** Add an [OrderFragment] to the [orderNodes] */
    operator fun OrderFragment.unaryMinus(): OrderNode = add(this)

    fun add(token: OrderToken): OrderNode {
        val orderNode = token.token
        orderNodes.add(orderNode)
        return orderNode
    }

    /**
     * Add an [OrderFragment] to the [orderNodes].
     * All instances of the [fragment] object are removed from the list before the OrderNode from [fragment] is added.
     */
    fun add(fragment: OrderFragment): OrderNode =
        fragment.toNode().also {
            remove(fragment)
            remove(it)
            orderNodes.add(it)
        }

    /**
     * Add an [Op] to the [userDefinedOps].
     */
    fun add(op: Op): OrderNode {
        val terminalOrderNode = TerminalOrderNode(
            baseName = op.ownerClassFqn,
            opName = if (op.contentHashCode != null) {
                op.ownerClassMethodFqn + "$" + op.contentHashCode
            } else {
                op.ownerClassMethodFqn
            }
        )
        this@OrderBuilder.userDefinedOps[terminalOrderNode.opName] = op
        orderNodes.add(terminalOrderNode)
        return terminalOrderNode
    }

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
