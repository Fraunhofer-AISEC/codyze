
package de.fraunhofer.aisec.crymlin;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import de.fraunhofer.aisec.cpg.TranslationConfiguration;
import de.fraunhofer.aisec.cpg.TranslationManager;
import de.fraunhofer.aisec.cpg.TranslationResult;
import de.fraunhofer.aisec.crymlin.connectors.db.Database;
import de.fraunhofer.aisec.crymlin.connectors.db.OverflowDatabase;
import de.fraunhofer.aisec.crymlin.server.AnalysisContext;
import de.fraunhofer.aisec.crymlin.server.AnalysisServer;
import de.fraunhofer.aisec.crymlin.server.ServerConfiguration;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.Test;

class OrderTest {

	private void performTest(String sourceFileName) throws Exception {
		ClassLoader classLoader = AnalysisServerBotanTest.class.getClassLoader();

		URL resource = classLoader.getResource(sourceFileName);
		assertNotNull(resource);
		File cppFile = new File(resource.getFile());
		assertNotNull(cppFile);

		resource = classLoader.getResource("unittests/order.mark");
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

		// Start the analysis
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

		assertEquals(6, findings.stream().filter(s -> s.contains("Violation against Order")).count());

		assertTrue(findings.contains("line 44: Violation against Order: p4.start(iv); (start) is not allowed. Expected one of: END (WrongUseOfBotan_CipherMode)"));
		// assertTrue(
		//    findings.contains(
		//        "line 45: Violation against Order: p4.finish(buf); is not allowed. Base contains
		// errors already. (WrongUseOfBotan_CipherMode)"));
		assertTrue(
			findings.contains("line 29: Violation against Order: p3.finish(buf); (finish) is not allowed. Expected one of: cm.start (WrongUseOfBotan_CipherMode)"));
		assertTrue(findings.contains("line 13: Violation against Order: p.set_key(key); (init) is not allowed. Expected one of: cm.start (WrongUseOfBotan_CipherMode)"));
		// assertTrue(
		//    findings.contains(
		//        "line 14: Violation against Order: p.start(iv.bits_of()); is not allowed. Base
		// contains errors already. (WrongUseOfBotan_CipherMode)"));
		// assertTrue(
		//    findings.contains(
		//        "line 15: Violation against Order: p.finish(buf); is not allowed. Base contains errors
		// already. (WrongUseOfBotan_CipherMode)"));
		// assertTrue(
		//    findings.contains(
		//        "line 17: Violation against Order: p.set_key(key); is not allowed. Base contains
		// errors already. (WrongUseOfBotan_CipherMode)"));
		assertTrue(findings.contains(
			"line 21: Violation against Order: Base p2 is not correctly terminated. Expected one of [cm.finish] to follow the correct last call on this base. (WrongUseOfBotan_CipherMode)"));
		assertTrue(
			findings.contains("line 55: Violation against Order: p5.finish(buf); (finish) is not allowed. Expected one of: cm.start (WrongUseOfBotan_CipherMode)"));
		assertTrue(findings.contains(
			"line 51: Violation against Order: Base p5 is not correctly terminated. Expected one of [cm.finish] to follow the correct last call on this base. (WrongUseOfBotan_CipherMode)"));

		// Stop the analysis server
		server.stop();
	}

	@Test
	void checkJava() throws Exception {
		performTest("unittests/order.java");
	}

	@Test
	void checkCpp() throws Exception {
		performTest("unittests/order.cpp");
	}
}
