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
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.GroupingOp
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.dsl.CokoConfiguration
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.dsl.CokoSarifBuilder
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.dsl.CokoScript
import io.github.detekt.sarif4k.Run
import io.github.oshai.kotlinlogging.KotlinLogging
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.script.experimental.api.*
import kotlin.script.experimental.api.SourceCode
import kotlin.script.experimental.host.FileScriptSource
import kotlin.script.experimental.host.StringScriptSource
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
                createJvmCompilationConfigurationFromTemplate<CokoScript>()
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
        fun compileScriptsIntoSpecEvaluator(
            backend: CokoBackend,
            specFiles: List<Path>
        ): SpecEvaluator {
            var baseClassLoader: ClassLoader? = null
            val specEvaluator = SpecEvaluator()
            for (specFile in specFiles) {
                val scriptSource = if (specFile.extension == "concepts") {
                    transformConceptFile(specFile)
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

        private fun transformConceptFile(specFile: Path): StringScriptSource {
            val fileScriptSource = FileScriptSource(specFile.toFile())

            val fileText = fileScriptSource.text
            val preliminaryScriptText = fileText
                // Remove all comments
                // TODO: Is there a way to specify that all Regexes should not include matches found in comments?
                .replace(Regex("//.*\\n"), "\n")
                // Handle all `op` keywords that represent an operationPointer (`'op' <name> '=' <name> ('|' <name>)*`)
                .replace(Regex("\\s+op\\s+.+=.+\\s")) { opPointerMatchResult ->
                    handleOpPointer(opPointerMatchResult, fileText)
                }

            val scriptText = preliminaryScriptText
                // Replace all `concept` keywords with `interface` and ensure that the name is capitalized
                .replace(Regex("concept\\s+.")) {
                    makeLastCharUpperCase(
                        it.value.replace("concept", "interface")
                    )
                }
                // Replace all `enum` keywords with `enum class` and ensure that the name is capitalized
                .replace(Regex("enum\\s+.")) {
                    makeLastCharUpperCase(
                        it.value.replace("enum", "enum class")
                    )
                }
                // Ensure that all type names are capitalized
                .replace(Regex(":\\s*[a-zA-Z]")) {
                    it.value.uppercase()
                }
                // Replace all `op` keywords that represent an operation with `fun` and add types for the parameters
                .replace(Regex("\\s+op\\s.+\\)\\s?")) { opMatchResult ->
                    opMatchResult.value
                        .replace("op", "fun")
                        .replace(Regex(",( )?.*\\.\\.\\.")) {
                            it.value.dropLast(3).replace(Regex(",( )?",), ", vararg ")
                        }
                        .replace(",", ": Any?,")
                        .replace(Regex("[^(]\\)")) {
                            "${it.value.dropLast(1)}: Any?): Op"
                        }
                        .plus("\n")
                }
                .replace(Regex("\\s+var\\s+.+\\s")) { varMatchResult ->
                    val property = varMatchResult.value.replace("var", "val").dropLast(1)
                    if (property.contains(':')) {
                        "$property\n"
                    } else {
                        "$property: Any?\n"
                    }
                }

            return StringScriptSource(scriptText, fileScriptSource.name)
        }


        /**
         * This translates a match of a `op` keyword that represent an operationPointer (`'op' <name> '=' <name> ('|' <name>)*`)
         * into a Kotlin function that returns a [GroupingOp].
         *
         * Example:
         * ```
         * op log = info | warn
         * op info(msg)
         * op warn(msg)
         * ```
         * is translated into
         * ```
         * fun log(msg: Any?): GroupingOp = opGroup(info(msg), warn(msg))
         * ```
         */
        private fun handleOpPointer(opPointerMatchResult: MatchResult, fileText: String): String {
            val preliminaryResult = opPointerMatchResult.value
                // remove the whitespace character at the end of the matchResult
                .dropLast(1)
                // Replace all `op` keywords with `fun`
                .replace("op", "fun")

            // The index of the '{' character that starts the body of the concept that the "op" resides in
            val conceptBodyStart = fileText.lastIndexOf('{', opPointerMatchResult.range.first)
            // The index of the '}' character that ends the body of the concept that the "op" resides in
            val conceptBodyEnd = fileText.indexOf('}', opPointerMatchResult.range.last)

            // The body of the concept as string
            val conceptBody = fileText.subSequence(conceptBodyStart, conceptBodyEnd)

            // Split the definition of the operationPointer at the `=`
            val (firstHalf, secondHalf) = preliminaryResult.split(Regex("\\s*=\\s*"), limit = 2)
            // Split the grouped ops into separate strings
            val opNames = secondHalf.split(Regex("\\s*\\|\\s*"))

            val sb = StringBuilder(firstHalf)

            sb.append("(")
            // Find the definitions of the ops that are used for this opPointer
            val opDefinitions = opNames.map { (Regex("\\s+op\\s+$it\\(.*\\)").find(conceptBody)?.value ?: "") }
            // Append parameters needed for the function which are built by combining
            // the parameters of the grouped ops
            sb.append(buildFunctionParameters(opDefinitions))
            sb.append("): Op = opGroup(")

            // Append calls to ops
            val functionCalls = opDefinitions.map { opDefinition ->
                // remove the `op` keyword and all `...`
                opDefinition.replace(Regex("(\\s+op\\s+)|(\\.\\.\\.)"), "")
            }
            sb.append(functionCalls.joinToString())

            sb.append(")\n")
            return sb.toString()
        }

        /**
         * This combines all parameters of the ops in [opDefinitions] into a set of parameters
         * that are needed to call all functions representing the ops and combines them into a string.
         */
        private fun buildFunctionParameters(opDefinitions: List<String>): String {
            // Find out all needed parameters
            val functionParameters = opDefinitions.flatMap { opDefinition ->
                // find the parameters that are used for the op
                val opParameters = Regex("\\(.*\\)").find(opDefinition)?.value ?: ""
                // remove the `(` and `)` and then split the parameters
                removeFirstAndLastChar(opParameters).split(Regex("\\s*,\\s*"))
            }
                .filter { it.isNotEmpty() }
                .toSet()
                // add types to the parameters
                .map {
                    if (it.endsWith("...")) {
                        // translate vararg parameters to the correct Kotlin syntax
                        "vararg ${it.substring(0, it.length - 3)}: Any?"
                    } else {
                        "$it: Any?"
                    }
                }

            return functionParameters.joinToString()
        }

        private fun makeLastCharUpperCase(string: String): String {
            val lastCharAsUpper = string.last().uppercaseChar()
            return string.dropLast(1) + lastCharAsUpper
        }

        private fun removeFirstAndLastChar(string: String): String =
            if (string.isEmpty()) {
                string
            } else {
                string.substring(1, string.length - 1)
            }
    }
}
