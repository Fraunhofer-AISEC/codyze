package de.fraunhofer.aisec.codyze.legacy.analysis.markevaluation

import de.fraunhofer.aisec.cpg.ExperimentalGraph
import de.fraunhofer.aisec.cpg.graph.Graph
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.declarations.*

/**
 * Returns all functions, i.e. [de.fraunhofer.aisec.cpg.graph.declarations.FunctionDeclaration]s
 * contained in the graph.
 */
@ExperimentalGraph
val Graph.functions: List<FunctionDeclaration>
    get() {
        return all()
    }

/**
 * Returns all methods, i.e. [de.fraunhofer.aisec.cpg.graph.declarations.MethodDeclaration]s
 * contained in the graph.
 */
@ExperimentalGraph
val Graph.methods: List<MethodDeclaration>
    get() {
        return all()
    }

/**
 * Returns all variables, i.e. [de.fraunhofer.aisec.cpg.graph.declarations.VariableDeclaration]s
 * contained in the graph.
 */
@ExperimentalGraph
val Graph.variables: List<VariableDeclaration>
    get() {
        return all()
    }

/**
 * Returns all namespaces, i.e. [de.fraunhofer.aisec.cpg.graph.declarations.NamespaceDeclaration]s
 * contained in the graph.
 */
@ExperimentalGraph
val Graph.namespaces: List<NamespaceDeclaration>
    get() {
        return all()
    }

/**
 * Returns all records, i.e. [de.fraunhofer.aisec.cpg.graph.declarations.RecordDeclaration]s
 * contained in the graph.
 */
@ExperimentalGraph
val Graph.records: List<RecordDeclaration>
    get() {
        return all()
    }

/**
 * Returns all namespaces, i.e. [de.fraunhofer.aisec.cpg.graph.declarations.NamespaceDeclaration]s
 * with the specified substring contained in the graph.
 *
 * @param pattern the substring to look for
 */
@ExperimentalGraph
fun Graph.namespaces(pattern: String): List<NamespaceDeclaration> {
    return all(pattern)
}

/** Returns all nodes with the type [T]. */
@ExperimentalGraph
inline fun <reified T : Node> Graph.all(): List<T> {
    return this.nodes.filterIsInstance<T>()
}

/** Returns all nodes with the type [T] and the name pattern. */
@ExperimentalGraph
inline fun <reified T : Node> Graph.all(pattern: String): List<T> {
    return all<T>().filter { it.name.contains(pattern) }
}
