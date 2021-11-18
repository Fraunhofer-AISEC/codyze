package de.fraunhofer.aisec.codyze.crymlin

import de.fraunhofer.aisec.codyze.analysis.Finding
import de.fraunhofer.aisec.codyze.analysis.FindingDescription
import de.fraunhofer.aisec.codyze.analysis.SarifInstantiator
import de.fraunhofer.aisec.cpg.sarif.Region
import de.fraunhofer.aisec.mark.markDsl.Action
import java.io.File
import java.net.URI
import kotlin.test.assertNotNull
import org.junit.jupiter.api.Test

internal class SarifInstantiatorTest {
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
        val f = Finding(id, Action.FAIL, logMsg, artifactUri, regions, isProblem)

        // Initialize database with explanations
        val fd = FindingDescription.instance
        val url = this.javaClass.classLoader.getResource("unittests/findingDescription.json")

        assertNotNull(url)

        fd.init(File(url.toURI()))

        s.pushRun(setOf(f))

        println(s.toString())
    }
}
