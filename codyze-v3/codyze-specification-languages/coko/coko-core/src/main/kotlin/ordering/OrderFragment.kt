@file:Suppress("UNUSED")

package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.dsl.ordering

import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.CokoMarker
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.ordering.OrderQuantifier
import kotlin.reflect.KFunction

typealias OrderToken = KFunction<*>

@CokoMarker
sealed interface OrderFragment {
    fun toNode(): OrderFragment

    val token: OrderFragment
        get() = this
}

/**
 * Represents a regex sequence, where one [OrderFragment] must be followed by another
 * [OrderFragment]
 */
data class SequenceNode(val left: OrderFragment, val right: OrderFragment) : OrderFragment {
    override fun toNode(): OrderFragment = this
}

/** Represents a regex OR ('|') */
data class AlternativeNode(val left: OrderFragment, val right: OrderFragment) : OrderFragment {
    override fun toNode(): OrderFragment = this
}

/** Represents a regex quantifier like: '*', '?', etc. */
data class QuantifierNode(
    val child: OrderFragment,
    val type: OrderQuantifier,
    val value: Any? = null
) : OrderFragment {
    init {
        if (
            type in listOf(OrderQuantifier.ATLEAST, OrderQuantifier.BETWEEN, OrderQuantifier.COUNT)
        ) {
            checkNotNull(value) { "You must provide a value for this kind of quantifier." }
        }
    }
    override fun toNode(): OrderFragment = this
}

/** Represents an [OrderToken] */
data class TerminalNode(val base: String, val op: String, val block: (() -> Unit)? = null) :
    OrderFragment {
    override fun toNode(): OrderFragment = this
}
