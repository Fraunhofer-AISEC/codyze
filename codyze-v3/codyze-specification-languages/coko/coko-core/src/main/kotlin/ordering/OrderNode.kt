package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.ordering

import de.fraunhofer.aisec.cpg.analysis.fsm.NFA

sealed interface OrderNode : OrderFragment {
    /** Convert this [OrderNode] to a binary syntax tree */
    override fun toNode() = this

    /** Constructs a NFA of this [OrderNode] */
    fun toNfa(): NFA
}
