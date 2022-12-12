/*
 * Copyright (c) 2022, Fraunhofer AISEC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fraunhofer.aisec.codyze.specificationLanguages.coko.dsl

import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.CokoBackend
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.Import
import mu.KotlinLogging
import java.io.File
import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.FileBasedScriptSource
import kotlin.script.experimental.host.FileScriptSource
import kotlin.script.experimental.jvm.*
import kotlin.script.experimental.jvm.util.scriptCompilationClasspathFromContext

private val logger = KotlinLogging.logger {}

@Suppress("UNUSED") // this is copied to the script base class TODO: this is currently not working somehow
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
    // UNTESTED: we expect this to improve performance. Does not seem to break anything
    evaluationConfiguration = ProjectScriptEvaluationConfiguration::class
)
// the class is used as the script base class, therefore it should be open or abstract
open class CokoScript {
    // Configures the plugins used by the project.
    @Suppress("UnusedPrivateMember")
    fun plugins(configure: PluginDependenciesSpec.() -> Unit) = Unit
}

interface PluginDependenciesSpec {
    // Applies a plugin by id.
    fun id(id: String)
}

internal object ProjectScriptCompilationConfiguration :
    ScriptCompilationConfiguration({
        baseClass(CokoScript::class)
        jvm {
            dependenciesFromClassContext( // extract dependencies from the host environment
                CokoScript::class, // use this class classloader for dependencies search
                *baseLibraries, // search these libraries in it and use then as a script compilation classpath
                wholeClasspath = false // manually add all needed dependencies using the baseLibraries
            )
        }

        // the actual receiver must be passed to the script class in the constructor
        implicitReceivers(
            CokoBackend::class
        )

        /**
         * - Enable the experimental context receivers feature
         * - Make sure to set the jvm-target of the script compiler to the same jvm-target the rest
         * of Codyze is using
         */
        compilerOptions("-Xcontext-receivers", "-jvm-target=11", "-Xskip-prerelease-check")

        // adds implicit import statements (in this case `import
        // de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.dsl.*`, etc.)
        // to each script on compilation
        defaultImports.append(
            "de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.*"
        )

        // section that defines callbacks during compilation
        refineConfiguration {
            beforeCompiling { context ->
                val pluginsBlock = pluginsBlockOrNullFrom(context.script.text)
                if (pluginsBlock != null) {

                    val augmentedClasspath = resolveClasspathAndGenerateExtensionsFor(pluginsBlock)

                    context.compilationConfiguration
                        .with {
                            defaultImports.append(
                                "de.fraunhofer.aisec.codyze.backends.cpg.coko.dsl.*"
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

private val baseLibraries =
    arrayOf("coko-core", "codyze-core", "coko-dsl", "kotlin-stdlib", "kotlin-reflect")

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
    logger.debug { "Generating extensions for $pluginsBlock" }

    val baseLibrariesPlusExtensions = arrayOf("cpg") + baseLibraries

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
        // If no action is performed, the original configuration should be returned
        context.collectedData?.get(ScriptCollectedData.foundAnnotations)?.takeIf { it.isNotEmpty() }
            ?: return context.compilationConfiguration.asSuccess()

    val scriptBaseDir = (context.script as? FileBasedScriptSource)?.file?.parentFile
    val importedSources =
        annotations.flatMap {
            (it as? Import)?.paths?.map { sourceName ->
                FileScriptSource(scriptBaseDir?.resolve(sourceName) ?: File(sourceName))
            }.orEmpty()
        }

    return ScriptCompilationConfiguration(context.compilationConfiguration) {
        if (importedSources.isNotEmpty()) importScripts.append(importedSources)
    }
        .asSuccess()
}

object ProjectScriptEvaluationConfiguration :
    ScriptEvaluationConfiguration({
        // if a script is imported multiple times in the import hierarchy, use a single copy
        scriptsInstancesSharing(true)
    })
