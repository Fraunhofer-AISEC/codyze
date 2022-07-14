package de.fraunhofer.aisec.codyze.specification_languages.coko.nwt.dsl

fun nwt(initialize: NwtBuilder.() -> Unit) {
    NwtBuilder().apply(initialize)
}
