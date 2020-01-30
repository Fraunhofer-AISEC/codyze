
package de.fraunhofer.aisec.crymlin;

import de.fraunhofer.aisec.analysis.structures.Finding;
import de.fraunhofer.aisec.analysis.structures.TYPESTATE_ANALYSIS;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderTestInterproc extends AbstractMarkTest {

	OrderTestInterproc() {
		TYPESTATEANALYSIS = TYPESTATE_ANALYSIS.WPDS;
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

		Set<Integer> startLineNumbers = findings.stream().map(f -> f.getRanges().get(0).getStart().getLine()).collect(Collectors.toSet());

		// Note that line numbers of the "range" are the actual line numbers -1. This is required for proper LSP->editor mapping
		assertTrue(startLineNumbers.contains(28));
		assertTrue(startLineNumbers.contains(30));
		assertTrue(startLineNumbers.contains(32));

	}

	@Test
	void testCppInterprocNOk2() throws Exception {
		@NonNull
		Set<Finding> findings = performTest("unittests/orderInterprocNOk2.cpp", "unittests/order2.mark");

		Set<Integer> startLineNumbers = findings.stream().map(f -> f.getRanges().get(0).getStart().getLine()).collect(Collectors.toSet());

		// Note that line numbers of the "range" are the actual line numbers -1. This is required for proper LSP->editor mapping
		assertTrue(startLineNumbers.contains(30));
	}
}
