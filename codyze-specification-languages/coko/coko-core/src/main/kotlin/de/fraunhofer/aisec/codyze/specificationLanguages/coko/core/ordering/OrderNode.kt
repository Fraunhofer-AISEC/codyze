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
package de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.ordering

sealed interface OrderNode : OrderFragment {
    /** Convert this [OrderNode] to a binary syntax tree */
    override fun toNode() = this

    /**
     * Apply the given [block] to each [OrderNode] in this [OrderNode]. Uses Depth First Search
     * (DFS).
     */
    fun applyToAll(block: OrderNode.() -> Unit) {
        val stack = ArrayDeque<OrderNode>()
        stack.addLast(this)
        while (stack.isNotEmpty()) {
            val node = stack.removeLast()
            when (node) {
                is TerminalOrderNode -> {}
                is SequenceOrderNode -> {
                    stack.addLast(node.left)
                    stack.addLast(node.right)
                }
                is AlternativeOrderNode -> {
                    stack.addLast(node.left)
                    stack.addLast(node.right)
                }
                is QuantifierOrderNode -> stack.addLast(node.child)
            }
            node.apply(block)
        }
    }
}
