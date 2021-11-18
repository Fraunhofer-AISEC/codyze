package de.fraunhofer.aisec.codyze.crymlin

import de.fraunhofer.aisec.codyze.analysis.Finding
import de.fraunhofer.aisec.codyze.analysis.FindingDescription
import de.fraunhofer.aisec.codyze.analysis.SarifInstantiator
import de.fraunhofer.aisec.cpg.sarif.Region
import de.fraunhofer.aisec.mark.markDsl.Action
import java.io.File
import java.net.URI
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test

internal class SarifInstantiatorTest {
    @Test
    fun testToString() {
        val s = SarifInstantiator()

        // "borrowed" the FindingDescriptionTest for a Finding to use
        // Create some Finding object
        val logMsg = "Variable cm not initialized"
        val artifactUri = URI.create("file:///tmp/test.cpp")
        val id = "WrongUseOfBotan_CipherMode"
        val regions = listOf(Region(0, 2, 10, 12))
        val isProblem = true
        val f1 = Finding(id, Action.FAIL, logMsg, artifactUri, regions, isProblem)

        // Initialize database with explanations
        val fd = FindingDescription.instance
        val url = this.javaClass.classLoader.getResource("unittests/findingDescription.json")

        assertNotNull(url)

        fd.init(File(url.toURI()))

        s.pushRun(setOf(f1))

        val output = s.toString()

        val startOfResults = output.indexOf("results")
        val substring1 = output.substring(startOfResults + 33, startOfResults + 39)
        assertEquals(
            "ruleId",
            substring1,
            "The ruleId of the first Result is not in the expected place!"
        )
        val substring2 = output.substring(startOfResults + 43, startOfResults + 69)
        assertEquals(
            "WrongUseOfBotan_CipherMode",
            substring2,
            "The first Result has an unexpected ruleId!"
        )
    }

    @Test
    fun testOutput() {
        val s = SarifInstantiator()

        // "borrowed" the FindingDescriptionTest for a Finding to use
        // Create some Finding object
        val logMsg = "Variable cm not initialized"
        val artifactUri = URI.create("file:///tmp/test.cpp")
        val id = "WrongUseOfBotan_CipherMode"
        val regions = listOf(Region(0, 2, 10, 12))
        val isProblem = true
        val f1 = Finding(id, Action.FAIL, logMsg, artifactUri, regions, isProblem)

        // Initialize database with explanations
        val fd = FindingDescription.instance
        val url = this.javaClass.classLoader.getResource("unittests/findingDescription.json")

        assertNotNull(url)

        fd.init(File(url.toURI()))

        s.pushRun(setOf(f1))

        val filepath = "src/test/resources/unittests/testOutput.sarif"

        val file = File(filepath)
        file.delete()

        s.generateOutput(file)

        assertTrue(file.exists(), "There was no output file created!")
    }
}
