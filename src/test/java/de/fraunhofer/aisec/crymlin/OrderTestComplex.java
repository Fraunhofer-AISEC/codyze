
package de.fraunhofer.aisec.crymlin;

import de.fraunhofer.aisec.analysis.structures.Finding;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderTestComplex extends AbstractMarkTest {

	private void performTestAndCheck(String sourceFileName) throws Exception {

		Set<Finding> results = performTest(sourceFileName, "unittests/order2.mark");
		Set<String> findings = results.stream()
				.map(Finding::toString)
				.collect(Collectors.toSet());

		assertEquals(5, findings.stream().filter(s -> s.contains("Violation against Order")).count());

		assertTrue(
			findings.contains(
				"line 53: Violation against Order: p5.init(); (initOp) is not allowed. Expected one of: cm.createOp (WrongUseOfBotan_CipherMode): The order of called Botan methods is wrong."));
		assertTrue(
			findings.contains(
				"line 68: Violation against Order: p6.reset(); (resetOp) is not allowed. Expected one of: cm.startOp (WrongUseOfBotan_CipherMode): The order of called Botan methods is wrong."));
		assertTrue(
			findings.contains(
				"line 62: Violation against Order: Base p6 is not correctly terminated. Expected one of [cm.startOp] to follow the correct last call on this base. (WrongUseOfBotan_CipherMode): The order of called Botan methods is wrong."));
		assertTrue(
			findings.contains(
				"line 80: Violation against Order: p6.reset(); (resetOp) is not allowed. Expected one of: cm.createOp (WrongUseOfBotan_CipherMode): The order of called Botan methods is wrong."));
		assertTrue(
			findings.contains(
				"line 74: Violation against Order: p6.create(); (createOp) is not allowed. Expected one of: END, cm.resetOp, cm.startOp (WrongUseOfBotan_CipherMode): The order of called Botan methods is wrong."));

		server.stop();
	}

	@Test
	void testJava() throws Exception {
		performTestAndCheck("unittests/order2.java");
	}

	@Test
	void testCpp() throws Exception {
		performTestAndCheck("unittests/order2.cpp");
	}
}
