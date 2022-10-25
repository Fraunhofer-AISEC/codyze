package de.fraunhofer.aisec.codyze_backends.cpg

import de.fraunhofer.aisec.cpg.frontends.LanguageFrontend
import java.lang.reflect.Modifier

/**
 * Simple enum that maps all optional language frontends of the CPG to the FQN of the frontend in
 * the CPG package.
 */
enum class Language(private val className: String) {
    PYTHON("de.fraunhofer.aisec.cpg.frontends.python.PythonLanguageFrontend"),
    GO("de.fraunhofer.aisec.cpg.frontends.golang.GoLanguageFrontend");

    /** Component operator that returns the [Class] of this language. */
    @Suppress("UNCHECKED_CAST")
    operator fun component1(): Class<out LanguageFrontend> {
        val clazz = Class.forName(className)

        return clazz as Class<out LanguageFrontend>
    }

    /** Component operator that returns the list of supported file types by this language. */
    @Suppress("UNCHECKED_CAST")
    operator fun component2(): List<String> {
        val clazz = component1()

        // look for a static field that ends with EXTENSIONS
        val field =
            clazz.declaredFields.firstOrNull {
                it.name.endsWith("EXTENSIONS") && Modifier.isStatic(it.modifiers)
            }

        return field?.get(null) as? List<String> ?: listOf()
    }
}
