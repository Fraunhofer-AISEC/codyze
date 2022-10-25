package de.fraunhofer.aisec.codyze_core.wrapper

interface Backend {
    var graph: Any

    fun initialize()
}
