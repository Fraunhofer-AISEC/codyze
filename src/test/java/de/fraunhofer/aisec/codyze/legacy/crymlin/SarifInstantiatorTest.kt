package de.fraunhofer.aisec.codyze.legacy.crymlin

import com.fasterxml.jackson.databind.ObjectMapper
import de.fraunhofer.aisec.codyze.legacy.analysis.Finding
import de.fraunhofer.aisec.codyze.legacy.analysis.FindingDescription
import de.fraunhofer.aisec.codyze.legacy.sarif.SarifInstantiator
import de.fraunhofer.aisec.codyze.sarif.schema.Result
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
        val url = this.javaClass.classLoader.getResource("legacy/unittests/findingDescription.json")
        assertNotNull(url)
        fd.init(File(url.toURI()))
        s.pushRun(setOf(f1))

        val output = s.toString()
        println(output)

        // parse the output string again and match the fields
        val mapper = ObjectMapper()
        val parsed = mapper.readTree(output)
        assertEquals(
            "\"codyze\"",
            parsed.get("runs").get(0).get("tool").get("driver").get("name").toString()
        )
        assertEquals(
            "\"WrongUseOfBotan_CipherMode\"",
            parsed.get("runs").get(0).get("results").get(0).get("ruleId").toString()
        )
        assertEquals(
            "\"fail\"",
            parsed.get("runs").get(0).get("results").get(0).get("kind").toString()
        )
        assertEquals(
            "\"error\"",
            parsed.get("runs").get(0).get("results").get(0).get("level").toString()
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
        val url = this.javaClass.classLoader.getResource("legacy/unittests/findingDescription.json")

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
        private const val filepath = "src/test/resources/legacy/unittests/testOutput.sarif"

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
