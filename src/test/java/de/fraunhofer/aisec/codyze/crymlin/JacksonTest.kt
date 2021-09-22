package de.fraunhofer.aisec.codyze.crymlin

import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import org.junit.jupiter.api.Test

internal class JacksonTest : AbstractMarkTest() {
    @Test
    @Throws(Exception::class)
    fun testBasic() {
        val findings = performTest("java/jackson/Serialization.java", "mark/jackson/")
        assertNotNull(findings)
        assertEquals(1, findings.size)

        val finding = findings.iterator().next()

        assertEquals(9, finding.locations[0].region.startLine)
        assertEquals("FORBIDDEN", finding.onfailIdentifier)
    }
}
