package de.fraunhofer.aisec.codyze.crymlin

import de.fraunhofer.aisec.codyze.analysis.Finding
import de.fraunhofer.aisec.codyze.analysis.FindingDescription.Companion.instance
import de.fraunhofer.aisec.cpg.sarif.Region
import de.fraunhofer.aisec.mark.markDsl.Action
import java.io.File
import java.net.URI
import java.net.URISyntaxException
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import org.junit.jupiter.api.Test

/** Tests the SARIF-like format for Finding outputs. */
internal class FindingDescriptionTest {
    @Test
    @Throws(URISyntaxException::class)
    fun testFindingDescription() {
        // Create some Finding object
        val logMsg = "Variable cm not initialized"
        val artifactUri = URI.create("file:///tmp/test.cpp")
        val id = "WrongUseOfBotan_CipherMode"
        val regions = listOf(Region(1, 2, 10, 12))
        val isProblem = true
        Finding(id, Action.FAIL, logMsg, artifactUri, regions, isProblem)

        // Initialize database with explanations
        val fd = instance
        val url = this.javaClass.classLoader.getResource("unittests/findingDescription.json")

        assertNotNull(url)

        fd.init(File(url.toURI()))

        val item = fd.get(id)
        val fullDescription = fd.getDescriptionFull(id)
        val shortDescription = fd.getDescriptionShort(id)
        val passMessage = fd.getDescriptionPass(id)
        val helpUri = fd.getHelpUri(id)
        val fixes = fd.getFixes(id)

        assertNotNull(item)
        assertEquals("Full description", fullDescription)
        assertEquals("The order of called Botan methods is wrong.", shortDescription)
        assertEquals("Order of Botan methods validated.", passMessage)
        assertEquals("https://www.codyze.io/explanations/10", helpUri)
        assertEquals(1, fixes?.size)
        assertEquals("Just fix it!", fixes?.get(0) ?: "")
    }
}
