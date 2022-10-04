@file:Suppress("UNUSED")

package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.ordering

import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.CokoMarker
import kotlin.reflect.KFunction

typealias OrderToken = KFunction<*>

@CokoMarker
sealed interface OrderFragment {
    /**
     * Convert this [OrderFragment] to a binary syntax tree
     */
    fun toNode(): OrderNode

    val token: OrderFragment
        get() = this
}