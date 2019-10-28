
package de.fraunhofer.aisec.crymlin;

import de.fraunhofer.aisec.cpg.TranslationConfiguration;
import de.fraunhofer.aisec.cpg.TranslationManager;
import de.fraunhofer.aisec.cpg.TranslationResult;
import de.fraunhofer.aisec.crymlin.connectors.db.Database;
import de.fraunhofer.aisec.crymlin.connectors.db.OverflowDatabase;
import de.fraunhofer.aisec.crymlin.server.AnalysisContext;
import de.fraunhofer.aisec.crymlin.server.AnalysisServer;
import de.fraunhofer.aisec.crymlin.server.ServerConfiguration;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

class OrderTestComplex {

	private void performTest(String sourceFileName) throws Exception {
		ClassLoader classLoader = AnalysisServerBotanTest.class.getClassLoader();

		URL resource = classLoader.getResource(sourceFileName);
		assertNotNull(resource);
		File cppFile = new File(resource.getFile());
		assertNotNull(cppFile);

		resource = classLoader.getResource("unittests/order2.mark");
		assertNotNull(resource);
		File markPoC1 = new File(resource.getFile());
		assertNotNull(markPoC1);

		// Make sure we start with a clean (and connected) db
		try {
			Database db = OverflowDatabase.getInstance();
			db.connect();
			db.purgeDatabase();
		}
		catch (Throwable e) {
			e.printStackTrace();
			assumeFalse(true); // Assumption for this test not fulfilled. Do not fail but bail.
		}

		// Start an analysis server
		AnalysisServer server = AnalysisServer.builder().config(
			ServerConfiguration.builder().launchConsole(false).launchLsp(false).markFiles(markPoC1.getAbsolutePath()).build()).build();
		server.start();

		TranslationManager translationManager = TranslationManager.builder().config(
			TranslationConfiguration.builder().debugParser(true).failOnError(false).codeInNodes(true).defaultPasses().sourceFiles(cppFile).build()).build();
		CompletableFuture<TranslationResult> analyze = server.analyze(translationManager);
		TranslationResult result;
		try {
			result = analyze.get(5, TimeUnit.MINUTES);
		}
		catch (TimeoutException t) {
			analyze.cancel(true);
			throw t;
		}

		AnalysisContext ctx = (AnalysisContext) result.getScratch().get("ctx");
		assertNotNull(ctx);
		assertTrue(ctx.methods.isEmpty());

		List<String> findings = new ArrayList<>();
		assertNotNull(ctx.getFindings());
		ctx.getFindings().forEach(x -> findings.add(x.toString()));

		for (String s : findings) {
			System.out.println(s);
		}

		assertEquals(5, findings.stream().filter(s -> s.contains("Violation against Order")).count());

		assertTrue(
			findings.contains(
				"line 53: Violation against Order: p5.init(); (init) is not allowed. Expected one of: cm.create (WrongUseOfBotan_CipherMode)"));
		// assertTrue(
		//    findings.contains(
		//        "line 54: Violation against Order: p5.start(); is not allowed. Base contains errors
		// already. (WrongUseOfBotan_CipherMode)"));
		// assertTrue(
		//    findings.contains(
		//        "line 55: Violation against Order: p5.process(); is not allowed. Base contains errors
		// already. (WrongUseOfBotan_CipherMode)"));
		// assertTrue(
		//    findings.contains(
		//        "line 56: Violation against Order: p5.finish(); is not allowed. Base contains errors
		// already. (WrongUseOfBotan_CipherMode)"));
		assertTrue(
			findings.contains(
				"line 68: Violation against Order: p6.reset(); (reset) is not allowed. Expected one of: cm.start (WrongUseOfBotan_CipherMode)"));
		assertTrue(
			findings.contains(
				"line 68: Violation against Order: Base p6 is not correctly terminated. Expected one of [cm.start] to follow the correct last call on this base. (WrongUseOfBotan_CipherMode)"));
		assertTrue(
			findings.contains(
				"line 80: Violation against Order: p6.reset(); (reset) is not allowed. Expected one of: cm.create (WrongUseOfBotan_CipherMode)"));
		assertTrue(
			findings.contains(
				"line 74: Violation against Order: p6.create(); (create) is not allowed. Expected one of: END, cm.reset, cm.start (WrongUseOfBotan_CipherMode)"));

		server.stop();
	}

	@Test
	void testJava() throws Exception {
		performTest("unittests/order2.java");
	}

	@Test
	void testCpp() throws Exception {
		performTest("unittests/order2.cpp");
	}
}
