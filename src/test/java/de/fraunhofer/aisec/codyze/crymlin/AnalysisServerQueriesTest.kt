package de.fraunhofer.aisec.codyze.crymlin

import de.fraunhofer.aisec.codyze.analysis.AnalysisContext
import de.fraunhofer.aisec.codyze.analysis.AnalysisServer
import de.fraunhofer.aisec.codyze.config.CodyzeConfiguration
import de.fraunhofer.aisec.codyze.config.Configuration
import de.fraunhofer.aisec.codyze.config.CpgConfiguration
import java.io.*
import java.lang.Exception
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.Throws
import kotlin.test.assertNotNull
import org.junit.jupiter.api.*

internal class AnalysisServerQueriesTest : AbstractTest() {
    /** Test analysis context - additional in-memory structures used for analysis. */
    @Test
    fun contextTest() {
        // Get analysis context from scratch
        val ctx = result
        assertNotNull(ctx)
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
            assertNotNull(resource)

            val javaFile = File(resource.file)
            assertNotNull(javaFile)

            resource = classLoader.getResource("mark/PoC_MS1/Botan_AutoSeededRNG.mark")
            assertNotNull(resource)

            val markPoC1 = File(resource.file)
            assertNotNull(markPoC1)

            val markModelFiles = markPoC1.parent

            // Start an analysis server
            val codyze = CodyzeConfiguration()
            codyze.mark = arrayOf(File(markModelFiles))

            val config = Configuration(codyze, CpgConfiguration())
            config.executionMode.isCli = false
            config.executionMode.isLsp = false

            server = AnalysisServer(config)
            server.start()

            // Start the analysis
            val translationManager = newAnalysisRun(javaFile)
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
    }
}
