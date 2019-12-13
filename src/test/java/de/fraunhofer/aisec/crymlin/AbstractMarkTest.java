
package de.fraunhofer.aisec.crymlin;

import de.fraunhofer.aisec.analysis.server.AnalysisServer;
import de.fraunhofer.aisec.analysis.structures.AnalysisContext;
import de.fraunhofer.aisec.analysis.structures.Finding;
import de.fraunhofer.aisec.analysis.structures.ServerConfiguration;
import de.fraunhofer.aisec.analysis.structures.TYPESTATE_ANALYSIS;
import de.fraunhofer.aisec.cpg.TranslationConfiguration;
import de.fraunhofer.aisec.cpg.TranslationManager;
import de.fraunhofer.aisec.cpg.TranslationResult;
import de.fraunhofer.aisec.crymlin.connectors.db.Database;
import de.fraunhofer.aisec.crymlin.connectors.db.OverflowDatabase;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.net.URL;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

public class AbstractMarkTest {

	Set<Finding> performTest(String sourceFileName) throws Exception {
		return performTest(sourceFileName, null);
	}

	@NonNull
	Set<Finding> performTest(String sourceFileName, @Nullable String markFileName) throws Exception {
		ClassLoader classLoader = RealBCTest.class.getClassLoader();

		URL resource = classLoader.getResource(sourceFileName);
		assertNotNull(resource);
		File javaFile = new File(resource.getFile());
		assertNotNull(javaFile);

		String markDirPath = "";
		if (markFileName != null) {
			resource = classLoader.getResource(markFileName);
			assertNotNull(resource);
			File markDir = new File(resource.getFile());
			assertNotNull(markDir);
			markDirPath = markDir.getAbsolutePath();
		}

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
		AnalysisServer server = AnalysisServer.builder()
				.config(
					ServerConfiguration.builder()
							.launchConsole(false)
							.launchLsp(false)
							.typestateAnalysis(TYPESTATE_ANALYSIS.NFA)
							.markFiles(markDirPath)
							.build())
				.build();
		server.start();

		TranslationManager translationManager = TranslationManager.builder()
				.config(
					TranslationConfiguration.builder()
							.debugParser(true)
							.failOnError(false)
							.codeInNodes(true)
							.defaultPasses()
							.loadIncludes(true)
							.sourceFiles(javaFile)
							.build())
				.build();
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

		for (Finding s : ctx.getFindings()) {
			System.out.println(s);
		}

		return ctx.getFindings();
	}

}
