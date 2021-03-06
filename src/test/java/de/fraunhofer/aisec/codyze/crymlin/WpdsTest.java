
package de.fraunhofer.aisec.codyze.crymlin;

import de.fraunhofer.aisec.codyze.analysis.Finding;
import de.fraunhofer.aisec.codyze.analysis.TypestateMode;
import de.fraunhofer.aisec.codyze.analysis.wpds.NFA;
import de.fraunhofer.aisec.codyze.analysis.wpds.NFATransition;
import de.fraunhofer.aisec.mark.XtextParser;
import de.fraunhofer.aisec.mark.markDsl.OrderExpression;
import de.fraunhofer.aisec.codyze.markmodel.fsm.FSM;
import de.fraunhofer.aisec.codyze.markmodel.fsm.StateNode;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for WPDS-based evaluation of order expressions.
 */
class WpdsTest extends AbstractMarkTest {

	WpdsTest() {
		// Using WPDS instead of NFA
		tsMode = TypestateMode.WPDS;
	}

	@Test
	void testRegexToNFA() {
		XtextParser parser = new XtextParser();
		parser.addMarkFile(new File("src/test/resources/unittests/nfa-test.mark"));
		OrderExpression expr = (OrderExpression) parser
				.parse()
				.values()
				.iterator()
				.next()
				.getRule()
				.get(0)
				.getStmt()
				.getEnsure()
				.getExp();
		expr = expr.getExp();

		// Use this implementation ...
		FSM fsm = new FSM();
		fsm.sequenceToFSM(expr);

		Set<StateNode> worklist = fsm.getStart();
		Set<String> edgesFSM = new HashSet<>();
		for (StateNode n : worklist) {
			edgesFSM.add("START.START -> " + n.getName()); // artificial node existing in the NFA
		}
		Set<StateNode> seen = new HashSet<>();
		while (!worklist.isEmpty()) {
			Set<StateNode> nextWorkList = new HashSet<>();
			for (StateNode n : worklist) {
				for (StateNode succ : n.getSuccessors()) {
					if (!seen.contains(succ)) {
						seen.add(succ);
						nextWorkList.add(succ);
					}
					edgesFSM.add(n.getName() + " -> " + succ.getName());
				}
			}
			worklist = nextWorkList;
		}

		// ... and that implementation ...
		NFA nfa = NFA.of(expr);
		Set<String> edgesNFA = new HashSet<>();
		for (NFATransition<StateNode> t : nfa.getTransitions()) {
			edgesNFA.add(t.getSource().getName() + " -> " + t.getTarget().getName());
		}

		System.out.println(fsm);

		System.out.println(nfa);
		// ... and make sure they deliver same results.
		for (String s : edgesFSM) {
			assertTrue(edgesNFA.contains(s), s + " found in FSM, but not in NFA-FSM");
		}
		for (String s : edgesNFA) {
			assertTrue(edgesFSM.contains(s), s + " found in NFA, but not in FSM");
		}
	}

	/**
	 * Test for issue 88.
	 *
	 * @throws Exception
	 */
	@Test
	void testCppRegression88() throws Exception {
		@NonNull
		Set<Finding> findings = performTest("unittests/regression88.cpp", "../../src/dist/mark/botan");

		// Note that line numbers of the "range" are the actual line numbers -1. This is required for proper LSP->editor mapping
		assertEquals(7, findings.stream().filter(Finding::isProblem).count());
	}

	@Test
	void testCppInterprocOk1() throws Exception {
		@NonNull
		Set<Finding> findings = performTest("unittests/orderInterprocOk1.cpp", "unittests/order2.mark");

		// Note that line numbers of the "range" are the actual line numbers -1. This is required for proper LSP->editor mapping
		assertEquals(0, findings.stream().filter(Finding::isProblem).count());
	}

	@Test
	void testCppInterprocOk1Legacy() throws Exception {
		@NonNull
		Set<Finding> findings = performTest("unittests/orderInterprocOk1.cpp", null, "unittests/order2.mark");

		// Note that line numbers of the "range" are the actual line numbers -1. This is required for proper LSP->editor mapping
		assertEquals(0, findings.stream().filter(Finding::isProblem).count());
	}

	@Test
	void testWpdsVector() throws Exception {

		@NonNull
		Set<Finding> findings = performTest("unittests/wpds-vector-example.java", "unittests/vector.mark");

		// Extract <line nr, isProblem> from findings
		Map<Integer, Boolean> startLineNumbers = findings.stream()
				.collect(Collectors.toMap(
					f -> f.getRegions().get(0).getStartLine(),
					Finding::isProblem,
					(isProblemA, isProblemB) -> {
						System.out.println("Several findings : " + isProblemA + "/" + isProblemB);
						return isProblemA && isProblemB;
					}));
	}

	@Test
	void testWpdsOK1() throws Exception {
		@NonNull
		Set<Finding> findings = performTest("unittests/wpds-ok1.cpp", "unittests/order2.mark");

		// Extract <line nr, isProblem> from findings
		Map<Integer, Boolean> startLineNumbers = findings.stream()
				.collect(Collectors.toMap(
					f -> f.getRegions().get(0).getStartLine(),
					Finding::isProblem,
					(isProblemA, isProblemB) -> {
						System.out.println("Several findings : " + isProblemA + "/" + isProblemB);
						return isProblemA && isProblemB;
					}));

		// Note that line numbers of the "range" are the actual line numbers -1. This is required for proper LSP->editor mapping
		assertTrue(startLineNumbers.containsKey(10)); // create
		assertFalse(startLineNumbers.get(10));
		assertTrue(startLineNumbers.containsKey(15)); // init
		assertFalse(startLineNumbers.get(15));
		assertTrue(startLineNumbers.containsKey(21)); // start
		assertFalse(startLineNumbers.get(21));
		assertTrue(startLineNumbers.containsKey(22)); // process
		assertFalse(startLineNumbers.get(22));
		assertTrue(startLineNumbers.containsKey(24)); // process
		assertFalse(startLineNumbers.get(24));
		assertTrue(startLineNumbers.containsKey(26)); // finish
		assertFalse(startLineNumbers.get(26));
	}

	@Test
	void testWpdsOK2() throws Exception {
		@NonNull
		Set<Finding> findings = performTest("unittests/wpds-ok2.cpp", "unittests/order2.mark");

		// Extract <line nr, isProblem> from findings
		Map<Integer, Boolean> startLineNumbers = findings.stream()
				.collect(Collectors.toMap(
					f -> f.getRegions().get(0).getStartLine(),
					Finding::isProblem,
					(isProblemA, isProblemB) -> {
						System.out.println("Several findings : " + isProblemA + "/" + isProblemB);
						return isProblemA && isProblemB;
					}));

		// Note that line numbers of the "range" are the actual line numbers -1. This is required for proper LSP->editor mapping
		assertTrue(startLineNumbers.containsKey(10)); // create
		assertFalse(startLineNumbers.get(10));
		assertTrue(startLineNumbers.containsKey(15)); // init
		assertFalse(startLineNumbers.get(15));
		assertTrue(startLineNumbers.containsKey(17)); // start
		assertFalse(startLineNumbers.get(17));
		assertTrue(startLineNumbers.containsKey(20)); // process
		assertFalse(startLineNumbers.get(20));
		assertTrue(startLineNumbers.containsKey(21)); // process
		assertFalse(startLineNumbers.get(21));
		assertTrue(startLineNumbers.containsKey(23)); // process
		assertFalse(startLineNumbers.get(23));
		assertTrue(startLineNumbers.containsKey(25)); // finish
		assertFalse(startLineNumbers.get(25));
	}

	@Test
	void testWpdsOk3() throws Exception {
		@NonNull
		Set<Finding> findings = performTest("unittests/wpds-ok3.cpp", "unittests/wpds-3.mark");

		// Extract <line nr, isProblem> from findings
		Map<Integer, Boolean> startLineNumbers = findings.stream()
				.collect(Collectors.toMap(
					f -> f.getRegions().get(0).getStartLine(),
					Finding::isProblem,
					(isProblemA, isProblemB) -> {
						System.out.println("Several findings : " + isProblemA + "/" + isProblemB);
						return isProblemA && isProblemB;
					}));

		// Note that line numbers of the "range" are the actual line numbers -1. This is required for proper LSP->editor mapping
		assertTrue(startLineNumbers.containsKey(9));
		assertFalse(startLineNumbers.get(9));
		assertTrue(startLineNumbers.containsKey(21));
		assertFalse(startLineNumbers.get(21));
		assertTrue(startLineNumbers.containsKey(24));
		assertFalse(startLineNumbers.get(24));
	}

	@Test
	//@Disabled // Disabled as if-branches are not yet correctly translated into WPDS rules
	void testWpdsOk4() throws Exception {
		@NonNull
		Set<Finding> findings = performTest("unittests/wpds-ok4.cpp", "unittests/wpds-4.mark");

		// Extract <line nr, isProblem> from findings
		Map<Integer, Boolean> startLineNumbers = findings.stream()
				.collect(Collectors.toMap(
					f -> f.getRegions().get(0).getStartLine(),
					Finding::isProblem,
					(isProblemA, isProblemB) -> {
						System.out.println("Several findings : " + isProblemA + "/" + isProblemB);
						return isProblemA && isProblemB;
					}));

		// Note that line numbers of the "range" are the actual line numbers -1. This is required for proper LSP->editor mapping
		assertTrue(startLineNumbers.containsKey(16));
		assertFalse(startLineNumbers.get(16));
		assertTrue(startLineNumbers.containsKey(12));
		assertFalse(startLineNumbers.get(12));
		assertTrue(startLineNumbers.containsKey(15));
		assertFalse(startLineNumbers.get(15));
		assertTrue(startLineNumbers.containsKey(13));
		assertFalse(startLineNumbers.get(13));
	}

	@Test
	void testWpdsNOK1() throws Exception {
		@NonNull
		Set<Finding> findings = performTest("unittests/wpds-nok1.cpp", "unittests/order2.mark");

		// Extract <line nr, isProblem> from findings
		Map<Integer, Boolean> startLineNumbers = findings.stream()
				.collect(Collectors.toMap(
					f -> f.getRegions().get(0).getStartLine(),
					Finding::isProblem,
					(isProblemA, isProblemB) -> {
						System.out.println("Several findings : " + isProblemA + "/" + isProblemB);
						return isProblemA && isProblemB;
					}));

		// Note that line numbers of the "range" are the actual line numbers -1. This is required for proper LSP->editor mapping
		assertTrue(startLineNumbers.containsKey(22)); // start
		assertTrue(startLineNumbers.get(22));
		assertTrue(startLineNumbers.containsKey(24)); // start
		assertTrue(startLineNumbers.get(24));
		//		assertTrue(startLineNumbers.containsKey(29)); // start
		//		assertTrue(startLineNumbers.get(29));
	}

	@Test
	void testCppInterprocNOk1() throws Exception {
		@NonNull
		Set<Finding> findings = performTest("unittests/orderInterprocNOk1.cpp", "unittests/order2.mark");

		// Extract <line nr, isProblem> from findings
		Map<Integer, Boolean> startLineNumbers = findings.stream()
				.collect(Collectors.toMap(
					f -> f.getRegions().get(0).getStartLine(),
					Finding::isProblem,
					(isProblemA, isProblemB) -> {
						System.out.println("Several findings : " + isProblemA + "/" + isProblemB);
						return isProblemA && isProblemB;
					}));

		// Note that line numbers of the "range" are the actual line numbers -1. This is required for proper LSP->editor mapping
		assertTrue(startLineNumbers.containsKey(28));
		assertTrue(startLineNumbers.get(28)); // isProblem
		assertTrue(startLineNumbers.containsKey(30));
		assertTrue(startLineNumbers.get(30)); // isProblem
		assertTrue(startLineNumbers.containsKey(32));
		assertTrue(startLineNumbers.get(32)); // isProblem
	}

	@Test
	void testCppInterprocNOk2() throws Exception {
		@NonNull
		Set<Finding> findings = performTest("unittests/orderInterprocNOk2.cpp", "unittests/order2.mark");

		Map<Integer, Boolean> startLineNumbers = findings.stream()
				.collect(Collectors.toMap(
					f -> f.getRegions().get(0).getStartLine(),
					Finding::isProblem,
					(isProblemA, isProblemB) -> {
						System.out.println("Several findings : " + isProblemA + "/" + isProblemB);
						return isProblemA && isProblemB;
					}));

		// Note that line numbers of the "range" are the actual line numbers -1. This is required for proper LSP->editor mapping
		assertTrue(startLineNumbers.containsKey(30));
		assertTrue(startLineNumbers.get(30)); // isProblem
	}

	@Test
	void testJavaMethodArgs() throws Exception {
		@NonNull
		Set<Finding> findings = performTest("java/jca/AESCBC.java", "../../src/dist/mark/bouncycastle");

		Map<Integer, Boolean> startLineNumbers = findings.stream()
				.collect(Collectors.toMap(
					f -> f.getRegions().get(0).getStartLine(),
					Finding::isProblem,
					(isProblemA, isProblemB) -> {
						System.out.println("Several findings : " + isProblemA + "/" + isProblemB);
						return isProblemA || isProblemB;
					}));

		// Note that line numbers of the "range" are the actual line numbers -1. This is required for proper LSP->editor mapping
		assertTrue(startLineNumbers.containsKey(10));
		assertTrue(startLineNumbers.get(10)); // isProblem
		assertTrue(startLineNumbers.containsKey(12));
		assertTrue(startLineNumbers.get(12)); // isProblem
		assertTrue(startLineNumbers.containsKey(13));
		assertTrue(startLineNumbers.get(13)); // isProblem
		assertTrue(startLineNumbers.containsKey(15));
		assertTrue(startLineNumbers.get(15)); // isProblem
	}

	@Test
	void testWpdsOpensslSimplified() throws Exception {
		@NonNull
		Set<Finding> findings = performTest("openssl/github.com/DaniloVlad/OpenSSL-AES/aes-simplified.c", "openssl/github.com/DaniloVlad/OpenSSL-AES/mark");

		// Extract <line nr, isProblem> from findings
		Map<Integer, Boolean> startLineNumbers = findings.stream()
				.collect(Collectors.toMap(
					f -> f.getRegions().isEmpty() ? -1 : f.getRegions().get(0).getStartLine(),
					Finding::isProblem,
					(isProblemA, isProblemB) -> {
						System.out.println("Several findings : " + isProblemA + "/" + isProblemB);
						return isProblemA && isProblemB;
					}));

		// Note that line numbers of the "range" are the actual line numbers -1. This is required for proper LSP->editor mapping
		assertFalse(startLineNumbers.get(10)); // EVP_CIPHER_CTX_new
		assertFalse(startLineNumbers.get(12)); // EVP_EncryptInit
		assertFalse(startLineNumbers.get(15)); // EVP_EncryptUpdate
		assertFalse(startLineNumbers.get(18)); // EVP_EncryptFinal
	}

}
