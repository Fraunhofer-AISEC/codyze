package de.fraunhofer.aisec.codyze.crymlin

import de.fraunhofer.aisec.codyze.analysis.Finding
import java.lang.Exception
import kotlin.Throws
import kotlin.test.assertEquals
import org.junit.jupiter.api.*

internal class MarkJavaTest : AbstractMarkTest() {
    @Test
    @Throws(Exception::class)
    fun split_1() {
        val findings =
            performTest(
                "legacy/mark_java/simplesplit_splitstring.java",
                "legacy/mark_java/splitstring.mark"
            )
        println("All findings:")
        for (f in findings) {
            println(f.toString())
        }
        val expectedFindings =
            arrayOf(
                "line 23: Rule SPLIT_FIRSTELEMENT_EQUALS_AES violated",
                "line 14: Rule SPLIT_FIRSTELEMENT_EQUALS_AES verified",
                "line 14: Rule SPLIT_SECONDELEMENT_EQUALS_FIRST violated",
                "line 23: Rule SPLIT_SECONDELEMENT_EQUALS_FIRST verified"
            )
        for (expected in expectedFindings) {
            assertEquals(
                1,
                findings.stream().filter { f: Finding? -> f.toString() == expected }.count(),
                "not found: \"$expected\""
            )
            val first =
                findings.stream().filter { f: Finding? -> f.toString() == expected }.findFirst()
            findings.remove(first.get())
        }
        if (findings.size > 0) {
            println("Additional Findings:")
            for (f in findings) {
                println(f.toString())
            }
        }
        assertEquals(0, findings.size)
    }

    @Test
    @Throws(Exception::class)
    fun is_instance_1() {
        val findings =
            performTest(
                "legacy/mark_java/simple_instancestring.java",
                "legacy/mark_java/instancestring.mark"
            )
        println("All findings:")
        for (f in findings) {
            println(f.toString())
        }
        val expectedFindings =
            arrayOf("line 12: Rule HasBeenCalled verified", "line 15: Rule HasBeenCalled verified")
        for (expected in expectedFindings) {
            assertEquals(
                1,
                findings.stream().filter { f: Finding? -> f.toString() == expected }.count(),
                "not found: \"$expected\""
            )
            val first =
                findings.stream().filter { f: Finding? -> f.toString() == expected }.findFirst()
            findings.remove(first.get())
        }
        if (findings.size > 0) {
            println("Additional Findings:")
            for (f in findings) {
                println(f.toString())
            }
        }
        assertEquals(0, findings.size)
    }

    @Test
    @Throws(Exception::class)
    fun const_value() {
        val findings = performTest("legacy/mark_java/const.java", "legacy/mark_java/const.mark")

        // todo: missing: Enum is not handled yet
        expected(
            findings,
            "line 11: Rule Enum violated",
            "line [17, 3]: Rule Static verified",
            "line [15, 3]: Rule Static verified"
        )
    }
}
