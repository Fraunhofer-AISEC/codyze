package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core

typealias Action<T> = T.() -> Unit

object Called

typealias called = Called

infix fun <A, B> A.follows(that: B): Pair<A, B> = Pair(this, that)

infix fun <A, B> A.`is`(that: B): Pair<A, B> = Pair(this, that)

object Wildcard

typealias wildcard = Wildcard
