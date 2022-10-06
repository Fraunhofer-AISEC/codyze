@file:Suppress("UNUSED")

package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.dsl

import de.fraunhofer.aisec.cpg.graph.Node

/** Matches any value. */
object Wildcard

typealias wildcard = Wildcard

typealias Nodes = Collection<Node>

/** Stores the fully qualified name of a class */
data class Type(val fqn: String)
