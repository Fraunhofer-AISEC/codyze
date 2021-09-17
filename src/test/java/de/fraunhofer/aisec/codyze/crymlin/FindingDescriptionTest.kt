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
        val onFailId = "WrongUseOfBotan_CipherMode"
        val regions = listOf(Region(1, 2, 10, 12))
        val isProblem = true
        Finding("Test", onFailId, Action.FAIL, logMsg, artifactUri, regions, isProblem)

        // Initialize database with explanations
        val fd = instance
        val url = this.javaClass.classLoader.getResource("unittests/findingDescription.json")

        assertNotNull(url)

        fd.init(File(url.toURI()))

        val item = fd.get(onFailId)
        val fullDescription = fd.getDescriptionFull(onFailId)
        val shortDescription = fd.getDescriptionShort(onFailId)
        val helpUri = fd.getHelpUri(onFailId)
        val fixes = fd.getFixes(onFailId)

        assertNotNull(item)
        assertEquals("Full description", fullDescription)
        assertEquals("The order of called Botan methods is wrong.", shortDescription)
        assertEquals("https://www.codyze.io/explanations/10", helpUri)
        assertEquals(1, fixes?.size)
        assertEquals("Just fix it!", fixes?.get(0) ?: "")
    }
}
