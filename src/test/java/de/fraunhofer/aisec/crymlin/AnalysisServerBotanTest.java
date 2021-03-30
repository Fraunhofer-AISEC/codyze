
package de.fraunhofer.aisec.crymlin;

import de.fraunhofer.aisec.analysis.server.AnalysisServer;
import de.fraunhofer.aisec.analysis.structures.AnalysisContext;
import de.fraunhofer.aisec.analysis.structures.ServerConfiguration;
import de.fraunhofer.aisec.cpg.TranslationConfiguration;
import de.fraunhofer.aisec.cpg.TranslationManager;
import de.fraunhofer.aisec.markmodel.MEntity;
import de.fraunhofer.aisec.markmodel.MRule;
import de.fraunhofer.aisec.markmodel.Mark;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

public class AnalysisServerBotanTest {

	private static AnalysisServer server;
	private static AnalysisContext result;

	@BeforeAll
	public static void startup() throws Exception {

		ClassLoader classLoader = AnalysisServerBotanTest.class.getClassLoader();

		URL resource = classLoader.getResource("mark_cpp/symm_block_cipher.cpp");
		assertNotNull(resource);
		File cppFile = new File(resource.getFile());
		assertNotNull(cppFile);

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

		// Start the analysis (BOTAN Symmetric Example by Oliver)
		TranslationManager translationManager = newAnalysisRun(cppFile);
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

	/** Test analysis context - additional in-memory structures used for analysis. */
	@Test
	void contextTest() {
		// Get analysis context from scratch
		AnalysisContext ctx = AnalysisServerBotanTest.result;

		// We expect no methods (as there is no class)
		assertNotNull(ctx);
		assertTrue(ctx.methods.isEmpty());
	}

	@Test
	void markModelTest() {
		Mark markModel = server.getMarkModel();
		assertNotNull(markModel);
		List<MRule> rules = markModel.getRules();
		assertEquals(7, rules.size());

		Collection<MEntity> ents = markModel.getEntities();
		assertEquals(9, ents.size());
	}

	@Test
	void markEvaluationTest() {
		AnalysisContext ctx = AnalysisServerBotanTest.result;
		assertNotNull(ctx);
		List<String> findings = new ArrayList<>();
		assertNotNull(ctx.getFindings());
		ctx.getFindings().forEach(x -> findings.add(x.toString()));

		System.out.println("Findings");
		for (String finding : findings) {
			System.out.println(finding);
		}
	}

	/**
	 * Helper method for initializing an Analysis Run.
	 *
	 * @param sourceLocations
	 * @return
	 */
	private static TranslationManager newAnalysisRun(File... sourceLocations) {
		return TranslationManager.builder()
				.config(
					TranslationConfiguration.builder().debugParser(true).failOnError(false).defaultPasses().sourceLocations(sourceLocations).build())
				.build();
	}
}
