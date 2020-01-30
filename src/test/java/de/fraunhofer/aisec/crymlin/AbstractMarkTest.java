
package de.fraunhofer.aisec.crymlin;

import de.fraunhofer.aisec.analysis.server.AnalysisServer;
import de.fraunhofer.aisec.analysis.structures.AnalysisContext;
import de.fraunhofer.aisec.analysis.structures.Finding;
import de.fraunhofer.aisec.analysis.structures.ServerConfiguration;
import de.fraunhofer.aisec.analysis.structures.TYPESTATE_ANALYSIS;
import de.fraunhofer.aisec.cpg.TranslationConfiguration;
import de.fraunhofer.aisec.cpg.TranslationManager;
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

public class AbstractMarkTest {

	protected TranslationManager translationManager;
	protected AnalysisServer server;
	protected AnalysisContext ctx;
	protected TYPESTATE_ANALYSIS TYPESTATEANALYSIS = TYPESTATE_ANALYSIS.NFA;

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

		// Start an analysis server
		server = AnalysisServer.builder()
				.config(
					ServerConfiguration.builder()
							.launchConsole(false)
							.launchLsp(false)
							.typestateAnalysis(TYPESTATEANALYSIS)
							.markFiles(markDirPath)
							.build())
				.build();
		server.start();

		translationManager = TranslationManager.builder()
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
		CompletableFuture<AnalysisContext> analyze = server.analyze(translationManager);
		try {
			ctx = analyze.get(5, TimeUnit.MINUTES);
		}
		catch (TimeoutException t) {
			analyze.cancel(true);
			throw t;
		}

		assertNotNull(ctx);
		assertTrue(ctx.methods.isEmpty());

		for (Finding s : ctx.getFindings()) {
			System.out.println(s);
		}

		return ctx.getFindings();
	}

}
