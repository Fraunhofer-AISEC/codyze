package de.fraunhofer.aisec.codyze.crymlin

import de.fraunhofer.aisec.codyze.analysis.Finding
import java.lang.Exception
import java.util.stream.Collectors
import kotlin.Throws
import org.junit.jupiter.api.*

internal class OrderTestComplex : AbstractMarkTest() {
    @Throws(Exception::class)
    private fun performTestAndCheck(sourceFileName: String) {
        val results = performTest(sourceFileName, "unittests/order2.mark")
        val findings =
            results.stream().map { obj: Finding? -> obj.toString() }.collect(Collectors.toSet())
        Assertions.assertEquals(
            5,
            findings.stream().filter { s: String -> s.contains("Violation against Order") }.count()
        )
        Assertions.assertTrue(
            findings.contains(
                "line 53: Violation against Order: p5.init(); (initOp) is not allowed. Expected one of: cm.createOp (WrongUseOfBotan_CipherMode): The order of called Botan methods is wrong."
            )
        )
        Assertions.assertTrue(
            findings.contains(
                "line 68: Violation against Order: p6.reset(); (resetOp) is not allowed. Expected one of: cm.startOp (WrongUseOfBotan_CipherMode): The order of called Botan methods is wrong."
            )
        )
        Assertions.assertTrue(
            findings.contains(
                "line 62: Violation against Order: Base p6 is not correctly terminated. Expected one of [cm.startOp] to follow the correct last call on this base. (WrongUseOfBotan_CipherMode): The order of called Botan methods is wrong."
            )
        )
        Assertions.assertTrue(
            findings.contains(
                "line 80: Violation against Order: p6.reset(); (resetOp) is not allowed. Expected one of: cm.createOp (WrongUseOfBotan_CipherMode): The order of called Botan methods is wrong."
            )
        )
        Assertions.assertTrue(
            findings.contains(
                "line 74: Violation against Order: p6.create(); (createOp) is not allowed. Expected one of: END, cm.resetOp, cm.startOp (WrongUseOfBotan_CipherMode): The order of called Botan methods is wrong."
            )
        )
    }

    @Test
    @Throws(Exception::class)
    fun testJava() {
        performTestAndCheck("unittests/order2.java")
    }

    @Test
    @Throws(Exception::class)
    fun testCpp() {
        performTestAndCheck("unittests/order2.cpp")
    }
}
