
package de.fraunhofer.aisec.crymlin;

import de.fraunhofer.aisec.analysis.cpgpasses.EdgeCachePass;
import de.fraunhofer.aisec.analysis.cpgpasses.IdentifierPass;
import de.fraunhofer.aisec.analysis.cpgpasses.StatementsPerMethodPass;
import de.fraunhofer.aisec.analysis.server.AnalysisServer;
import de.fraunhofer.aisec.analysis.structures.AnalysisContext;
import de.fraunhofer.aisec.analysis.structures.Method;
import de.fraunhofer.aisec.analysis.structures.ServerConfiguration;
import de.fraunhofer.aisec.cpg.TranslationConfiguration;
import de.fraunhofer.aisec.cpg.TranslationManager;
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

class AnalysisServerQueriesTest {

	private static AnalysisServer server;
	private static AnalysisContext result;

	@BeforeAll
	public static void startup() throws Exception {
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
					ServerConfiguration.builder().disableOverflow(true).launchConsole(false).launchLsp(false).markFiles(markModelFiles).build())
				.build();
		server.start();

		// Start the analysis
		TranslationManager translationManager = newJavaAnalysisRun(javaFile);
		CompletableFuture<AnalysisContext> analyze = server.analyze(translationManager);
		try {
			result = analyze.get(5, TimeUnit.MINUTES);
		}
		catch (TimeoutException t) {
			analyze.cancel(true);
			throw t;
		}
	}

	@AfterAll
	public static void teardown() {
		// Stop the analysis server
		server.stop();
	}

	/**
	 * Test analysis context - additional in-memory structures used for analysis.
	 */
	@Test
	void contextTest() {
		// Get analysis context from scratch
		AnalysisContext ctx = AnalysisServerQueriesTest.result;

		// We expect at least some methods
		assertNotNull(ctx);
		assertFalse(ctx.methods.isEmpty());
		Method meth = ctx.methods.entrySet().stream().findFirst().get().getValue();
		assertFalse(meth.getStatements().isEmpty());
	}

	/**
	 * Helper method for initializing an Analysis Run.
	 *
	 * @param sourceLocations
	 * @return
	 */
	private static TranslationManager newJavaAnalysisRun(File... sourceLocations) {
		return TranslationManager.builder()
				.config(
					TranslationConfiguration.builder()
							.debugParser(true)
							.failOnError(false)
							.defaultPasses()
							.defaultLanguages()
							.registerPass(new IdentifierPass())
							.registerPass(new EdgeCachePass())
							.registerPass(new StatementsPerMethodPass())
							.sourceLocations(
								sourceLocations)
							.build())
				.build();
	}
}
