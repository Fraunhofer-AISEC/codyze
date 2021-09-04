package de.fraunhofer.aisec.codyze.crymlin

import de.fraunhofer.aisec.codyze.analysis.AnalysisContext
import de.fraunhofer.aisec.codyze.analysis.AnalysisServer
import de.fraunhofer.aisec.codyze.analysis.ServerConfiguration
import de.fraunhofer.aisec.codyze.analysis.passes.EdgeCachePass
import de.fraunhofer.aisec.codyze.analysis.passes.IdentifierPass
import de.fraunhofer.aisec.cpg.TranslationConfiguration
import de.fraunhofer.aisec.cpg.TranslationManager
import java.io.*
import java.lang.Exception
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.Throws
import org.junit.jupiter.api.*

internal class AnalysisServerQueriesTest {
    /** Test analysis context - additional in-memory structures used for analysis. */
    @Test
    fun contextTest() {
        // Get analysis context from scratch
        val ctx = result
        Assertions.assertNotNull(ctx)
    }

    companion object {
        private lateinit var server: AnalysisServer
        private var result: AnalysisContext? = null
        @BeforeAll
        @Throws(Exception::class)
        @JvmStatic
        fun startup() {
            val classLoader = AnalysisServerQueriesTest::class.java.classLoader
            var resource = classLoader.getResource("good/Bouncycastle.java")
            Assertions.assertNotNull(resource)
            val javaFile = File(resource.file)
            Assertions.assertNotNull(javaFile)
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

            // Start the analysis
            val translationManager = newJavaAnalysisRun(javaFile)
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
            server.stop()
        }

        /**
         * Helper method for initializing an Analysis Run.
         *
         * @param sourceLocations
         * @return
         */
        private fun newJavaAnalysisRun(vararg sourceLocations: File): TranslationManager {
            return TranslationManager.builder()
                .config(
                    TranslationConfiguration.builder()
                        .debugParser(true)
                        .failOnError(false)
                        .defaultPasses()
                        .defaultLanguages()
                        .registerPass(IdentifierPass())
                        .registerPass(EdgeCachePass())
                        .sourceLocations(*sourceLocations)
                        .build()
                )
                .build()
        }
    }
}
