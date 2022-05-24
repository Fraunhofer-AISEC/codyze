package de.fraunhofer.aisec.codyze.crymlin

import de.fraunhofer.aisec.codyze.analysis.utils.Utils
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.sarif.PhysicalLocation
import de.fraunhofer.aisec.cpg.sarif.Region
import java.net.URI
import kotlin.test.*
import org.junit.jupiter.api.Test

internal class UtilsTest {
    @Test
    fun getRegionTest() {
        val node = Node()
        val resultNoLocation = Utils.getRegion(node)
        assertEquals(-1, resultNoLocation.endColumn, "endColumn was not equal")
        assertEquals(-1, resultNoLocation.startColumn, "startColumn was not equal")
        assertEquals(-1, resultNoLocation.endLine, "endLine was not equal")
        assertEquals(-1, resultNoLocation.startLine, "startLine was not equal")

        val region = Region(0, 0, 0, 0)
        node.location = PhysicalLocation(URI("0"), region)
        val resultWithLocation = Utils.getRegion(node)
        assertEquals(0, resultWithLocation.endColumn, "endColumn was not equal")
        assertEquals(0, resultWithLocation.startColumn, "startColumn was not equal")
        assertEquals(0, resultWithLocation.endLine, "endLine was not equal")
        assertEquals(0, resultWithLocation.startLine, "startLine was not equal")
    }

    @Test
    fun stripQuotedCharacterTest() {
        assertEquals("s", Utils.stripQuotedCharacter("\'s\'"))
        assertEquals("s\'", Utils.stripQuotedCharacter("s\'"))
        assertEquals("\'s", Utils.stripQuotedCharacter("\'s"))
        assertEquals("s", Utils.stripQuotedCharacter("s"))
    }

    @Test
    fun extractMethodNameTest() {
        assertEquals("cppMethod", Utils.extractMethodName("CppClass::cppMethod"))
        assertEquals("method", Utils.extractMethodName("class->method"))
        assertEquals("javaMethod", Utils.extractMethodName("package.JavaClass.javaMethod"))
        assertEquals("typo:method", Utils.extractMethodName("typo:method"))
    }
}
