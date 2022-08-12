package de.fraunhofer.aisec.codyze.specification_languages.nwt

import de.fraunhofer.aisec.codyze.specification_languages.nwt.scripting.NwtScript
import de.fraunhofer.aisec.codyze_core.Executor
import de.fraunhofer.aisec.codyze_core.config.ExecutorConfiguration
import de.fraunhofer.aisec.cpg.TranslationManager
import io.github.detekt.sarif4k.Result
import java.nio.file.Path
import kotlin.script.experimental.api.EvaluationResult
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.ScriptDiagnostic
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate

class NwtExecutor : Executor {
    override val name: String
        get() = NwtExecutor::class.simpleName ?: "NwtExecutor"
    override val supportedFileExtension: String
        get() = "nwt.kts"

    lateinit var configuration: ExecutorConfiguration

    override fun initialize(configuration: ExecutorConfiguration) {
        this.configuration = configuration
    }

    @OptIn(de.fraunhofer.aisec.cpg.ExperimentalGraph::class)
    override fun evaluate(analyzer: TranslationManager): List<Result> {
        fun evalFile(scriptFile: Path): ResultWithDiagnostics<EvaluationResult> {
            val compilationConfiguration =
                createJvmCompilationConfigurationFromTemplate<NwtScript> {
                    jvm { dependenciesFromCurrentContext(wholeClasspath = true) }
                }
            return BasicJvmScriptingHost()
                .eval(scriptFile.toScriptSource(), compilationConfiguration, null)
        }

        fun execFile(path: Path) {
            val res = evalFile(path)

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
}
