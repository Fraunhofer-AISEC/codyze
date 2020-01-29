
package de.fraunhofer.aisec.crymlin;

import de.fraunhofer.aisec.analysis.structures.Finding;
import de.fraunhofer.aisec.cpg.TranslationConfiguration;
import de.fraunhofer.aisec.cpg.TranslationManager;
import de.fraunhofer.aisec.cpg.TranslationResult;
import de.fraunhofer.aisec.cpg.passes.VariableUsageResolver;
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

class ForbiddenTest extends AbstractMarkTest {

	@Test
	void testJava() throws Exception {
		Set<Finding> results = performTest(
			"unittests/forbidden.java", "unittests/forbidden.mark");

		Set<String> findings = results.stream()
				.map(f -> f.toString())
				.collect(Collectors.toSet());
		assertEquals(
			5, findings.stream().filter(s -> s.contains("Violation against forbidden call")).count());

		assertTrue(
			findings.contains(
				"line 41: Violation against forbidden call(s) BotanF.set_key(_,_) in entity Forbidden. Call was b.set_key(nonce, iv);"));
		assertTrue(
			findings.contains(
				"line 36: Violation against forbidden call(s) BotanF.start(nonce: int,_) in entity Forbidden. Call was b.start(nonce, b);"));
		assertTrue(
			findings.contains(
				"line 35: Violation against forbidden call(s) BotanF.start() in entity Forbidden. Call was b.start();"));
		assertTrue(
			findings.contains(
				"line 38: Violation against forbidden call(s) BotanF.start_msg(...) in entity Forbidden. Call was b.start_msg(nonce);"));
		assertTrue(
			findings.contains(
				"line 39: Violation against forbidden call(s) BotanF.start_msg(...) in entity Forbidden. Call was b.start_msg(nonce, iv, b);"));
	}

	@Test
	void testCpp() throws Exception {
		performTest("unittests/forbidden.cpp");
	}
}
