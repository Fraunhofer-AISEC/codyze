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