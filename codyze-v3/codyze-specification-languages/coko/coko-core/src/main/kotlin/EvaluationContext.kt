package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core

import kotlin.reflect.KFunction
import kotlin.reflect.KParameter

data class EvaluationContext(val rule: KFunction<*>, val parameterMap: Map<KParameter, Any>)
