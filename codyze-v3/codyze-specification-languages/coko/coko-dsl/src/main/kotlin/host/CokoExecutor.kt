package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_dsl.host

import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.Project
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_dsl.CokoScript
import de.fraunhofer.aisec.codyze_core.Executor
import de.fraunhofer.aisec.codyze_core.config.ExecutorConfiguration
import de.fraunhofer.aisec.cpg.TranslationManager
import io.github.detekt.sarif4k.Result
import java.nio.file.Path
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate

class CokoExecutor : Executor {
    override val name: String
        get() = CokoExecutor::class.simpleName ?: "NwtExecutor"
    override val supportedFileExtension: String
        get() = "nwt.kts"

    lateinit var configuration: ExecutorConfiguration

    override fun initialize(configuration: ExecutorConfiguration) {
        this.configuration = configuration
    }

    @OptIn(de.fraunhofer.aisec.cpg.ExperimentalGraph::class)
    override fun evaluate(analyzer: TranslationManager): List<Result> {

        fun execFile(path: Path) {
            val compilationConfiguration =
                createJvmCompilationConfigurationFromTemplate<CokoScript> {
                    jvm { dependenciesFromCurrentContext(wholeClasspath = true) }
                }
            val res =
                BasicJvmScriptingHost().eval(path.toScriptSource(), compilationConfiguration, null)
            // val res = eval(path.toScriptSource(), project = Project)  // TODO: get a Project
            // object!

            res.reports.forEach {
                if (it.severity > ScriptDiagnostic.Severity.DEBUG) {
                    println(
                        " : ${it.message}" + if (it.exception == null) "" else ": ${it.exception}"
                    )
                }
            }
        }

        execFile(configuration.spec[0])
        return listOf()
    }

    // this is used in the tests for now
    companion object {
        /** Evaluates the given project script [sourceCode] against the given [project]. */
        fun eval(sourceCode: String, project: Project) = eval(sourceCode.toScriptSource(), project)

        /** Evaluates the given project script [sourceCode] against the given [project]. */
        fun eval(
            sourceCode: SourceCode,
            project: Project
        ): ResultWithDiagnostics<EvaluationResult> =
            BasicJvmScriptingHost()
                .evalWithTemplate<CokoScript>(sourceCode, evaluation = { constructorArgs(project) })
    }
}
