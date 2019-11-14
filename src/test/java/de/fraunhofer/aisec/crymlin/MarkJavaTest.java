
package de.fraunhofer.aisec.crymlin;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import de.fraunhofer.aisec.cpg.TranslationConfiguration;
import de.fraunhofer.aisec.cpg.TranslationManager;
import de.fraunhofer.aisec.cpg.TranslationResult;
import de.fraunhofer.aisec.crymlin.connectors.db.OverflowDatabase;
import de.fraunhofer.aisec.crymlin.server.AnalysisContext;
import de.fraunhofer.aisec.crymlin.server.AnalysisServer;
import de.fraunhofer.aisec.crymlin.server.ServerConfiguration;
import de.fraunhofer.aisec.crymlin.structures.Finding;
import java.io.File;
import java.net.URL;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MarkJavaTest {

	@BeforeEach
	public void clearDatabase() {
		// Make sure we start with a clean (and connected) db
		try {
			OverflowDatabase.getInstance().purgeDatabase();
		}
		catch (Throwable e) {
			e.printStackTrace();
			assumeFalse(true); // Assumption for this test not fulfilled. Do not fail but bail.
		}
	}

	@Test
	public void split_1() throws Exception {
		runTest("simplesplit_splitstring");
	}

	@Test
	public void is_instance_1() throws Exception {
		// fixme disabled until is_instance is implemented
		// runTest("simple_instancestring");
	}

	private void runTest(@NonNull String fileNamePart)
			throws ExecutionException, InterruptedException, TimeoutException {
		String type = fileNamePart.substring(fileNamePart.lastIndexOf('_') + 1);

		ClassLoader classLoader = MarkJavaTest.class.getClassLoader();
		URL resource = classLoader.getResource("mark_java/" + fileNamePart + ".java");
		assertNotNull(resource);
		File cppFile = new File(resource.getFile());
		assertNotNull(cppFile);

		resource = classLoader.getResource("mark_java/" + type + ".mark");
		assertNotNull(resource);
		File markFile = new File(resource.getFile());
		assertNotNull(markFile);

		// Start an analysis server
		AnalysisServer server = AnalysisServer.builder()
				.config(
					ServerConfiguration.builder().launchConsole(false).launchLsp(false).markFiles(markFile.getAbsolutePath()).build())
				.build();
		server.start();

		// Start the analysis
		TranslationManager translationManager = TranslationManager.builder()
				.config(
					TranslationConfiguration.builder().debugParser(true).failOnError(false).codeInNodes(true).defaultPasses().sourceFiles(cppFile).build())
				.build();
		CompletableFuture<TranslationResult> analyze = server.analyze(translationManager);
		try {
			TranslationResult result = analyze.get(5, TimeUnit.MINUTES);

			AnalysisContext ctx = (AnalysisContext) result.getScratch().get("ctx");
			assertNotNull(ctx.getFindings());
			Set<Finding> findings = ctx.getFindings();

			assertEquals(1, findings.size());
			findings.forEach((f) -> {
				System.out.println(f.toString());
			});
		}
		catch (TimeoutException t) {
			analyze.cancel(true);
			throw t;
		}
	}
}
