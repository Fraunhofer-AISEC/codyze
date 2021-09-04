package de.fraunhofer.aisec.codyze.crymlin

import de.fraunhofer.aisec.codyze.analysis.Finding
import de.fraunhofer.aisec.codyze.analysis.TypestateMode
import de.fraunhofer.aisec.codyze.analysis.wpds.NFA
import de.fraunhofer.aisec.codyze.markmodel.fsm.FSM
import de.fraunhofer.aisec.codyze.markmodel.fsm.StateNode
import de.fraunhofer.aisec.mark.XtextParser
import de.fraunhofer.aisec.mark.markDsl.OrderExpression
import java.io.*
import java.lang.Exception
import java.util.HashSet
import java.util.function.BinaryOperator
import java.util.function.Function
import java.util.stream.Collectors
import kotlin.Throws
import org.junit.jupiter.api.*

/** Tests for WPDS-based evaluation of order expressions. */
internal class WpdsTest : AbstractMarkTest() {
    @Test
    fun testRegexToNFA() {
        val parser = XtextParser()
        parser.addMarkFile(File("src/test/resources/unittests/nfa-test.mark"))
        var expr =
            parser.parse().values.iterator().next().rule[0].stmt.ensure.exp as OrderExpression
        expr = expr.exp

        // Use this implementation ...
        val fsm = FSM()
        fsm.sequenceToFSM(expr)
        var worklist = fsm.start
        val edgesFSM: MutableSet<String> = HashSet()
        for (n in worklist) {
            edgesFSM.add("START.START -> " + n.name) // artificial node existing in the NFA
        }
        val seen: MutableSet<StateNode> = HashSet()
        while (!worklist.isEmpty()) {
            val nextWorkList: MutableSet<StateNode> = HashSet()
            for (n in worklist) {
                for (succ in n.successors) {
                    if (!seen.contains(succ)) {
                        seen.add(succ)
                        nextWorkList.add(succ)
                    }
                    edgesFSM.add(n.name + " -> " + succ.name)
                }
            }
            worklist = nextWorkList
        }

        // ... and that implementation ...
        val nfa = NFA.of(expr)
        val edgesNFA: MutableSet<String> = HashSet()
        for (t in nfa.transitions) {
            edgesNFA.add(t.source.name + " -> " + t.target.name)
        }
        println(fsm)
        println(nfa)
        // ... and make sure they deliver same results.
        for (s in edgesFSM) {
            Assertions.assertTrue(edgesNFA.contains(s), "$s found in FSM, but not in NFA-FSM")
        }
        for (s in edgesNFA) {
            Assertions.assertTrue(edgesFSM.contains(s), "$s found in NFA, but not in FSM")
        }
    }

    /**
     * Test for issue 88.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testCppRegression88() {
        val findings = performTest("unittests/regression88.cpp", "../../src/dist/mark/botan")

        // Note that line numbers of the "range" are the actual line numbers -1. This is required
        // for proper LSP->editor mapping
        Assertions.assertEquals(
            7,
            findings.stream().filter { obj: Finding? -> obj!!.isProblem }.count()
        )
    }

    @Test
    @Throws(Exception::class)
    fun testCppInterprocOk1() {
        val findings = performTest("unittests/orderInterprocOk1.cpp", "unittests/order2.mark")

        // Note that line numbers of the "range" are the actual line numbers -1. This is required
        // for proper LSP->editor mapping
        Assertions.assertEquals(
            0,
            findings.stream().filter { obj: Finding? -> obj!!.isProblem }.count()
        )
    }

    @Test
    @Throws(Exception::class)
    fun testCppInterprocOk1Legacy() {
        val findings = performTest("unittests/orderInterprocOk1.cpp", null, "unittests/order2.mark")

        // Note that line numbers of the "range" are the actual line numbers -1. This is required
        // for proper LSP->editor mapping
        Assertions.assertEquals(
            0,
            findings.stream().filter { obj: Finding? -> obj!!.isProblem }.count()
        )
    }

    @Test
    @Throws(Exception::class)
    fun testWpdsVector() {
        val findings = performTest("unittests/wpds-vector-example.java", "unittests/vector.mark")

        // Extract <line nr, isProblem> from findings
        val startLineNumbers =
            findings
                .stream()
                .collect(
                    Collectors.toMap(
                        Function { f: Finding? -> f!!.regions[0].startLine },
                        Function { obj: Finding? -> obj!!.isProblem },
                        BinaryOperator { isProblemA: Boolean, isProblemB: Boolean ->
                            println("Several findings : $isProblemA/$isProblemB")
                            isProblemA && isProblemB
                        }
                    )
                )
    }

    @Test
    @Throws(Exception::class)
    fun testWpdsOK1() {
        val findings = performTest("unittests/wpds-ok1.cpp", "unittests/order2.mark")

        // Extract <line nr, isProblem> from findings
        val startLineNumbers =
            findings
                .stream()
                .collect(
                    Collectors.toMap(
                        Function { f: Finding? -> f!!.regions[0].startLine },
                        Function { obj: Finding? -> obj!!.isProblem },
                        BinaryOperator { isProblemA: Boolean, isProblemB: Boolean ->
                            println("Several findings : $isProblemA/$isProblemB")
                            isProblemA && isProblemB
                        }
                    )
                )

        // Note that line numbers of the "range" are the actual line numbers -1. This is required
        // for proper LSP->editor mapping
        Assertions.assertTrue(startLineNumbers.containsKey(10)) // create
        Assertions.assertFalse(startLineNumbers[10]!!)
        Assertions.assertTrue(startLineNumbers.containsKey(15)) // init
        Assertions.assertFalse(startLineNumbers[15]!!)
        Assertions.assertTrue(startLineNumbers.containsKey(21)) // start
        Assertions.assertFalse(startLineNumbers[21]!!)
        Assertions.assertTrue(startLineNumbers.containsKey(22)) // process
        Assertions.assertFalse(startLineNumbers[22]!!)
        Assertions.assertTrue(startLineNumbers.containsKey(24)) // process
        Assertions.assertFalse(startLineNumbers[24]!!)
        Assertions.assertTrue(startLineNumbers.containsKey(26)) // finish
        Assertions.assertFalse(startLineNumbers[26]!!)
    }

    @Test
    @Throws(Exception::class)
    fun testWpdsOK2() {
        val findings = performTest("unittests/wpds-ok2.cpp", "unittests/order2.mark")

        // Extract <line nr, isProblem> from findings
        val startLineNumbers =
            findings
                .stream()
                .collect(
                    Collectors.toMap(
                        Function { f: Finding? -> f!!.regions[0].startLine },
                        Function { obj: Finding? -> obj!!.isProblem },
                        BinaryOperator { isProblemA: Boolean, isProblemB: Boolean ->
                            println("Several findings : $isProblemA/$isProblemB")
                            isProblemA && isProblemB
                        }
                    )
                )

        // Note that line numbers of the "range" are the actual line numbers -1. This is required
        // for proper LSP->editor mapping
        Assertions.assertTrue(startLineNumbers.containsKey(10)) // create
        Assertions.assertFalse(startLineNumbers[10]!!)
        Assertions.assertTrue(startLineNumbers.containsKey(15)) // init
        Assertions.assertFalse(startLineNumbers[15]!!)
        Assertions.assertTrue(startLineNumbers.containsKey(17)) // start
        Assertions.assertFalse(startLineNumbers[17]!!)
        Assertions.assertTrue(startLineNumbers.containsKey(20)) // process
        Assertions.assertFalse(startLineNumbers[20]!!)
        Assertions.assertTrue(startLineNumbers.containsKey(21)) // process
        Assertions.assertFalse(startLineNumbers[21]!!)
        Assertions.assertTrue(startLineNumbers.containsKey(23)) // process
        Assertions.assertFalse(startLineNumbers[23]!!)
        Assertions.assertTrue(startLineNumbers.containsKey(25)) // finish
        Assertions.assertFalse(startLineNumbers[25]!!)
    }

    @Test
    @Throws(Exception::class)
    fun testWpdsOk3() {
        val findings = performTest("unittests/wpds-ok3.cpp", "unittests/wpds-3.mark")

        // Extract <line nr, isProblem> from findings
        val startLineNumbers =
            findings
                .stream()
                .collect(
                    Collectors.toMap(
                        Function { f: Finding? -> f!!.regions[0].startLine },
                        Function { obj: Finding? -> obj!!.isProblem },
                        BinaryOperator { isProblemA: Boolean, isProblemB: Boolean ->
                            println("Several findings : $isProblemA/$isProblemB")
                            isProblemA && isProblemB
                        }
                    )
                )

        // Note that line numbers of the "range" are the actual line numbers -1. This is required
        // for proper LSP->editor mapping
        Assertions.assertTrue(startLineNumbers.containsKey(9))
        Assertions.assertFalse(startLineNumbers[9]!!)
        Assertions.assertTrue(startLineNumbers.containsKey(21))
        Assertions.assertFalse(startLineNumbers[21]!!)
        Assertions.assertTrue(startLineNumbers.containsKey(24))
        Assertions.assertFalse(startLineNumbers[24]!!)
    }

    @Test
    @Throws(Exception::class)
    fun // @Disabled // Disabled as if-branches are not yet correctly translated into WPDS rules
    testWpdsOk4() {
        val findings = performTest("unittests/wpds-ok4.cpp", "unittests/wpds-4.mark")

        // Extract <line nr, isProblem> from findings
        val startLineNumbers =
            findings
                .stream()
                .collect(
                    Collectors.toMap(
                        Function { f: Finding? -> f!!.regions[0].startLine },
                        Function { obj: Finding? -> obj!!.isProblem },
                        BinaryOperator { isProblemA: Boolean, isProblemB: Boolean ->
                            println("Several findings : $isProblemA/$isProblemB")
                            isProblemA && isProblemB
                        }
                    )
                )

        // Note that line numbers of the "range" are the actual line numbers -1. This is required
        // for proper LSP->editor mapping
        Assertions.assertTrue(startLineNumbers.containsKey(16))
        Assertions.assertFalse(startLineNumbers[16]!!)
        Assertions.assertTrue(startLineNumbers.containsKey(12))
        Assertions.assertFalse(startLineNumbers[12]!!)
        Assertions.assertTrue(startLineNumbers.containsKey(15))
        Assertions.assertFalse(startLineNumbers[15]!!)
        Assertions.assertTrue(startLineNumbers.containsKey(13))
        Assertions.assertFalse(startLineNumbers[13]!!)
    }

    @Test
    @Throws(Exception::class)
    fun testWpdsNOK1() {
        val findings = performTest("unittests/wpds-nok1.cpp", "unittests/order2.mark")

        // Extract <line nr, isProblem> from findings
        val startLineNumbers =
            findings
                .stream()
                .collect(
                    Collectors.toMap(
                        Function { f: Finding? -> f!!.regions[0].startLine },
                        Function { obj: Finding? -> obj!!.isProblem },
                        BinaryOperator { isProblemA: Boolean, isProblemB: Boolean ->
                            println("Several findings : $isProblemA/$isProblemB")
                            isProblemA && isProblemB
                        }
                    )
                )

        // Note that line numbers of the "range" are the actual line numbers -1. This is required
        // for proper LSP->editor mapping
        Assertions.assertTrue(startLineNumbers.containsKey(22)) // start
        Assertions.assertTrue(startLineNumbers[22]!!)
        Assertions.assertTrue(startLineNumbers.containsKey(24)) // start
        Assertions.assertTrue(startLineNumbers[24]!!)
        //		assertTrue(startLineNumbers.containsKey(29)); // start
        //		assertTrue(startLineNumbers.get(29));
    }

    @Test
    @Throws(Exception::class)
    fun testCppInterprocNOk1() {
        val findings = performTest("unittests/orderInterprocNOk1.cpp", "unittests/order2.mark")

        // Extract <line nr, isProblem> from findings
        val startLineNumbers =
            findings
                .stream()
                .collect(
                    Collectors.toMap(
                        Function { f: Finding? -> f!!.regions[0].startLine },
                        Function { obj: Finding? -> obj!!.isProblem },
                        BinaryOperator { isProblemA: Boolean, isProblemB: Boolean ->
                            println("Several findings : $isProblemA/$isProblemB")
                            isProblemA && isProblemB
                        }
                    )
                )

        // Note that line numbers of the "range" are the actual line numbers -1. This is required
        // for proper LSP->editor mapping
        Assertions.assertTrue(startLineNumbers.containsKey(28))
        Assertions.assertTrue(startLineNumbers[28]!!) // isProblem
        Assertions.assertTrue(startLineNumbers.containsKey(30))
        Assertions.assertTrue(startLineNumbers[30]!!) // isProblem
        Assertions.assertTrue(startLineNumbers.containsKey(32))
        Assertions.assertTrue(startLineNumbers[32]!!) // isProblem
    }

    @Test
    @Throws(Exception::class)
    fun testCppInterprocNOk2() {
        val findings = performTest("unittests/orderInterprocNOk2.cpp", "unittests/order2.mark")
        val startLineNumbers =
            findings
                .stream()
                .collect(
                    Collectors.toMap(
                        Function { f: Finding? -> f!!.regions[0].startLine },
                        Function { obj: Finding? -> obj!!.isProblem },
                        BinaryOperator { isProblemA: Boolean, isProblemB: Boolean ->
                            println("Several findings : $isProblemA/$isProblemB")
                            isProblemA && isProblemB
                        }
                    )
                )

        // Note that line numbers of the "range" are the actual line numbers -1. This is required
        // for proper LSP->editor mapping
        Assertions.assertTrue(startLineNumbers.containsKey(30))
        Assertions.assertTrue(startLineNumbers[30]!!) // isProblem
    }

    @Test
    @Throws(Exception::class)
    fun testJavaMethodArgs() {
        val findings = performTest("java/jca/AESCBC.java", "../../src/dist/mark/bouncycastle")
        val startLineNumbers =
            findings
                .stream()
                .collect(
                    Collectors.toMap(
                        Function { f: Finding? -> f!!.regions[0].startLine },
                        Function { obj: Finding? -> obj!!.isProblem },
                        BinaryOperator { isProblemA: Boolean, isProblemB: Boolean ->
                            println("Several findings : $isProblemA/$isProblemB")
                            isProblemA || isProblemB
                        }
                    )
                )

        // Note that line numbers of the "range" are the actual line numbers -1. This is required
        // for proper LSP->editor mapping
        Assertions.assertTrue(startLineNumbers.containsKey(10))
        Assertions.assertTrue(startLineNumbers[10]!!) // isProblem
        Assertions.assertTrue(startLineNumbers.containsKey(12))
        Assertions.assertTrue(startLineNumbers[12]!!) // isProblem
        Assertions.assertTrue(startLineNumbers.containsKey(13))
        Assertions.assertTrue(startLineNumbers[13]!!) // isProblem
        Assertions.assertTrue(startLineNumbers.containsKey(15))
        Assertions.assertTrue(startLineNumbers[15]!!) // isProblem
    }

    @Test
    @Throws(Exception::class)
    fun testWpdsOpensslSimplified() {
        val findings =
            performTest(
                "openssl/github.com/DaniloVlad/OpenSSL-AES/aes-simplified.c",
                "openssl/github.com/DaniloVlad/OpenSSL-AES/mark"
            )

        // Extract <line nr, isProblem> from findings
        val startLineNumbers =
            findings
                .stream()
                .collect(
                    Collectors.toMap(
                        Function { f: Finding? ->
                            if (f!!.regions.isEmpty()) -1 else f.regions[0].startLine
                        },
                        Function { obj: Finding? -> obj!!.isProblem },
                        BinaryOperator { isProblemA: Boolean, isProblemB: Boolean ->
                            println("Several findings : $isProblemA/$isProblemB")
                            isProblemA && isProblemB
                        }
                    )
                )

        // Note that line numbers of the "range" are the actual line numbers -1. This is required
        // for proper LSP->editor mapping
        Assertions.assertFalse(startLineNumbers[10]!!) // EVP_CIPHER_CTX_new
        Assertions.assertFalse(startLineNumbers[12]!!) // EVP_EncryptInit
        Assertions.assertFalse(startLineNumbers[15]!!) // EVP_EncryptUpdate
        Assertions.assertFalse(startLineNumbers[18]!!) // EVP_EncryptFinal
    }

    init {
        // Using WPDS instead of NFA
        tsMode = TypestateMode.WPDS
    }
}
