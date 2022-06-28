package de.fraunhofer.aisec.codyze.crymlin

import de.fraunhofer.aisec.codyze.analysis.Finding
import java.lang.Exception
import java.util.stream.Collectors
import kotlin.Throws
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.jupiter.api.*

internal class ForbiddenTest : AbstractMarkTest() {
    @Test
    @Throws(Exception::class)
    fun testJava() {
        val results = performTest("unittests/forbidden.java", "unittests/forbidden.mark")
        val findings =
            results.stream().map { obj: Finding? -> obj.toString() }.collect(Collectors.toSet())
        assertEquals(
            5,
            findings
                .stream()
                .filter { s: String -> s.contains("Violation against forbidden call") }
                .count()
        )
        assertTrue(
            findings.contains(
                "line 41: Violation against forbidden call(s) BotanF.set_key(_,_) in entity Forbidden. Call was b.set_key(nonce, iv);"
            )
        )
        assertTrue(
            findings.contains(
                "line 36: Violation against forbidden call(s) BotanF.start(nonce: int,_) in entity Forbidden. Call was b.start(nonce, b);"
            )
        )
        assertTrue(
            findings.contains(
                "line 35: Violation against forbidden call(s) BotanF.start() in entity Forbidden. Call was b.start();"
            )
        )
        assertTrue(
            findings.contains(
                "line 38: Violation against forbidden call(s) BotanF.start_msg(...) in entity Forbidden. Call was b.start_msg(nonce);"
            )
        )
        assertTrue(
            findings.contains(
                "line 39: Violation against forbidden call(s) BotanF.start_msg(...) in entity Forbidden. Call was b.start_msg(nonce, iv, b);"
            )
        )
    }

    @Test
    @Throws(Exception::class)
    fun testCpp() {
        performTest("unittests/forbidden.cpp")
    }
}
