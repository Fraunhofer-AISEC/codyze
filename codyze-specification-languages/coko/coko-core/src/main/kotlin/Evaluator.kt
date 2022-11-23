package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core

interface Evaluator {
    fun evaluate(context: EvaluationContext): EvaluationResult
}
