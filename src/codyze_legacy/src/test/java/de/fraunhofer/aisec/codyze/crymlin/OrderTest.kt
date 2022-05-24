package de.fraunhofer.aisec.codyze.crymlin

import de.fraunhofer.aisec.codyze.analysis.Finding
import java.lang.Exception
import java.util.stream.Collectors
import kotlin.Throws
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.jupiter.api.*

internal class OrderTest : AbstractMarkTest() {
    @Test
    @Throws(Exception::class)
    fun checkJava() {
        val results = performTest("legacy/unittests/order.java", "legacy/unittests/order.mark")
        val findings =
            results.stream().map { obj: Finding? -> obj.toString() }.collect(Collectors.toSet())
        check(findings)
    }

    @Test
    @Throws(Exception::class)
    fun checkCpp() {
        val results = performTest("legacy/unittests/order.cpp", "legacy/unittests/order.mark")
        val findings =
            results.stream().map { obj: Finding? -> obj.toString() }.collect(Collectors.toSet())
        check(findings)
    }

    private fun check(findings: Set<String>) {
        assertTrue(
            findings.contains(
                "line 48: Violation against Order: p4.start(iv); (start) is not allowed. Expected one of: END (WrongUseOfBotan_CipherMode): The order of called Botan methods is wrong."
            )
        )
        assertTrue(
            findings.contains(
                "line 31: Violation against Order: p3.finish(buf); (finish) is not allowed. Expected one of: cm.start (WrongUseOfBotan_CipherMode): The order of called Botan methods is wrong."
            )
        )
        assertTrue(
            findings.contains(
                "line 13: Violation against Order: p.set_key(key); (init) is not allowed. Expected one of: cm.start (WrongUseOfBotan_CipherMode): The order of called Botan methods is wrong."
            )
        )
        assertTrue(
            findings.contains(
                "line 22: Violation against Order: Base p2 is not correctly terminated. Expected one of [cm.finish] to follow the correct last call on this base. (WrongUseOfBotan_CipherMode): The order of called Botan methods is wrong."
            )
        )
        assertTrue(
            findings.contains(
                "line 60: Violation against Order: p5.finish(buf); (finish) is not allowed. Expected one of: cm.start (WrongUseOfBotan_CipherMode): The order of called Botan methods is wrong."
            )
        )
        assertTrue(
            findings.contains(
                "line 56: Violation against Order: Base p5 is not correctly terminated. Expected one of [cm.finish] to follow the correct last call on this base. (WrongUseOfBotan_CipherMode): The order of called Botan methods is wrong."
            )
        )
        assertTrue(
            findings.contains(
                "line 66: Violation against Order: Base p2 is not correctly terminated. Expected one of [cm.finish] to follow the correct last call on this base. (WrongUseOfBotan_CipherMode): The order of called Botan methods is wrong."
            )
        )
        assertEquals(
            7,
            findings.stream().filter { s: String -> s.contains("Violation against Order") }.count()
        )
    }
}
