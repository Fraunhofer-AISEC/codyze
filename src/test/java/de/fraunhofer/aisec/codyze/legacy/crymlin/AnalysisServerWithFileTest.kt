package de.fraunhofer.aisec.codyze.legacy.crymlin

import de.fraunhofer.aisec.codyze.legacy.analysis.AnalysisContext
import de.fraunhofer.aisec.codyze.legacy.analysis.AnalysisServer
import de.fraunhofer.aisec.codyze.legacy.config.CodyzeConfiguration
import java.io.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.function.Consumer
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import org.junit.jupiter.api.*

class AnalysisServerWithFileTest : AbstractTest() {
    @Test
    fun markModelTest() {
        val markModel = server.markModel
        assertNotNull(markModel)

        val rules = markModel.rules
        assertEquals(7, rules.size)

        val entities = markModel.entities
        assertEquals(9, entities.size)
    }

    @Test
    fun markEvaluationTest() {
        val ctx = result
        assertNotNull(ctx)

        val findings: MutableList<String> = ArrayList()
        assertNotNull(ctx.findings)

        ctx.findings.forEach(Consumer { findings.add(it.toString()) })

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
        @JvmStatic
        fun startup() {
            val classLoader = AnalysisServerBotanTest::class.java.classLoader
            var resource = classLoader.getResource("legacy/mark_cpp/symm_block_cipher.cpp")
            assertNotNull(resource)
            val cppFile = File(resource.file)
            assertNotNull(cppFile)
            resource = classLoader.getResource("legacy/mark/PoC_MS1/Botan_AutoSeededRNG.mark")
            assertNotNull(resource)
            val markPoC1 = File(resource.file)
            assertNotNull(markPoC1)
            val markModelFiles = markPoC1.parent

            // Start an analysis server
            val codyze = CodyzeConfiguration()
            codyze.mark = arrayOf(File(markModelFiles))

            val config = newAnalysisRun(codyze)
            config.executionMode.isCli = false
            config.executionMode.isLsp = false

            server = AnalysisServer(config)
            server.start()

            // Start the analysis (BOTAN Symmetric Example by Oliver)
            val analyze = server.analyze(cppFile.absolutePath)
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
