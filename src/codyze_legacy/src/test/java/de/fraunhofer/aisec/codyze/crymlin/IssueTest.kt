package de.fraunhofer.aisec.codyze.crymlin

import java.lang.Exception
import kotlin.Throws
import org.junit.jupiter.api.Test

class IssueTest : AbstractMarkTest() {

    @Test
    @Throws(Exception::class)
    fun issue219() {
        val findings = performTest("legacy/issues/219/Main.java", "legacy/issues/219/")

        expected(findings, "line 6: Rule JCAProvider_PBEParameterSpec_2 violated")
    }
}
