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
package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.ordering

import kotlin.jvm.internal.CallableReference

/** Represents an [OrderToken]. */
data class TerminalOrderNode(val baseName: String, val opName: String) : OrderNode

fun OrderToken.toTerminalOrderNode(
    baseName: String = (this as CallableReference).owner.toString(),
    opName: String = name
) = TerminalOrderNode(baseName, opName)

/** Represents a regex sequence, where one [OrderNode] must be followed by another [OrderNode] */
data class SequenceOrderNode(val left: OrderNode, val right: OrderNode) : OrderNode

/** Represents a regex OR ('|') */
data class AlternativeOrderNode(val left: OrderNode, val right: OrderNode) : OrderNode

/** Represents a regex quantifier like: '*', '?', etc. */
data class QuantifierOrderNode(
    val child: OrderNode,
    val type: OrderQuantifier,
    val value: Any? = null
) : OrderNode {
    init {
        if (
            type in listOf(OrderQuantifier.ATLEAST, OrderQuantifier.BETWEEN, OrderQuantifier.COUNT)
        ) {
            checkNotNull(value) { "You must provide a value for this kind of quantifier." }
        }
    }
}

/** All the available quantifiers for this simple regex like DSL. */
enum class OrderQuantifier {
    COUNT,
    BETWEEN,
    ATLEAST,
    MAYBE,
    OPTION
}
