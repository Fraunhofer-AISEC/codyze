package de.fraunhofer.aisec.codyze.executor

import de.fraunhofer.aisec.codyze.plugins.aggregator.Parser
import de.fraunhofer.aisec.codyze.plugins.executor.PMDExecutor
import de.fraunhofer.aisec.codyze.plugins.executor.SpotbugsExecutor
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

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

        // TODO
        val results = run.results
        assertNotNull(results)
        assertEquals(2, results.size)
        assertEquals("DM_DEFAULT_ENCODING", results[0].ruleID)
        assertEquals(102, results[0].locations?.first()?.physicalLocation?.region?.startLine)
        assertEquals("DM_DEFAULT_ENCODING", results[1].ruleID)
        assertEquals(103, results[1].locations?.first()?.physicalLocation?.region?.startLine)
    }

    @AfterEach
    fun cleanup() {
        File("src/test/resources/generatedReports/$resultFileName").delete()
    }
}