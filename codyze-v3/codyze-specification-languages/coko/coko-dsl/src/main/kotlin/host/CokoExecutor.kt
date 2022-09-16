package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_dsl.host

import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.Project
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_dsl.CokoScript
import de.fraunhofer.aisec.codyze_core.Executor
import de.fraunhofer.aisec.codyze_core.config.ExecutorConfiguration
import de.fraunhofer.aisec.cpg.TranslationManager
import io.github.detekt.sarif4k.Result
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.baseClassLoader
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate
import kotlin.script.experimental.jvmhost.createJvmEvaluationConfigurationFromTemplate
import kotlin.script.experimental.util.PropertiesCollection
import kotlin.time.*
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class CokoExecutor : Executor {
    override val name: String
        get() = CokoExecutor::class.simpleName ?: "CokoExecutor"
    override val supportedFileExtension: String
        get() = "codyze.kts"

    lateinit var configuration: ExecutorConfiguration
    var sharedClassLoader: ClassLoader? = null

    override fun initialize(configuration: ExecutorConfiguration) {
        this.configuration = configuration
    }

    /**
     * Compiles all the specification files, translates the CPG and finally triggers the evaluation
     * of the specs.
     */
    @OptIn(ExperimentalTime::class)
    override fun evaluate(analyzer: TranslationManager): List<Result> {
        logger.info { "Constructing the CPG..." }
        val cpg = analyzer.analyze().get()

        val specEvaluator = SpecEvaluator()
        val project = Project(cpg)

        logger.info { "Compiling specification scripts..." }
        // compile the spec scripts
        val specCompilationDuration: Duration = measureTime {
            for (specFile in configuration.spec) {

                // compile the script
                val result =
                    eval(
                        specFile.toScriptSource(),
                        project = project,
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
        }
        logger.debug {
            "Compiled specification scripts in ${specCompilationDuration.toString(unit = DurationUnit.SECONDS, decimals = 2)}"
        }

        logger.info {
            "Evaluating ${specEvaluator.rules.size} ${if (specEvaluator.rules.size == 1) "rule" else "rules"}..."
        }
        // evaluate the spec scripts
        val (findings: Unit, scriptEvaluationDuration: Duration) =
            measureTimedValue { specEvaluator.evaluate() }
        logger.debug {
            "Evaluated specification scripts in ${scriptEvaluationDuration.toString(unit = DurationUnit.SECONDS, decimals = 2)}"
        }
        return listOf()
    }

    companion object {
        /**
         * Evaluates the given project script [sourceCode] against the given [project] and
         * [evaluator].
         */
        fun eval(sourceCode: String, project: Project) = eval(sourceCode.toScriptSource(), project)

        /** Evaluates the given project script [sourceCode] against the given [project]. */
        fun eval(
            sourceCode: SourceCode,
            project: Project,
            sharedClassLoader: ClassLoader? = null
        ): ResultWithDiagnostics<EvaluationResult> {
            val compilationConfiguration =
                createJvmCompilationConfigurationFromTemplate<CokoScript>()
            val evaluationConfiguration =
                createJvmEvaluationConfigurationFromTemplate<CokoScript> {
                    // constructorArgs(...)
                    implicitReceivers(project)
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
    }
}