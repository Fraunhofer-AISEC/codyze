package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.ordering

import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.CokoMarker

@CokoMarker
sealed interface OrderNode : OrderFragment {
    /** Convert this [OrderNode] to a binary syntax tree */
    override fun toNode() = this

    /** Constructs a NFA of this [OrderNode] */
    fun toNfa(): NFA
}
