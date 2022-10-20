package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.ordering

sealed interface OrderNode : OrderFragment {
    /** Convert this [OrderNode] to a binary syntax tree */
    override fun toNode() = this
}
