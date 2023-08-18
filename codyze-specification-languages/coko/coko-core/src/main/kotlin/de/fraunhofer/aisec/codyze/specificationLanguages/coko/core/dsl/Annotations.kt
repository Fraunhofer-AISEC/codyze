/*
 * Copyright (c) 2022, Fraunhofer AISEC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:Suppress("UNUSED")

/*
See: https://github.com/Kotlin/kotlin-script-examples/blob/master/jvm/simple-main-kts/simple-main-kts/src/main/kotlin/org/jetbrains/kotlin/script/examples/simpleMainKts/annotations.kt
 */

package de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl

/** Import other coko script(s) */
@Target(AnnotationTarget.FILE)
@Repeatable
@Retention(AnnotationRetention.SOURCE)
annotation class Import(vararg val paths: String)

/** Marks a function that should be evaluated as a rule by Codyze */
@Target(AnnotationTarget.FUNCTION)
@MustBeDocumented
@Suppress("LongParameterList")
annotation class Rule(
    val description: String = "",
    val shortDescription: String = "",
    val severity: Severity = Severity.WARNING, // converted to either problem.severity or security-severity
    val passMessage: String = "", // if empty string, use the default message of the evaluator
    val failMessage: String = "", // if empty string, use the default message of the evaluator
    val help: String = "",
    val tags: Array<String> = [], // tags to filter rules e.g., security, language-features etc.
    val precision: Precision = Precision.UNKNOWN,
)

annotation class RuleSet()

enum class Severity {
    INFO,
    WARNING,
    ERROR
}

enum class Precision {
    VERY_HIGH,
    HIGH,
    MEDIUM,
    LOW,
    UNKNOWN
}
