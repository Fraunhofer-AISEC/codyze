package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core

import kotlin.reflect.KCallable

typealias Action<T> = T.() -> Unit

interface Concept

interface RuleConcept

annotation class rule

infix fun <A, B> A.follows(that: B): Pair<A, B> = Pair(this, that)

fun <T> occurance(func: KCallable<T>, vararg arguments: Any) = Unit

fun call(full_name: String) = Unit

fun any(): String = ""

fun variable(name: String): String = ""
