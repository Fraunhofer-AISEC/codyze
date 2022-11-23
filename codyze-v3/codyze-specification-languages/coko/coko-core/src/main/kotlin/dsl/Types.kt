@file:Suppress("UNUSED")

package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.dsl

/** Matches any value. */
object Wildcard

typealias wildcard = Wildcard

/** Stores the fully qualified name of a class */
data class Type(val fqn: String)
