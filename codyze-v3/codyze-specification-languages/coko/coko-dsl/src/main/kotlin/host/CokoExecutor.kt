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
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
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

        val evaluator = cpgEvaluator(analyzer)
        val project = CokoProject()

        logger.info { "Compiling specification scripts..." }
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

                // analyze script contents
                result.valueOrNull()?.returnValue?.scriptClass?.let { evaluator.addSpec(it) }
            }
        }
        logger.debug {
            "Compiled specification scripts in ${ specCompilationDuration.inWholeMilliseconds } ms"
        }

        logger.info { "Evaluating specification scripts..." }
        // evaluate the spec scripts
        val findings = evaluator.evaluate()
        return listOf()
    }

    companion object {
        /**
         * Evaluates the given project script [sourceCode] against the given [project] and
         * [evaluator].
         */
        fun eval(sourceCode: String, project: Project, evaluator: cpgEvaluator) =
            eval(sourceCode.toScriptSource(), project, evaluator)

        /** Evaluates the given project script [sourceCode] against the given [project]. */
        fun eval(
            sourceCode: SourceCode,
            project: Project,
            evaluator: cpgEvaluator
        ): ResultWithDiagnostics<EvaluationResult> =
            BasicJvmScriptingHost()
                .evalWithTemplate<CokoScript>(
                    sourceCode,
                    evaluation = {
                        constructorArgs(project)
                        implicitReceivers(evaluator)
                    }
                )
    }
}
