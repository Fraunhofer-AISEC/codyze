package de.fraunhofer.aisec.codyze.executor

import de.fraunhofer.aisec.codyze.plugins.aggregator.Parser
import de.fraunhofer.aisec.codyze.plugins.executor.FindSecBugsExecutor
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class FindSecBugsExecutorTest {
    private val resultFileName = "findsecbugs.sarif"

    @Test
    fun testExample() {
        val findSecBugs = FindSecBugsExecutor()
        findSecBugs.execute(
            listOf(Path.of("src/test/resources/targets/libs/demo-cloud-service-1.0.0.jar")),
            File("src/test/resources/generatedReports/$resultFileName")
        )

        // FIXME: Parsing fails because SpotBugs violates the SARIF specification
        val run = Parser().extractLastRun(File("src/test/resources/generatedReports/$resultFileName"))
        assertNotNull(run)

        if (!run.invocations.isNullOrEmpty()) {
            run.invocations!!.forEach { assertTrue { it.executionSuccessful } }
        }

        // FIXME: Results that should be found are not reported, see https://find-sec-bugs.github.io/bugs.htm
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