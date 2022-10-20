package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core

@DslMarker
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.CLASS,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.TYPEALIAS,
    AnnotationTarget.TYPE
)
annotation class CokoMarker

@Suppress("UNUSED")
@CokoMarker
/*
 * Receives a [cpg] translation result to identify matching nodes and evaluate the expressions.
 * All the functionality of the DSL are implemented as extension functions on [Coko].
 */
class EvaluationContext(val backend: CokoBackendManager)
