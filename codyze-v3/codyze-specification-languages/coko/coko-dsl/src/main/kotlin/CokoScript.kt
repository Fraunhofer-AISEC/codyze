package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_dsl

import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.Project
import java.io.File
import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.*
import kotlin.script.experimental.jvm.dependenciesFromClassContext
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvm.updateClasspath
import kotlin.script.experimental.jvm.util.scriptCompilationClasspathFromContext

@KotlinScript(
    displayName = "Codyze Specification Script",
    fileExtension = "codyze.kts",
    compilationConfiguration = ProjectScriptCompilationConfiguration::class,
)
abstract class CokoScript(project: Project) : Project by project {

    /** Configures the plugins used by the project. */
    fun plugins(configure: PluginDependenciesSpec.() -> Unit) = Unit
}

interface PluginDependenciesSpec {

    /** Applies a plugin by id. */
    fun id(id: String)
}

internal object ProjectScriptCompilationConfiguration :
    ScriptCompilationConfiguration({
        baseClass(CokoScript::class)

        jvm { dependenciesFromClassContext(CokoScript::class, *baseLibraries) }

        refineConfiguration {
            beforeCompiling { context ->
                val pluginsBlock = pluginsBlockOrNullFrom(context.script.text)
                if (pluginsBlock != null) {

                    val augmentedClasspath = resolveClasspathAndGenerateExtensionsFor(pluginsBlock)

                    context.compilationConfiguration
                        .with {
                            defaultImports(
                                "de.fraunhofer.aisec.codyze.specification_languages.coko.coko_extensions.*"
                            )
                            updateClasspath(augmentedClasspath)
                        }
                        .asSuccess()
                } else {
                    context.compilationConfiguration.asSuccess()
                }
            }
        }

        ide { acceptedLocations(ScriptAcceptedLocation.Project) }
    })

private val baseLibraries = arrayOf("coko-core", "coko-dsl", "kotlin-stdlib", "kotlin-reflect")

private fun pluginsBlockOrNullFrom(scriptText: String) =
    scriptText.run {
        when (val startIndex = indexOf("plugins {")) {
            -1 -> null
            else ->
                when (val endIndex = indexOf("}", startIndex)) {
                    -1 -> null
                    else -> substring(startIndex, endIndex + 1)
                }
        }
    }

private fun resolveClasspathAndGenerateExtensionsFor(pluginsBlock: String?): List<File> {
    println("Generating extensions for $pluginsBlock")

    val baseLibrariesPlusExtensions = arrayOf("coko-extensions", *baseLibraries)

    return scriptCompilationClasspathFromContext(
        *baseLibrariesPlusExtensions,
        classLoader = CokoScript::class.java.classLoader
    )
}
