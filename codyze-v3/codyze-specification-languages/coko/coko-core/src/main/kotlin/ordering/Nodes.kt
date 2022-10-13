package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.ordering

import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.Nodes
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.dsl.Op
import kotlin.reflect.KFunction

/**
 * Represents an [OrderToken].
 *
 * @param correspondingNodes: A lambda that returns the cpg [Nodes], which correspond to the user
 * specified in coko using e.g., an [Op].
 */
data class TerminalOrderNode(
    val opReference: KFunction<*>,
    val correspondingNodes: (() -> Nodes) = { listOf() }
) : OrderNode {
    /** Constructs a NFA using Thompson's construction algorithm */
    override fun toNfa() = nfaForTerminalOrderNode(this)
    override fun equals(other: Any?) =
        if (other is TerminalOrderNode)
            opReference == other.opReference
        else false
}

/** Represents a regex sequence, where one [OrderNode] must be followed by another [OrderNode] */
data class SequenceOrderNode(val left: OrderNode, val right: OrderNode) : OrderNode {
    /** Constructs a NFA using Thompson's construction algorithm */
    override fun toNfa() = concatenateMultipleNfa(left.toNfa(), right.toNfa())
}

/** Represents a regex OR ('|') */
data class AlternativeOrderNode(val left: OrderNode, val right: OrderNode) : OrderNode {
    /** Constructs a NFA using Thompson's construction algorithm */
    override fun toNfa() = alternateTwoNfa(left.toNfa(), right.toNfa())
}

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

    /** Constructs a NFA using Thompson's construction algorithm */
    override fun toNfa() = nfaForQuantifierOrderNode(this)
}

/** All the available quantifiers for this simple regex like DSL. */
enum class OrderQuantifier {
    COUNT,
    BETWEEN,
    ATLEAST,
    MAYBE,
    OPTION
}
