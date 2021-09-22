package de.fraunhofer.aisec.codyze.crymlin

import org.junit.jupiter.api.Test

internal class CTest : AbstractMarkTest() {
    @Test
    @Throws(Exception::class)
    fun testDefinition() {
        val findings = performTest("c/def.c", "c/s.mark")
        containsFindings(findings, "line 14: Rule some_s verified")
    }

    @Test
    @Throws(Exception::class)
    fun testDeclAssign() {
        val findings = performTest("c/declassign.c", "c/s.mark")
        containsFindings(findings, "line 16: Rule some_s verified")
    }
}
