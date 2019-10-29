
package de.fraunhofer.aisec.crymlin;

import de.fraunhofer.aisec.cpg.TranslationConfiguration;
import de.fraunhofer.aisec.cpg.TranslationManager;
import de.fraunhofer.aisec.cpg.TranslationResult;
import de.fraunhofer.aisec.crymlin.connectors.db.Database;
import de.fraunhofer.aisec.crymlin.connectors.db.OverflowDatabase;
import de.fraunhofer.aisec.crymlin.passes.StatementsPerMethodPass;
import de.fraunhofer.aisec.crymlin.server.AnalysisContext;
import de.fraunhofer.aisec.crymlin.server.AnalysisServer;
import de.fraunhofer.aisec.crymlin.server.ServerConfiguration;
import de.fraunhofer.aisec.crymlin.structures.Method;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

public class AnalysisServerQueriesTest {

	private static AnalysisServer server;
	private static TranslationResult result;

	@BeforeAll
	public static void startup() throws Exception {
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

		ClassLoader classLoader = AnalysisServerQueriesTest.class.getClassLoader();

		URL resource = classLoader.getResource("good/Bouncycastle.java");
		assertNotNull(resource);
		File javaFile = new File(resource.getFile());
		assertNotNull(javaFile);

		resource = classLoader.getResource("mark/PoC_MS1/Botan_AutoSeededRNG.mark");
		assertNotNull(resource);
		File markPoC1 = new File(resource.getFile());
		assertNotNull(markPoC1);
		String markModelFiles = markPoC1.getParent();

		// Start an analysis server
		server = AnalysisServer.builder()
				.config(
					ServerConfiguration.builder().launchConsole(false).launchLsp(false).markFiles(markModelFiles).build())
				.build();
		server.start();

		// Start the analysis
		TranslationManager translationManager = newJavaAnalysisRun(javaFile);
		CompletableFuture<TranslationResult> analyze = server.analyze(translationManager);
		try {
			result = analyze.get(5, TimeUnit.MINUTES);
		}
		catch (TimeoutException t) {
			analyze.cancel(true);
			throw t;
		}
	}

	@AfterAll
	public static void teardown() throws Exception {
		// Stop the analysis server
		server.stop();
	}

	/** Test analysis context - additional in-memory structures used for analysis. */
	@Test
	public void contextTest() {
		// Get analysis context from scratch
		AnalysisContext ctx = (AnalysisContext) AnalysisServerQueriesTest.result.getScratch().get("ctx");

		// We expect at least some methods
		assertNotNull(ctx);
		assertFalse(ctx.methods.isEmpty());
		Method meth = ctx.methods.entrySet().stream().findFirst().get().getValue();
		assertFalse(meth.getStatements().isEmpty());
	}

	/**
	 * Helper method for initializing an Analysis Run.
	 *
	 * @param sourceFiles
	 * @return
	 */
	private static TranslationManager newJavaAnalysisRun(File... sourceFiles) {
		return TranslationManager.builder()
				.config(
					TranslationConfiguration.builder()
							.debugParser(true)
							.failOnError(false)
							.defaultPasses()
							.registerPass(new StatementsPerMethodPass())
							.sourceFiles(
								sourceFiles)
							.build())
				.build();
	}
}
