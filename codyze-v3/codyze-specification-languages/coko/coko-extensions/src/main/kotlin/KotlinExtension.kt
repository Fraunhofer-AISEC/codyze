package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_extensions

import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.Project

/** A simulated [Project] extension. */
class KotlinExtension {
    var isAwesome = true
}

/** Simulates a generated accessor to configure a [Project] extension. */
fun Project.kotlin(configure: KotlinExtension.() -> Unit) = Unit
