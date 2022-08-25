package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_dsl

import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.Import
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.Project
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_dsl.host.cpgEvaluator
import java.io.File
import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.FileBasedScriptSource
import kotlin.script.experimental.host.FileScriptSource
import kotlin.script.experimental.jvm.dependenciesFromClassContext
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvm.updateClasspath
import kotlin.script.experimental.jvm.util.scriptCompilationClasspathFromContext

// The KotlinScript annotation marks a class that can serve as a reference to the script definition
// for
// `createJvmCompilationConfigurationFromTemplate` call as well as for the discovery mechanism
// The marked class also become the base class for defined script type (unless redefined in the
// configuration)
@KotlinScript(
    displayName = "Codyze Specification Script",
    // file name extension by which this script type is recognized by mechanisms built into
    // scripting compiler plugin
    // and IDE support, it is recommended to use double extension with the last one being "kts", so
    // some non-specific
    // scripting support could be used, e.g. in IDE, if the specific support is not installed.
    fileExtension = "codyze.kts",
    // the class or object that defines script compilation configuration for this type of scripts
    compilationConfiguration = ProjectScriptCompilationConfiguration::class,
    // evaluationConfiguration = ProjectScriptEvaluationConfiguration::class  // using this caused
    // the resulting ResultWithDiagnostics<EvaluationResult> to be a failure instead of a success
    )
// the class is used as the script base class, therefore it should be open or abstract
abstract class CokoScript(project: Project) : Project by project {
    // the interface delegation is used to implement the extension functionality

    // Configures the plugins used by the project.
    fun plugins(configure: PluginDependenciesSpec.() -> Unit) = Unit
}

interface PluginDependenciesSpec {

    // Applies a plugin by id.
    fun id(id: String)
}

internal object ProjectScriptCompilationConfiguration :
    ScriptCompilationConfiguration({
        jvm { dependenciesFromClassContext(CokoScript::class, *baseLibraries) }

        implicitReceivers(
            cpgEvaluator::class
        ) // the actual receiver must be passed to the script class in the constructor

        // adds implicit import statements (in this case `import
        // de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.*`, etc.)
        // to each script on compilation
        defaultImports.append("de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.*")

        // section that defines callbacks during compilation
        refineConfiguration {
            beforeCompiling { context ->
                val pluginsBlock = pluginsBlockOrNullFrom(context.script.text)
                if (pluginsBlock != null) {

                    val augmentedClasspath = resolveClasspathAndGenerateExtensionsFor(pluginsBlock)

                    context.compilationConfiguration
                        .with {
                            defaultImports.append(
                                "de.fraunhofer.aisec.codyze.specification_languages.coko.coko_extensions.*"
                            )
                            updateClasspath(augmentedClasspath)
                        }
                        .asSuccess()
                } else {
                    context.compilationConfiguration.asSuccess()
                }
            }

            // the callback called than any of the listed file-level annotations are encountered in
            // the compiled script
            // the processing is defined by the `handler`, that may return refined configuration
            // depending on the annotations
            onAnnotations(Import::class, handler = ::configureImportDepsOnAnnotations)
        }

        ide { acceptedLocations(ScriptAcceptedLocation.Everywhere) }
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

// The handler that is called during script compilation in order to reconfigure compilation on the
// fly
fun configureImportDepsOnAnnotations(
    context: ScriptConfigurationRefinementContext
): ResultWithDiagnostics<ScriptCompilationConfiguration> {
    val annotations =
        context.collectedData?.get(ScriptCollectedData.foundAnnotations)?.takeIf { it.isNotEmpty() }
            ?: return context.compilationConfiguration
                .asSuccess() // If no action is performed, the original configuration should be
    // returned

    val scriptBaseDir = (context.script as? FileBasedScriptSource)?.file?.parentFile
    val importedSources =
        annotations.flatMap {
            (it as? Import)?.paths?.map { sourceName ->
                FileScriptSource(scriptBaseDir?.resolve(sourceName) ?: File(sourceName))
            }
                ?: emptyList()
        }

    return ScriptCompilationConfiguration(context.compilationConfiguration) {
            if (importedSources.isNotEmpty()) importScripts.append(importedSources)
        }
        .asSuccess()
}

// object ProjectScriptEvaluationConfiguration :
//    ScriptEvaluationConfiguration({
//        // script instance sharing causes problems somehow...
//        scriptsInstancesSharing(
//            true
//        ) // if a script is imported multiple times in the import hierarchy, use a single copy
//    })
