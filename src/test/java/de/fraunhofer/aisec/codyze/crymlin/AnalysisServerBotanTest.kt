package de.fraunhofer.aisec.codyze.crymlin

import de.fraunhofer.aisec.codyze.analysis.AnalysisContext
import de.fraunhofer.aisec.codyze.analysis.AnalysisServer
import de.fraunhofer.aisec.codyze.analysis.Finding
import de.fraunhofer.aisec.codyze.analysis.ServerConfiguration
import de.fraunhofer.aisec.codyze.analysis.passes.EdgeCachePass
import de.fraunhofer.aisec.codyze.analysis.passes.IdentifierPass
import de.fraunhofer.aisec.cpg.TranslationConfiguration
import de.fraunhofer.aisec.cpg.TranslationManager
import java.io.*
import java.lang.Exception
import java.util.ArrayList
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.function.Consumer
import kotlin.Throws
import org.junit.jupiter.api.*

internal class AnalysisServerBotanTest {
    @Test
    fun markModelTest() {
        val markModel = server!!.markModel
        Assertions.assertNotNull(markModel)
        val rules = markModel.rules
        Assertions.assertEquals(7, rules.size)
        val ents = markModel.entities
        Assertions.assertEquals(9, ents.size)
    }

    @Test
    fun markEvaluationTest() {
        val ctx = result
        Assertions.assertNotNull(ctx)
        val findings: MutableList<String> = ArrayList()
        Assertions.assertNotNull(ctx!!.findings)
        ctx.findings.forEach(Consumer { x: Finding -> findings.add(x.toString()) })
        println("Findings")
        for (finding in findings) {
            println(finding)
        }
    }

    companion object {
        private lateinit var server: AnalysisServer
        private var result: AnalysisContext? = null
        @BeforeAll
        @Throws(Exception::class)
        fun startup() {
            val classLoader = AnalysisServerBotanTest::class.java.classLoader
            var resource = classLoader.getResource("mark_cpp/symm_block_cipher.cpp")
            Assertions.assertNotNull(resource)
            val cppFile = File(resource.file)
            Assertions.assertNotNull(cppFile)
            resource = classLoader.getResource("mark/PoC_MS1/Botan_AutoSeededRNG.mark")
            Assertions.assertNotNull(resource)
            val markPoC1 = File(resource.file)
            Assertions.assertNotNull(markPoC1)
            val markModelFiles = markPoC1.parent

            // Start an analysis server
            server =
                AnalysisServer.builder()
                    .config(
                        ServerConfiguration.builder()
                            .launchConsole(false)
                            .launchLsp(false)
                            .markFiles(markModelFiles)
                            .build()
                    )
                    .build()
            server.start()

            // Start the analysis (BOTAN Symmetric Example by Oliver)
            val translationManager = newAnalysisRun(cppFile)
            val analyze = server.analyze(translationManager)
            try {
                result = analyze[5, TimeUnit.MINUTES]
            } catch (t: TimeoutException) {
                analyze.cancel(true)
                throw t
            }
        }

        @AfterAll
        fun teardown() {
            // Stop the analysis server
            server!!.stop()
        }

        /**
         * Helper method for initializing an Analysis Run.
         *
         * @param sourceLocations
         * @return
         */
        private fun newAnalysisRun(vararg sourceLocations: File): TranslationManager {
            return TranslationManager.builder()
                .config(
                    TranslationConfiguration.builder()
                        .debugParser(true)
                        .failOnError(false)
                        .defaultPasses()
                        .registerPass(IdentifierPass())
                        .registerPass(EdgeCachePass())
                        .defaultLanguages()
                        .sourceLocations(*sourceLocations)
                        .build()
                )
                .build()
        }
    }
}
