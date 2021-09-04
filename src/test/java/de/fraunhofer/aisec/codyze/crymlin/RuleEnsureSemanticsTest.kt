package de.fraunhofer.aisec.codyze.crymlin

import de.fraunhofer.aisec.codyze.analysis.AnalysisContext
import de.fraunhofer.aisec.codyze.analysis.MarkContextHolder
import de.fraunhofer.aisec.codyze.analysis.MarkIntermediateResult
import de.fraunhofer.aisec.codyze.analysis.ServerConfiguration
import de.fraunhofer.aisec.codyze.analysis.TypestateMode
import de.fraunhofer.aisec.codyze.analysis.markevaluation.ExpressionEvaluator
import de.fraunhofer.aisec.codyze.analysis.resolution.ConstantValue
import de.fraunhofer.aisec.codyze.analysis.wpds.NFA
import de.fraunhofer.aisec.codyze.markmodel.MarkModelLoader
import de.fraunhofer.aisec.cpg.ExperimentalGraph
import de.fraunhofer.aisec.cpg.TranslationManager
import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.graph
import de.fraunhofer.aisec.mark.XtextParser
import de.fraunhofer.aisec.mark.markDsl.MarkModel
import java.io.*
import java.util.*
import java.util.stream.Collectors
import org.junit.jupiter.api.*

internal class RuleEnsureSemanticsTest {
    @OptIn(ExperimentalGraph::class)
    private fun test(markFileEnding: String) {
        val markFilePaths =
            markModels
                .keys
                .stream()
                .filter { n: String -> n.endsWith(markFileEnding) }
                .collect(Collectors.toList())
        Assertions.assertEquals(1, markFilePaths.size)
        val mark = MarkModelLoader().load(markModels, markFilePaths[0])
        val config =
            ServerConfiguration.builder()
                .markFiles(markFilePaths[0])
                .typestateAnalysis(TypestateMode.NFA)
                .build()
        val graph = TranslationResult(TranslationManager.builder().build()).graph
        val ctx = AnalysisContext(File(markFilePaths[0]), graph)
        val allResults: MutableMap<String, Map<Int, MarkIntermediateResult>> = TreeMap()
        for (r in mark.rules) {
            val markContextHolder = MarkContextHolder()
            markContextHolder.allContexts[0] =
                null // add a dummy, so that we get exactly one result back for this context
            val ee = ExpressionEvaluator(graph, mark, r, ctx, config, markContextHolder)
            val ensureExpr = r.statement.ensure.exp
            val result = ee.evaluateExpression(ensureExpr)
            Assertions.assertEquals(1, result.size)
            allResults[r.name] = result
        }
        allResults.forEach { (key: String, value: Map<Int, MarkIntermediateResult>) ->
            Assertions.assertTrue(value[0] is ConstantValue)
            val inner = value[0]
            if (key.endsWith("true")) {
                Assertions.assertEquals(true, (inner as ConstantValue?)!!.value, key)
            } else if (key.endsWith("false")) {
                Assertions.assertEquals(false, (inner as ConstantValue?)!!.value, key)
            } else if (key.endsWith("fail")) {
                Assertions.assertTrue(ConstantValue.isError(inner))
            } else {
                Assertions.fail<Any>("Unexpected: Rule should have failed, but is $inner: $key")
            }
        }
    }

    @Test
    fun testEquals() {
        test("equals.mark")
    }

    @Test
    fun testLessThan() {
        test("lt.mark")
    }

    @Test
    fun testGreaterThan() {
        test("gt.mark")
    }

    companion object {
        private lateinit var markModels: HashMap<String, MarkModel>
        @BeforeAll
        @JvmStatic
        fun startup() {
            val resource =
                RuleEnsureSemanticsTest::class.java.classLoader.getResource(
                    "mark/rules/ensure/semantics/"
                )
            Assertions.assertNotNull(resource)
            val markFile = File(resource.file)
            Assertions.assertNotNull(markFile)
            var directoryContent =
                markFile.listFiles { current: File?, name: String -> name.endsWith(".mark") }
            if (directoryContent == null) {
                directoryContent = arrayOf(markFile)
            }
            Assertions.assertNotNull(directoryContent)
            Assertions.assertTrue(directoryContent.isNotEmpty())
            val parser = XtextParser()
            for (mf in directoryContent) {
                parser.addMarkFile(mf)
            }
            markModels = parser.parse()
            Assertions.assertFalse(markModels.isEmpty())
        }
    }
}
