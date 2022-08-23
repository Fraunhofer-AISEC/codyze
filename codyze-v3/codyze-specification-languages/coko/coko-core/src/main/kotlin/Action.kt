package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core

import kotlin.reflect.KCallable

typealias Action<T> = T.() -> Unit

interface Concept

interface RuleConcept

object Called

infix fun <A, B> A.follows(that: B): Pair<A, B> = Pair(this, that)

infix fun <A, B> A.`is`(that: B): Pair<A, B> = Pair(this, that)

class cpgEvaluator {
    fun any(): String = ""

    fun variable(name: String): String = ""

    fun <T> call(func: KCallable<T>, vararg arguments: Any) = Unit

    fun call(full_name: String) = Unit
}
