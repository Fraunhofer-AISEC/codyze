
package de.fraunhofer.aisec.crymlin;

import de.fraunhofer.aisec.analysis.structures.Finding;
import de.fraunhofer.aisec.cpg.TranslationConfiguration;
import de.fraunhofer.aisec.cpg.TranslationManager;
import de.fraunhofer.aisec.cpg.TranslationResult;
import de.fraunhofer.aisec.crymlin.connectors.db.Database;
import de.fraunhofer.aisec.crymlin.connectors.db.OverflowDatabase;
import de.fraunhofer.aisec.analysis.structures.AnalysisContext;
import de.fraunhofer.aisec.analysis.server.AnalysisServer;
import de.fraunhofer.aisec.analysis.structures.ServerConfiguration;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

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
				"line 68: Violation against Order: Base p6 is not correctly terminated. Expected one of [cm.startOp] to follow the correct last call on this base. (WrongUseOfBotan_CipherMode): The order of called Botan methods is wrong."));
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
