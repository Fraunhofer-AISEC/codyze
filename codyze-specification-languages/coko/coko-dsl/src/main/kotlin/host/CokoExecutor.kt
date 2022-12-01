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
package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_dsl.host

import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.CokoBackend
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_dsl.CokoScript
import de.fraunhofer.aisec.codyze.core.Executor
import de.fraunhofer.aisec.codyze.core.config.ExecutorConfiguration
import de.fraunhofer.aisec.codyze.core.timed
import de.fraunhofer.aisec.codyze.core.wrapper.BackendConfiguration
import io.github.detekt.sarif4k.Result
import mu.KotlinLogging
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf
import java.nio.file.Path
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.baseClassLoader
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate
import kotlin.script.experimental.jvmhost.createJvmEvaluationConfigurationFromTemplate
import kotlin.script.experimental.util.PropertiesCollection
import kotlin.time.*

private val logger = KotlinLogging.logger {}

class CokoExecutor : Executor, KoinComponent {
    override val name: String
        get() = CokoExecutor::class.simpleName ?: "CokoExecutor"
    override val supportedFileExtension: String
        get() = "codyze.kts"

    lateinit var backendConfiguration: BackendConfiguration
    lateinit var config: ExecutorConfiguration
    lateinit var backend: CokoBackend

    override fun initialize(
        backendConfiguration: BackendConfiguration,
        configuration: ExecutorConfiguration
    ) {
        this.backendConfiguration = backendConfiguration
        this.config = configuration

        backend = get { parametersOf(backendConfiguration) }
    }

    /**
     * Compiles all the specification files, translates the CPG and finally triggers the evaluation
     * of the specs.
     */
    @OptIn(ExperimentalTime::class)
    override fun evaluate(): List<Result> {
        val specEvaluator =
            timed("Compiled specification scripts in") {
                compileScriptsIntoSpecEvaluator(backend = backend, specFiles = config.spec)
            }

        logger.info {
            "Evaluating ${specEvaluator.rules.size} ${if (specEvaluator.rules.size == 1) "rule" else "rules"}..."
        }
        // evaluate the spec scripts
        val findings =
            timed("Evaluation of specification scripts took") { specEvaluator.evaluate() }
        return listOf() // TODO: return findings
    }

    companion object {
        /** Evaluates the given project script [sourceCode] against the given [backend]. */
        fun eval(sourceCode: String, backend: CokoBackend, sharedClassLoader: ClassLoader? = null) =
            eval(sourceCode.toScriptSource(), backend, sharedClassLoader)

        /** Evaluates the given project script [sourceCode] against the given [backend]. */
        fun eval(
            sourceCode: SourceCode,
            backend: CokoBackend,
            sharedClassLoader: ClassLoader? = null
        ): ResultWithDiagnostics<EvaluationResult> {
            val compilationConfiguration =
                createJvmCompilationConfigurationFromTemplate<CokoScript>()
            val evaluationConfiguration =
                createJvmEvaluationConfigurationFromTemplate<CokoScript> {
                    implicitReceivers(backend)
                    jvm {
                        if (sharedClassLoader != null) {
                            baseClassLoader.put(sharedClassLoader)
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
        fun compileScriptsIntoSpecEvaluator(
            backend: CokoBackend,
            specFiles: List<Path>
        ): SpecEvaluator {
            var sharedClassLoader: ClassLoader? = null
            val specEvaluator = SpecEvaluator()
            for (specFile in specFiles) {
                // compile the script
                val result =
                    eval(
                        specFile.toScriptSource(),
                        backend = backend,
                        sharedClassLoader = sharedClassLoader
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

                // Get the class loader for the first loaded script. We give that one to all the
                // other scripts to ensure they find "the same" classes.
                if (sharedClassLoader == null) {
                    sharedClassLoader =
                        scriptEvaluationResult.configuration?.get(
                            PropertiesCollection.Key<ClassLoader>("actualClassLoader")
                        )
                }

                // analyze script contents
                scriptEvaluationResult.returnValue.let {
                    if (it.scriptInstance != null && it.scriptClass != null) {
                        specEvaluator.addSpec(it.scriptClass!!, it.scriptInstance!!)
                    }
                }
            }
            return specEvaluator
        }
    }
}
