package de.fraunhofer.aisec.codyze.analysis.structures

import de.fraunhofer.aisec.cpg.graph.Node

/**
 * Maps a Mark instances ("b") to (one of) the CPG nodes that defines it.
 *
 * key: Mark instance ("b") value: the Vertex that usages of program variables corresponding to "b"
 * REFERS_TO. Typically a VariableDeclaration node.
 */
class GraphInstanceContext {
    // e.g. for
    //    using Botan as b, Random as r
    // maps "b" to its vertex and "r" to its vertex
    private val entityAssignment: MutableMap<String, Node?> = HashMap()

    /**
     * Stores a Mark instance (e.g., "b") and the node that defines it. In special cases, the node
     * can be null, i.e. if the entity purely exists virtually and only consists of function (not
     * method calls), and thus has no object node assigned to it.
     */
    fun putMarkInstance(s: String, n: Node?) {
        entityAssignment[s] = n
    }

    fun getNode(s: String): Node? {
        return entityAssignment[s]
    }

    val markInstances: Set<String>
        get() = entityAssignment.keys
    val markInstanceVertices: Collection<Node?>
        get() = entityAssignment.values

    fun containsInstance(instance: String): Boolean {
        return entityAssignment.containsKey(instance)
    }
}
