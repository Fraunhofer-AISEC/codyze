package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.ordering

import kotlin.jvm.internal.CallableReference

/**
 * Represents an [OrderToken].
 */
data class TerminalOrderNode(val baseName: String, val opName: String) : OrderNode

fun OrderToken.toTerminalOrderNode(baseName: String = (this as CallableReference).owner.toString(), opName: String = name) =
    TerminalOrderNode(baseName, opName)

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
