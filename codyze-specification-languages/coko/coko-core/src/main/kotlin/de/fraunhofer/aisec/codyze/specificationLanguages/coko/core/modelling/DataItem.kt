package de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.modelling

import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.Op

sealed interface DataItem

data class ReturnValueItem(val op: Op) : DataItem {
    override fun toString(): String = "Return value of $op"
}

data class Value(val value: Any) : DataItem

