package de.fraunhofer.aisec.codyze.crymlin

import de.fraunhofer.aisec.codyze.analysis.Finding
import de.fraunhofer.aisec.cpg.ExperimentalGraph
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.declarations.VariableDeclaration
import java.util.stream.Collectors
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

internal class RealBotanTest : AbstractMarkTest() {
    @OptIn(ExperimentalGraph::class)
    @Test
    @Throws(Exception::class)
    fun testSimple() {
        // Just a very simple test to explore the graph
        val findings =
            performTest(
                "real-examples/botan/streamciphers/bsex.cpp",
                "real-examples/botan/streamciphers/bsex.mark"
            )
        val graph = ctx!!.graph
        val variables =
            graph
                .nodes
                .stream()
                .filter { node: Node? -> node is VariableDeclaration }
                .collect(Collectors.toList())
        assertTrue(variables.size > 0)
    }

    @Test
    @Throws(Exception::class)
    fun testCorrecKeySize() {
        val findings =
            performTest(
                "real-examples/botan/streamciphers/bsex.cpp",
                "real-examples/botan/streamciphers/bsex.mark"
            )

        // Note that line numbers of the "range" are the actual line numbers -1. This is required
        // for proper LSP->editor mapping
        val correctKeyLength =
            findings
                .stream()
                .filter { f: Finding? -> f!!.onfailIdentifier == "CorrectPrivateKeyLength" }
                .findFirst()
        assertTrue(correctKeyLength.isPresent)
        assertFalse(correctKeyLength.get().isProblem)
    }

    @Test
    @Throws(Exception::class)
    fun testWrongKeySize() {
        val findings =
            performTest(
                "real-examples/botan/streamciphers/bsex.cpp",
                "real-examples/botan/streamciphers/bsex.mark"
            )

        // Note that line numbers of the "range" are the actual line numbers -1. This is required
        // for proper LSP->editor mapping
        val wrongKeyLength =
            findings
                .stream()
                .filter { f: Finding? -> f!!.onfailIdentifier == "WrongPrivateKeyLength" }
                .findFirst()
        assertTrue(wrongKeyLength.isPresent)
        assertTrue(wrongKeyLength.get().isProblem)
    }

    @Test
    @Disabled("WIP, not working yet")
    @Throws(Exception::class)
    fun testPlaybookCreator() {
        val findings =
            performTest(
                "real-examples/botan/blockciphers/obraunsdorf.playbook-creator/pbcStorage.cpp",
                "real-examples/botan/MARK"
            )
        assertTrue(findings.isEmpty())
    }

    @Test
    @Throws(Exception::class)
    fun testArsenic() {
        val findings =
            performTest(
                "real-examples/botan/blockciphers/Antidote1911.Arsenic/Crypto.cpp",
                "real-examples/botan/MARK"
            )

        // We expect a correct key size in line 250 and 355
        val correctKeySizes =
            findings.stream().filter { f: Finding? -> !f!!.isProblem }.collect(Collectors.toList())
        assertTrue(
            correctKeySizes.stream().anyMatch { f: Finding? -> f!!.regions[0].startLine == 249 }
        )
        assertTrue(
            correctKeySizes.stream().anyMatch { f: Finding? -> f!!.regions[0].startLine == 354 }
        )
        assertEquals(2, correctKeySizes.size)
    }

    @Test
    @Throws(Exception::class)
    fun testQt_LockBox() {
        val findings =
            performTest(
                "real-examples/botan/blockciphers/Prudkovskiy.Qt_LockBox/crypto.cpp",
                "real-examples/botan/MARK"
            )

        // We expect two correct block ciphers AES/CBC (isProblem == false) at line 16 and 22
        val blockCiphers =
            findings
                .stream()
                .filter { f: Finding? -> f!!.onfailIdentifier == "WrongBlockCipher" }
                .filter { f: Finding? -> !f!!.isProblem }
                .collect(Collectors.toList())
        assertTrue(blockCiphers.stream().anyMatch { f: Finding? -> f!!.regions[0].startLine == 15 })
        assertTrue(blockCiphers.stream().anyMatch { f: Finding? -> f!!.regions[0].startLine == 21 })
        assertEquals(2, blockCiphers.size)

        // We expect an incorrect key size at line 16 and 22 because it is not explicitly set.
        val keyLengths =
            findings
                .stream()
                .filter { f: Finding? -> f!!.onfailIdentifier == "BadKeyLength" }
                .filter { obj: Finding? -> obj!!.isProblem }
                .collect(Collectors.toList())
        assertEquals(2, keyLengths.size)
        assertTrue(keyLengths.stream().anyMatch { l: Finding? -> l!!.regions[0].startLine == 15 })
        assertTrue(keyLengths.stream().anyMatch { l: Finding? -> l!!.regions[0].startLine == 21 })
    }
}
