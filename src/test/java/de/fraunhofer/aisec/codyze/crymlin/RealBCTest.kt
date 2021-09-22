package de.fraunhofer.aisec.codyze.crymlin

import de.fraunhofer.aisec.codyze.analysis.Finding
import java.lang.Exception
import java.util.stream.Collectors
import kotlin.Throws
import kotlin.test.assertEquals
import org.junit.jupiter.api.*

// at least it breaks consistently now
internal class RealBCTest : AbstractMarkTest() {
    @Test
    @Throws(Exception::class)
    fun testSimple() {
        // Just a very simple test of a source file found in the wild.
        val findings =
            performTest(
                "real-examples/bc/rwedoff.Password-Manager/Main.java",
                "real-examples/bc/rwedoff.Password-Manager/"
            )
        for (f in findings) {
            println("  ->" + f.identifier + " " + f.regions[0] + " " + f.logMsg)
        }

        // We expect three (positive) verification of the algorithm used
        val wrongAlgorithmsFindings =
            findings
                .stream()
                .filter { f: Finding? -> f!!.identifier == "Wrong_Algorithm" }
                .collect(Collectors.toSet())
        assertEquals(3, wrongAlgorithmsFindings.size) // 3 in total
        assertEquals(
            0,
            wrongAlgorithmsFindings
                .stream()
                .filter { obj: Finding? -> obj!!.isProblem }
                .collect(Collectors.toSet())
                .size
        ) // None is a violation

        // We expect three correct usages of SecureRandom
        val correctOrderSecureRandom =
            findings
                .stream()
                .filter { f: Finding? -> f!!.logMsg == "Verified Order: SecureRandomOrder" }
                .collect(Collectors.toSet())
        assertEquals(3, correctOrderSecureRandom.size) // 3 in total
        assertEquals(
            0,
            correctOrderSecureRandom
                .stream()
                .filter { obj: Finding? -> obj!!.isProblem }
                .collect(Collectors.toSet())
                .size
        ) // None is a violation

        // We expect three correct usages of SHA 512
        val correctOrderSha512 =
            findings
                .stream()
                .filter { f: Finding? -> f!!.logMsg == "Verified Order: SHA512DigestOrder" }
                .collect(Collectors.toSet())
        assertEquals(3, correctOrderSha512.size) // 3 in total
        assertEquals(
            0,
            correctOrderSha512
                .stream()
                .filter { obj: Finding? -> obj!!.isProblem }
                .collect(Collectors.toSet())
                .size
        ) // None is a violation
    }
}
