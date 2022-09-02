package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_dsl.host

import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.CokoProject
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
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class CokoExecutor : Executor {
    override val name: String
        get() = CokoExecutor::class.simpleName ?: "CokoExecutor"
    override val supportedFileExtension: String
        get() = "codyze.kts"

    lateinit var configuration: ExecutorConfiguration

    override fun initialize(configuration: ExecutorConfiguration) {
        this.configuration = configuration
    }

    @OptIn(ExperimentalTime::class)
    override fun evaluate(analyzer: TranslationManager): List<Result> {
        logger.info("Constructing the CPG...")
        val cpg = analyzer.analyze().get()

        val evaluator = CPGEvaluator(cpg)
        val project = CokoProject()

        logger.info("Compiling specification scripts...")
        // compile the spec scripts
        val specCompilationDuration: Duration = measureTime {
            for (specFile in configuration.spec) {

                // compile the script
                val result =
                    eval(specFile.toScriptSource(), project = project, evaluator = evaluator)

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

                // Get the class loader for the first loaded skript. We give that one to all the
                // other skripts to ensure they find "the same" classes.
                if (actualClassLoader == null) {
                    actualClassLoader =
                        result
                            .valueOrNull()
                            ?.configuration
                            ?.get(PropertiesCollection.Key<ClassLoader>("actualClassLoader"))
                }

                // analyze script contents
                result.valueOrNull()?.returnValue?.let {
                    if (it.scriptInstance != null && it.scriptClass != null) {
                        evaluator.addSpec(it.scriptClass!!, it.scriptInstance!!)
                    }
                }
            }
        }
        logger.debug(
            "Compiled specification scripts in ${ specCompilationDuration.inWholeMilliseconds } ms"
        )

        logger.info("Evaluating specification scripts...")
        // evaluate the spec scripts
        val findings = evaluator.evaluate()
        return listOf()
    }

    companion object {
        /**
         * Evaluates the given project script [sourceCode] against the given [project] and
         * [evaluator].
         */
        fun eval(sourceCode: String, project: Project, evaluator: CPGEvaluator) =
            eval(sourceCode.toScriptSource(), project, evaluator)

        var actualClassLoader: ClassLoader? = null

        val scriptingHost by lazy { BasicJvmScriptingHost() }

        /** Evaluates the given project script [sourceCode] against the given [project]. */
        fun eval(
            sourceCode: SourceCode,
            project: Project,
            evaluator: CPGEvaluator,
        ): ResultWithDiagnostics<EvaluationResult> {
            val compilationConfiguration =
                createJvmCompilationConfigurationFromTemplate<CokoScript>()
            val evaluationConfiguration =
                createJvmEvaluationConfigurationFromTemplate<CokoScript> {
                    constructorArgs(project)
                    implicitReceivers(evaluator)
                    scriptsInstancesSharing(true)
                    jvm {
                        if (actualClassLoader != null) {
                            baseClassLoader.put(actualClassLoader)
                        }
                    }
                }

            return scriptingHost.eval(
                sourceCode,
                compilationConfiguration,
                evaluationConfiguration,
            )
        }
    }
}
