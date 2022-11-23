package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core

class EvaluationResult(val ruleEvaluationOutcome: Boolean) {
    override fun equals(other: Any?) =
        when (other) {
            is Boolean -> other == ruleEvaluationOutcome
            is EvaluationResult -> ruleEvaluationOutcome == other.ruleEvaluationOutcome
            else -> false
        }
}
