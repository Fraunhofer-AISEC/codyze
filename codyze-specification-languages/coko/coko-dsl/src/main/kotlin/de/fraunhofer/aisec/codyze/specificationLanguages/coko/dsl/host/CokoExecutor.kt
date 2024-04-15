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
package de.fraunhofer.aisec.codyze.specificationLanguages.coko.dsl.host

import de.fraunhofer.aisec.codyze.core.executor.Executor
import de.fraunhofer.aisec.codyze.core.timed
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.CokoBackend
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.Finding
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.Import
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.dsl.CokoConfiguration
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.dsl.CokoSarifBuilder
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.dsl.CokoScript
import io.github.detekt.sarif4k.Run
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.script.experimental.api.*
import kotlin.script.experimental.api.SourceCode
import kotlin.script.experimental.host.FileBasedScriptSource
import kotlin.script.experimental.host.FileScriptSource
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.baseClassLoader
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate
import kotlin.script.experimental.jvmhost.createJvmEvaluationConfigurationFromTemplate
import kotlin.script.experimental.util.PropertiesCollection

private val logger = KotlinLogging.logger {}

/**
 * The [Executor] to evaluate Coko (codyze.kts) specification files.
 */
class CokoExecutor(private val configuration: CokoConfiguration, private val backend: CokoBackend) :
    Executor {

    /**
     * Compiles all the specification files, translates the CPG and finally triggers the evaluation
     * of the rules defined in the specs.
     */
    @Suppress("UnusedPrivateMember")
    override fun evaluate(): Run {
        val specEvaluator =
            timed({ logger.info { "Compiled specification scripts in $it." } }) {
                compileScriptsIntoSpecEvaluator(backend = backend, specFiles = configuration.spec)
            }

        logger.info {
            "Evaluating ${specEvaluator.rules.size} ${if (specEvaluator.rules.size == 1) "rule" else "rules"}..."
        }
        // evaluate the spec scripts
        val findings = timed({ logger.info { "Evaluation of specification scripts took $it." } }) {
            specEvaluator.evaluate()
        }.toMutableMap()

        // filter the positive findings if the user disabled goodFindings
        if (!configuration.goodFindings) {
            for ((rule, ruleFindings) in findings) {
                findings[rule] = ruleFindings.filter { it.kind != Finding.Kind.Pass }
            }
        }

        val cokoSarifBuilder = CokoSarifBuilder(rules = specEvaluator.rules, backend = backend)
        return cokoSarifBuilder.buildRun(findings = findings)
    }

    companion object {
        /** Contains the class loaders of all evaluated scripts */
        private val classLoaders = mutableSetOf<ClassLoader>()

        private val conceptTranslator = ConceptTranslator()

        /** Evaluates the given project script [sourceCode] against the given [backend]. */
        fun eval(
            sourceCode: String,
            backend: CokoBackend,
            sharedClassLoader: ClassLoader? = null
        ) =
            eval(sourceCode.toScriptSource(), backend, sharedClassLoader)

        /** Evaluates the given project script [sourceCode] against the given [backend]. */
        fun eval(
            sourceCode: SourceCode,
            backend: CokoBackend,
            baseClassLoader: ClassLoader? = null
        ): ResultWithDiagnostics<EvaluationResult> {
            val compilationConfiguration =
                createJvmCompilationConfigurationFromTemplate<CokoScript> {
                    refineConfiguration {
                        // the callback called if any of the listed file-level annotations are encountered in
                        // the compiled script
                        // the processing is defined by the `handler`, that may return refined configuration
                        // depending on the annotations
                        onAnnotations(Import::class, handler = ::configureImportDepsOnAnnotations)
                    }
                }
            val evaluationConfiguration =
                createJvmEvaluationConfigurationFromTemplate<CokoScript> {
                    implicitReceivers(backend)
                    jvm {
                        if (baseClassLoader != null) {
                            // The BasicJvmScriptingHost will use this `baseClassLoader` as an ancestor
                            // for the actual class loader that loads the compiled class
                            this.baseClassLoader.put(baseClassLoader)
                        }
                    }
                }

            return BasicJvmScriptingHost()
                .eval(
                    sourceCode,
                    compilationConfiguration,
                    evaluationConfiguration,
                )
        }

        /**
         * Compiles the given specification files and analyzes the script contents by adding all the
         * extracted information into a [SpecEvaluator].
         *
         * @return A [SpecEvaluator] object containing the extracted information from all
         * [specFiles]
         */
        @Suppress("complexity.CyclomaticComplexMethod")
        fun compileScriptsIntoSpecEvaluator(
            backend: CokoBackend,
            specFiles: List<Path>
        ): SpecEvaluator {
            var baseClassLoader: ClassLoader? = null
            val specEvaluator = SpecEvaluator()
            for (specFile in specFiles) {
                val scriptSource = if (specFile.extension == "concepts") {
                    conceptTranslator.transformConceptFile(specFile)
                } else {
                    FileScriptSource(specFile.toFile())
                }

                // compile the script
                val result =
                    eval(
                        scriptSource,
                        backend = backend,
                        baseClassLoader = baseClassLoader
                    )

                // log script diagnostics
                result.reports.forEach {
                    val message =
                        " : ${specFile.fileName}: ${it.message}" +
                            if (it.exception == null) "" else ": ${it.exception}"
                    when (it.severity) {
                        ScriptDiagnostic.Severity.DEBUG -> logger.debug { message }
                        ScriptDiagnostic.Severity.INFO -> logger.info { message }
                        ScriptDiagnostic.Severity.WARNING -> logger.warn { message }
                        ScriptDiagnostic.Severity.ERROR -> logger.error { message }
                        ScriptDiagnostic.Severity.FATAL -> logger.error { message }
                    }
                }

                // throw an exception if the script could not be compiled
                val scriptEvaluationResult = result.valueOrThrow()

                // Get the class loader for the loaded script.
                val newClassLoader = scriptEvaluationResult.configuration?.get(
                    PropertiesCollection.Key<ClassLoader>("actualClassLoader")
                )

                // The ScriptingHost makes a class loader for each script.
                // We need to connect these class loaders of the scripts to be able to find all classes.
                // This is done by creating a chaining them through the `parent` property of the ClassLoader.
                // The BasicJVMScriptingHost will assign the given `baseClassLoader` as an ancestor for
                // the new class loader for the compiled script.

                // Since a script might import other scripts, some classes are already included in a class
                // loader when they are first compiled.
                // Since their assigned class loader will be the class loader that includes them, the chain
                // might be broken. All previous class loaders are therefore stored in the `classLoaders` set.
                // If `newClassLoader` is not yet in `classLoaders` it is used as the `baseClassLoader` for the
                // next script to continue the chain. Otherwise, the previous `baseClassLoader` is used again,
                // since it is still the last class loader of the chain.
                if (newClassLoader != null && classLoaders.add(newClassLoader)) {
                    baseClassLoader = newClassLoader
                }

                // analyze script contents
                @Suppress("UnsafeCallOnNullableType")
                scriptEvaluationResult.returnValue.let {
                    if (it.scriptInstance != null && it.scriptClass != null) {
                        specEvaluator.addSpec(it.scriptClass!!, it.scriptInstance!!)
                    }
                }
            }
            return specEvaluator
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
//            (it as? Import)?.paths?.map { sourceName ->
//                FileScriptSource(scriptBaseDir?.resolve(sourceName) ?: File(sourceName))
//            }.orEmpty()

                    (it as? Import)?.paths?.mapNotNull { sourceName ->
                        val file = scriptBaseDir?.resolve(sourceName) ?: File(sourceName).normalize()
                        if (sourceName.endsWith(".codyze.kts")) {
                            FileScriptSource(file)
                        } else if (sourceName.endsWith(".concepts")) {
                            conceptTranslator.transformConceptFile(file.toPath())
                        } else {
                            null
                        }
                    }.orEmpty()
                }

            return ScriptCompilationConfiguration(context.compilationConfiguration) {
                if (importedSources.isNotEmpty()) importScripts.append(importedSources)
            }
                .asSuccess()
        }
    }
}
