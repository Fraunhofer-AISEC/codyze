package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.ordering

/**
 * Represents an edge of the automaton. The edge label consists of an operation (typically a method
 * name) and a base which allows us to differentiate between multiple objects.
 */
data class Edge(val op: String, val base: String? = null, val nextState: State) {
    fun matches(edge: Edge): Boolean {
        return base == edge.base && op == edge.op
    }

    override fun toString(): String {
        return if (base != null) "-- $base.$op --> $nextState" else "-- $op --> $nextState"
    }

    fun toDotLabel(): String {
        return if (base != null) "$base.$op" else op
    }
}
