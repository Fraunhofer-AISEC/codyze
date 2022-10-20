package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_extensions

import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.EvaluationContext

/** A simulated [Project] extension. */
class KotlinExtension {
    var isAwesome = true
}

/** Simulates a generated accessor to configure a [Project] extension. */
fun EvaluationContext.kotlin(configure: KotlinExtension.() -> Unit) = Unit
