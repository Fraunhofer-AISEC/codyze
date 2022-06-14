package de.fraunhofer.aisec.codyze.specification_languages.mark

//import de.fraunhofer.aisec.codyze.mark.analysis.AnalysisContext
//import de.fraunhofer.aisec.codyze.mark.analysis.AnalysisServer
//import de.fraunhofer.aisec.codyze.mark.analysis.markevaluation.Evaluator
import de.fraunhofer.aisec.codyze_core.Executor
import de.fraunhofer.aisec.cpg.ExperimentalGraph
import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.graph
import de.fraunhofer.aisec.cpg.helpers.Benchmark
import io.github.detekt.sarif4k.Result
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class MarkExecutor : Executor {
    override val name: String
        get() = MarkExecutor::class.simpleName ?: "MarkExecutor"

    override val supportedFileExtensions: List<String>
        get() = listOf(".mark")

    override fun initialize(configuration: String) {
        TODO("Not yet implemented")
    }

    @OptIn(ExperimentalGraph::class)
    override fun evaluate(cpg: TranslationResult): List<Result> {
//        val analysisContext: AnalysisContext =
//            CompletableFuture.completedFuture(cpg)
//                .thenApply {
//                    val ctx =
//                        AnalysisContext(
//                            emptyList(),
//                            it.graph
//                        ) // NOTE: We currently operate on a single source file.
//
//                    // Attach analysis context to result
//                    it.getScratch().put("ctx", ctx)
//                    Pair<TranslationResult, AnalysisContext>(it, ctx)
//                }
//                .thenApply {
//                    val bench = Benchmark(AnalysisServer::class.java, "  Evaluation of MARK")
//                    // TODO reapply logging
//                    //            AnalysisServer.log.info(
//                    //                "Evaluating mark: {} entities, {} rules",
//                    //                this.markModel.getEntities().size,
//                    //                this.markModel.getRules().size
//                    //            )
//
//                    // Evaluate all MARK rules
//                    val evaluator = Evaluator(this.markModel, this.serverConfig)
//
//                    val result: TranslationResult = it.first
//                    val ctx: AnalysisContext = it.second
//
//                    evaluator.evaluate(result, ctx)
//
//                    bench.stop()
//
//                    ctx
//                }
//                .thenApply {
//                    val bench = Benchmark(AnalysisServer::class.java, "  Filtering results")
//
//                    if (!serverConfig.pedantic && serverConfig.disableGoodFindings) {
//                        // Filter out "positive" results
//                        it.getFindings().removeIf { finding -> !finding.isProblem() }
//                    }
//                    bench.stop()
//                    it
//                }
//                .get(1L, TimeUnit.HOURS)
//
//        // TODO convert findings into results
//        analysisContext.findings.forEach {
//
//        }

        // return list of results
        return emptyList()
    }
}
