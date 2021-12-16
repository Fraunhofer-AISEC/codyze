package de.fraunhofer.aisec.codyze.crymlin

import de.fraunhofer.aisec.codyze.analysis.Finding
import de.fraunhofer.aisec.codyze.analysis.FindingDescription
import de.fraunhofer.aisec.codyze.analysis.SarifInstantiator
import de.fraunhofer.aisec.codyze.analysis.generated.Result
import de.fraunhofer.aisec.cpg.sarif.Region
import de.fraunhofer.aisec.mark.markDsl.Action
import java.io.File
import java.net.URI
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail
import org.junit.jupiter.api.AfterAll
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
        val kind = Result.Kind.FAIL
        val f1 = Finding(id, Action.FAIL, logMsg, artifactUri, regions, kind)

        // Initialize database with explanations
        val fd = FindingDescription.instance
        val url = this.javaClass.classLoader.getResource("unittests/findingDescription.json")
        assertNotNull(url)
        fd.init(File(url.toURI()))
        s.pushRun(setOf(f1))

        val output = s.toString()
        val startOfResults = output.indexOf("results")
        val substring0 = output.substring(startOfResults, startOfResults + 7)
        val substring1 = output.substring(startOfResults + 22, startOfResults + 30)
        val substring2 = output.substring(startOfResults + 33, startOfResults + 61)

        println(output)

        assertEquals(
            "results",
            substring0,
            "The starting point of the result block was not initialized correctly."
        )
        // one char in each direction as buffer because for some reason the test randomly fails by
        // one character
        assertTrue(substring1.contains("ruleId"), "The ruleId-field was not in the expected place.")
        assertTrue(
            substring2.contains("WrongUseOfBotan_CipherMode"),
            "The ruleId-value was not in the expected place."
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
        val kind = Result.Kind.FAIL
        val f1 = Finding(id, Action.FAIL, logMsg, artifactUri, regions, kind)

        // Initialize database with explanations
        val fd = FindingDescription.instance
        val url = this.javaClass.classLoader.getResource("unittests/findingDescription.json")

        assertNotNull(url)

        fd.init(File(url.toURI()))

        s.pushRun(setOf(f1))

        val file = File(filepath)

        try {
            file.delete()
        } catch (se: SecurityException) {
            fail("The File already exists and could not be deleted beforehand!")
        }

        s.generateOutput(file)

        assertTrue(file.exists(), "There was no output file created!")
    }

    companion object {
        private const val filepath = "src/test/resources/unittests/testOutput.sarif"

        @AfterAll
        @JvmStatic
        fun removeFile() {
            val file = File(filepath)
            try {
                file.delete()
            } catch (se: SecurityException) {
                se.printStackTrace()
            }
        }
    }
}
