package de.fraunhofer.aisec.codyze.specification_languages.coko

import de.fraunhofer.aisec.codyze.specification_languages.coko.nwt.scripting.CoKoScript
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
import kotlin.time.ExperimentalTime

class CoKoExecutor : Executor {
    override val name: String
        get() = CoKoExecutor::class.simpleName ?: "CoKoExecutor"
    override val supportedFileExtension: String
        get() = "coko.kts"

    lateinit var configuration: ExecutorConfiguration

    override fun initialize(configuration: ExecutorConfiguration) {
        this.configuration = configuration
    }

    @OptIn(de.fraunhofer.aisec.cpg.ExperimentalGraph::class, ExperimentalTime::class)
    override fun evaluate(analyzer: TranslationManager): List<Result> {
        fun evalFile(scriptFile: Path): ResultWithDiagnostics<EvaluationResult> {
            val compilationConfiguration =
                createJvmCompilationConfigurationFromTemplate<CoKoScript> {
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
