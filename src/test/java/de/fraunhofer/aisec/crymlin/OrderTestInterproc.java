
package de.fraunhofer.aisec.crymlin;

import de.fraunhofer.aisec.analysis.structures.Finding;
import de.fraunhofer.aisec.analysis.structures.TypestateMode;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderTestInterproc extends AbstractMarkTest {

	OrderTestInterproc() {
		tsMode = TypestateMode.WPDS;
	}

	@Test
	void testCppInterprocOk1() throws Exception {
		@NonNull
		Set<Finding> findings = performTest("unittests/orderInterprocOk1.cpp", "unittests/order2.mark");

		// Note that line numbers of the "range" are the actual line numbers -1. This is required for proper LSP->editor mapping
		assertEquals(0, findings.stream().filter(Finding::isProblem).count());
	}

	@Test
	void testCppInterprocNOk1() throws Exception {
		@NonNull
		Set<Finding> findings = performTest("unittests/orderInterprocNOk1.cpp", "unittests/order2.mark");

		// Extract <line nr, isProblem> from findings
		Map<Integer, Boolean> startLineNumbers = findings.stream()
														  .collect(Collectors.toMap(
																  f -> f.getRanges().get(0).getStart().getLine(),
																  f -> f.isProblem()
														  ));

		// Note that line numbers of the "range" are the actual line numbers -1. This is required for proper LSP->editor mapping
		assertTrue(startLineNumbers.containsKey(28));
		assertTrue(startLineNumbers.get(28));  // isProblem
		assertTrue(startLineNumbers.containsKey(30));
		assertTrue(startLineNumbers.get(30));  // isProblem
		assertTrue(startLineNumbers.containsKey(32));
		assertTrue(startLineNumbers.get(32));  // isProblem
	}

	@Test
	void testCppInterprocNOk2() throws Exception {
		@NonNull
		Set<Finding> findings = performTest("unittests/orderInterprocNOk2.cpp", "unittests/order2.mark");

		Map<Integer, Boolean> startLineNumbers = findings.stream()
														 .collect(Collectors.toMap(
																 f -> f.getRanges().get(0).getStart().getLine(),
																 f -> f.isProblem()
														 ));

		// Note that line numbers of the "range" are the actual line numbers -1. This is required for proper LSP->editor mapping
		assertTrue(startLineNumbers.containsKey(30));
		assertTrue(startLineNumbers.get(30));  // isProblem
	}
}
