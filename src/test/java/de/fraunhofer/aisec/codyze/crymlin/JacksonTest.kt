package de.fraunhofer.aisec.codyze.crymlin

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class JacksonTest : AbstractMarkTest() {
    @Test
    @Throws(Exception::class)
    fun testBasic() {
        val findings = performTest("java/jackson/Serialization.java", "mark/jackson/")
        Assertions.assertNotNull(findings)
        Assertions.assertEquals(1, findings.size)
        val finding = findings.iterator().next()
        Assertions.assertEquals(9, finding.locations[0].region.startLine)
        Assertions.assertEquals("FORBIDDEN", finding.onfailIdentifier)
    }
}
