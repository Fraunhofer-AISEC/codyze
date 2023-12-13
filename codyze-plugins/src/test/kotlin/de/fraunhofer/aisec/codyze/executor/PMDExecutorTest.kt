package de.fraunhofer.aisec.codyze.executor

import de.fraunhofer.aisec.codyze.plugins.aggregator.Parser
import de.fraunhofer.aisec.codyze.plugins.executor.PMDExecutor
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PMDExecutorTest {
    private val resultFileName = "pmd.sarif"

    @Test
    fun testExample() {
        val pmd = PMDExecutor()
        pmd.execute(
            listOf(Path.of("src/test/resources/targets/TlsServer.java")),
            File("src/test/resources/generatedReports/$resultFileName")
        )

        val run = Parser().extractLastRun(File("src/test/resources/generatedReports/$resultFileName"))
        assertNotNull(run)

        if (!run.invocations.isNullOrEmpty()) {
            run.invocations!!.forEach { assertTrue { it.executionSuccessful } }
        }

        // TODO - Find Fix for clashing dependencies
        // we expect 1 "AvoidPrintStackTrace" and 24 "SystemPrintln" results
        var results = run.results
        assertNotNull(results)
        assertEquals(109, results.first { it.ruleID == "AvoidPrintStackTrace" }.locations?.first()?.physicalLocation?.region?.startLine)
        assertEquals(25, results.size)
        results = results.filterNot { it.ruleID == "AvoidPrintStackTrace" }.toList()
        assertEquals(24, results.size)
        results = results.filterNot { it.ruleID == "SystemPrintln" }
        assertEquals( 0, results.size)

    }

    @AfterEach
    fun cleanup() {
        File("src/test/resources/generatedReports/$resultFileName").delete()
    }
}