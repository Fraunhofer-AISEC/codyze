@file:Suppress("UNUSED")
/*
See: https://github.com/Kotlin/kotlin-script-examples/blob/master/jvm/simple-main-kts/simple-main-kts/src/main/kotlin/org/jetbrains/kotlin/script/examples/simpleMainKts/annotations.kt
 */

package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.dsl

/** Import other script(s) */
@Target(AnnotationTarget.FILE)
@Repeatable
@Retention(AnnotationRetention.SOURCE)
annotation class Import(vararg val paths: String)

@Target(AnnotationTarget.FUNCTION)
@MustBeDocumented
annotation class Rule(val description: String = "")