package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_dsl.host

import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.EvaluationContext
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.CokoBackendManager
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_dsl.CokoScript
import de.fraunhofer.aisec.codyze_core.Executor
import de.fraunhofer.aisec.codyze_core.config.ExecutorConfiguration
import de.fraunhofer.aisec.codyze_core.wrapper.BackendConfiguration
import io.github.detekt.sarif4k.Result
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
import mu.KotlinLogging
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf

private val logger = KotlinLogging.logger {}

class CokoExecutor : Executor, KoinComponent {
    override val name: String
        get() = CokoExecutor::class.simpleName ?: "CokoExecutor"
    override val supportedFileExtension: String
        get() = "codyze.kts"

    lateinit var backendConfiguration: BackendConfiguration
    lateinit var config: ExecutorConfiguration

    override fun initialize(backendConfiguration: BackendConfiguration, configuration: ExecutorConfiguration) {
        this.backendConfiguration = backendConfiguration
        this.config = configuration
    }

    private val backend: CokoBackendManager = get { parametersOf(backendConfiguration) }

    /**
     * Compiles all the specification files, translates the CPG and finally triggers the evaluation
     * of the specs.
     */
    @OptIn(ExperimentalTime::class)
    override fun evaluate(): List<Result> {
        logger.info { "Initializing the backend..." }
        val backendInitializationDuration: Duration =
            measureTime { backend.initialize() }
        logger.debug {
            "Initialized backend in ${backendInitializationDuration.toString(unit = DurationUnit.SECONDS, decimals = 2)}"
        }

        logger.info { "Compiling specification scripts..." }
        val (specEvaluator, specCompilationDuration: Duration) =
            measureTimedValue {
                compileScriptsIntoSpecEvaluator(
                    evaluationContext = EvaluationContext(backend),
                    specFiles = config.spec
                )
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
        fun eval(sourceCode: String, project: EvaluationContext) = eval(sourceCode.toScriptSource(), project)

        /** Evaluates the given project script [sourceCode] against the given [evaluationContext]. */
        fun eval(
            sourceCode: SourceCode,
            evaluationContext: EvaluationContext,
            sharedClassLoader: ClassLoader? = null
        ): ResultWithDiagnostics<EvaluationResult> {
            val compilationConfiguration =
                createJvmCompilationConfigurationFromTemplate<CokoScript>()
            val evaluationConfiguration =
                createJvmEvaluationConfigurationFromTemplate<CokoScript> {
                    // constructorArgs(...)
                    implicitReceivers(evaluationContext)
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
            evaluationContext: EvaluationContext,
            specFiles: List<Path>
        ): SpecEvaluator {
            var sharedClassLoader: ClassLoader? = null
            val specEvaluator = SpecEvaluator()
            for (specFile in specFiles) {
                // compile the script
                val result =
                    eval(
                        specFile.toScriptSource(),
                        evaluationContext = evaluationContext,
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
