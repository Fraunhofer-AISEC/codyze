package de.fraunhofer.aisec.codyze.crymlin

import de.fraunhofer.aisec.codyze.analysis.markevaluation.*
import de.fraunhofer.aisec.codyze.analysis.server.AnalysisServer
import de.fraunhofer.aisec.codyze.analysis.structures.AnalysisContext
import de.fraunhofer.aisec.codyze.analysis.structures.ServerConfiguration
import de.fraunhofer.aisec.cpg.ExperimentalGraph
import de.fraunhofer.aisec.cpg.TranslationConfiguration
import de.fraunhofer.aisec.cpg.TranslationManager
import de.fraunhofer.aisec.cpg.graph.Graph
import de.fraunhofer.aisec.cpg.graph.declarations.TypedefDeclaration
import de.fraunhofer.aisec.cpg.graph.statements.IfStatement
import de.fraunhofer.aisec.cpg.graph.statements.ReturnStatement
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

/** Tests structure of CPG generated from "real" source files. */
@ExperimentalGraph
internal class GraphTest {
    @Test
    fun testMethods() {
        val methods = graph.methods.map { it.name }

        println("METHODS:  " + methods.joinToString(", "))

        assertTrue(methods.contains("nok1"))
        assertTrue(methods.contains("nok2"))
        assertTrue(methods.contains("nok3"))
        assertTrue(methods.contains("nok4"))
        assertTrue(methods.contains("nok5"))
        assertTrue(methods.contains("ok"))
    }

    @Test
    fun crymlinDslTest() {
        val count = graph.records.size

        assertTrue(count > 0)
    }

    @Test
    fun crymlinIfstmtTest() {
        val count = graph.all<IfStatement>().size

        // Expecting 2 if stmts
        assertEquals(2, count)
    }

    @Test
    fun crymlinNamespacesTest() {
        val count = graph.namespaces.size

        // Expecting no namespaces (Java)
        assertEquals(0, count)
    }

    @Test
    fun crymlinNamespacesPatternTest() {
        val count = graph.namespaces("codyze").size

        // Expecting no namespaces (Java)
        assertEquals(0, count)
    }

    @Test
    fun crymlinTypedefsTest() {
        val count = graph.all<TypedefDeclaration>().size

        // Expecting no typedefs (Java)
        assertEquals(0, count)
    }

    @Test
    fun crymlinReturnsTest() {
        val returns: List<ReturnStatement> = graph.all()
        println(returns.size)

        // 15 (virtual) returns
        assertEquals(15, returns.size)
    }

    @Test
    fun crymlinVarsTest() {
        val vars: List<String> = graph.variables.map { it.name }

        assertTrue(vars.contains("p"))
        assertTrue(vars.contains("p2"))
        assertTrue(vars.contains("p3"))
        assertTrue(vars.contains("p4"))
        assertTrue(vars.contains("p5"))
    }

    @Test
    fun crymlinFunctionsTest() {
        val count = graph.functions.size
        assertEquals(17, count)
    }

    @Test
    fun crymlinFunctionsPatternTest() {
        val functions = graph.functions.filter { it.name.startsWith("nok") }.map { it.name }

        assertTrue(functions.contains("nok1"))
        assertTrue(functions.contains("nok2"))
        assertTrue(functions.contains("nok3"))
        assertTrue(functions.contains("nok4"))
        assertTrue(functions.contains("nok5"))
        assertFalse(functions.contains("ok"))
    }

    companion object {
        lateinit var result: AnalysisContext
        lateinit var server: AnalysisServer
        lateinit var graph: Graph

        @BeforeAll
        @JvmStatic
        @Throws(Exception::class)
        fun setup() {
            val classLoader = GraphTest::class.java.classLoader
            val resource = classLoader.getResource("unittests/order.java")
            assertNotNull(resource)

            val cppFile = File(resource.file)
            assertNotNull(cppFile)

            // Start an analysis server
            server =
                AnalysisServer.builder()
                    .config(
                        ServerConfiguration.builder().launchConsole(false).launchLsp(false).build()
                    )
                    .build()
            server.start()

            // Start the analysis
            val translationManager =
                TranslationManager.builder()
                    .config(
                        TranslationConfiguration.builder()
                            .debugParser(true)
                            .failOnError(false)
                            .codeInNodes(true)
                            .defaultPasses()
                            .defaultLanguages()
                            .sourceLocations(cppFile)
                            .build()
                    )
                    .build()
            val analyze = server.analyze(translationManager)
            try {
                result = analyze[5, TimeUnit.MINUTES]
                graph = result.graph
            } catch (t: TimeoutException) {
                analyze.cancel(true)
                throw t
            }
            val ctx = result
            assertNotNull(ctx)
        }

        @AfterAll
        fun teardown() {
            // Stop the analysis server
            server.stop()
        }
    }
}
