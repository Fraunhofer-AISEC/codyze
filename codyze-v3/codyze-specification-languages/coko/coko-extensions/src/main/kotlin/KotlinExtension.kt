package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_extensions

import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.CokoBackend

/** A simulated [CokoBackend] extension. */
class KotlinExtension {
    var isAwesome = true
}

/** Simulates a generated accessor to configure a [Project] extension. */
fun CokoBackend.kotlin(configure: KotlinExtension.() -> Unit) = Unit
