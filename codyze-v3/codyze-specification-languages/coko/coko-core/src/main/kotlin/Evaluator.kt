package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core

import kotlin.reflect.KFunction

interface Evaluator {
    fun evaluate(rule: KFunction<*>): EvaluationResult
}
