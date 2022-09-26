package de.fraunhofer.aisec.codyze.specification_languages.mark

import de.fraunhofer.aisec.codyze.analysis.AnalysisContext
import de.fraunhofer.aisec.codyze.analysis.AnalysisServer
import de.fraunhofer.aisec.codyze.analysis.FindingDescription
import de.fraunhofer.aisec.codyze.analysis.ServerConfiguration
import de.fraunhofer.aisec.codyze.analysis.TypestateMode as LegacyTypestateMode
import de.fraunhofer.aisec.codyze.analysis.markevaluation.Evaluator
import de.fraunhofer.aisec.codyze.markmodel.Mark
import de.fraunhofer.aisec.codyze_core.Executor
import de.fraunhofer.aisec.codyze_core.config.ExecutorConfiguration
import de.fraunhofer.aisec.cpg.ExperimentalGraph
import de.fraunhofer.aisec.cpg.TranslationManager
import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.graph
import de.fraunhofer.aisec.cpg.helpers.Benchmark
import io.github.detekt.sarif4k.Result
import kotlin.io.path.exists
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toTimeUnit
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class MarkExecutor : Executor {
    override val name: String
        get() = MarkExecutor::class.simpleName ?: "MarkExecutor"
    override val supportedFileExtension: String
        get() = "mark"

    lateinit var configuration: ExecutorConfiguration
    private lateinit var serverConfig: ServerConfiguration

    override fun initialize(configuration: ExecutorConfiguration) {
        this.configuration = configuration

        // we need this until the MarkEvaluator is rewritten to use a [ExecutorConfiguration]
        serverConfig =
            ServerConfiguration.builder()
                .markFiles(*configuration.spec.map { it.toString() }.toTypedArray())
                .typestateAnalysis(LegacyTypestateMode.valueOf(configuration.typestate.toString()))
                .disableGoodFindings(!configuration.goodFindings)
                .pedantic(configuration.pedantic)
                // .disableMark()  // TODO: not yet implemented
                .build()
    }

    @OptIn(ExperimentalGraph::class, ExperimentalTime::class)
    override fun evaluate(analyzer: TranslationManager): List<Result> {
        val markModel = Mark().from(markFiles = configuration.spec)

        // TODO: get spec description file from the given spec files?
        // load description file
        if (configuration.specDescription.exists())
            FindingDescription.instance.init(configuration.specDescription.toFile())
        else logger.info("MARK description file does not exist")

        val analysisContext: AnalysisContext =
            analyzer
                .analyze()
                .thenApply {
                    val ctx =
                        AnalysisContext(
                            emptyList(),
                            it.graph
                        ) // NOTE: We currently operate on a single source file.

                    // Attach analysis context to result
                    it.getScratch().put("ctx", ctx)
                    Pair<TranslationResult, AnalysisContext>(it, ctx)
                }
                .thenApply {
                    val bench = Benchmark(AnalysisServer::class.java, "  Evaluation of MARK")
                    logger.info {
                        "Evaluating mark: ${markModel.getEntities().size} entities, ${markModel.getRules().size} rules"
                    }

                    // Evaluate all MARK rules
                    val evaluator = Evaluator(markModel, this.serverConfig)

                    val result: TranslationResult = it.first
                    val ctx: AnalysisContext = it.second

                    evaluator.evaluate(result, ctx)

                    bench.stop()
                    ctx
                }
                .thenApply {
                    val bench = Benchmark(AnalysisServer::class.java, "  Filtering results")

                    if (!serverConfig.pedantic && serverConfig.disableGoodFindings) {
                        // Filter out "positive" results
                        it.getFindings().removeIf { finding -> !finding.isProblem() }
                    }
                    bench.stop()
                    it
                }
                .get(1L, DurationUnit.HOURS.toTimeUnit())

        // TODO convert findings into results
        analysisContext.findings.forEach { println(it) }
        return listOf()
    }
}
