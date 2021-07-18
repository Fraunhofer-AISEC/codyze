
package de.fraunhofer.aisec.codyze.crymlin;

import de.fraunhofer.aisec.codyze.analysis.cpgpasses.EdgeCachePass;
import de.fraunhofer.aisec.codyze.analysis.cpgpasses.IdentifierPass;
import de.fraunhofer.aisec.codyze.analysis.server.AnalysisServer;
import de.fraunhofer.aisec.codyze.analysis.structures.AnalysisContext;
import de.fraunhofer.aisec.codyze.analysis.structures.Finding;
import de.fraunhofer.aisec.codyze.analysis.structures.ServerConfiguration;
import de.fraunhofer.aisec.codyze.analysis.structures.TypestateMode;
import de.fraunhofer.aisec.cpg.TranslationConfiguration;
import de.fraunhofer.aisec.cpg.TranslationManager;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public abstract class AbstractMarkTest {

	protected TranslationManager translationManager;
	protected AnalysisServer server;
	protected AnalysisContext ctx;
	protected TypestateMode tsMode = TypestateMode.NFA;

	protected Set<Finding> performTest(String sourceFileName) throws Exception {
		return performTest(sourceFileName, null);
	}

	@NonNull
	protected Set<Finding> performTest(String sourceFileName, @Nullable String markFileName) throws Exception {
		return performTest(sourceFileName, null, markFileName);
	}

	@NonNull
	protected Set<Finding> performTest(String sourceFileName, String[] additionalFiles, @Nullable String markFileName) throws Exception {
		ClassLoader classLoader = AbstractMarkTest.class.getClassLoader();

		URL resource = classLoader.getResource(sourceFileName);
		assertNotNull(resource, "Resource " + sourceFileName + " not found");
		File javaFile = new File(resource.getFile());
		assertNotNull(javaFile, "File " + sourceFileName + " not found");

		ArrayList<File> toAnalyze = new ArrayList<>();
		toAnalyze.add(javaFile);

		if (additionalFiles != null) {
			for (String s : additionalFiles) {
				resource = classLoader.getResource(s);
				assertNotNull(resource, "Resource " + s + " not found");
				javaFile = new File(resource.getFile());
				assertNotNull(javaFile, "File " + s + " not found");
				toAnalyze.add(javaFile);
			}
		}

		String markDirPath = "";
		if (markFileName != null) {
			resource = classLoader.getResource(markFileName);

			if (resource == null) {
				// Assume `markFileName` is relative to project base `src` folder
				Path p = Path.of(classLoader.getResource(".").toURI()).resolve(Path.of("..", "..", "..", "src")).resolve(markFileName).normalize();
				resource = p.toUri().toURL();
			}

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
							.typestateAnalysis(tsMode)
							.markFiles(markDirPath)
							.useLegacyEvaluator()
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
							.defaultLanguages()
							.registerPass(new IdentifierPass())
							.registerPass(new EdgeCachePass())
							.loadIncludes(true)
							.sourceLocations(toAnalyze.toArray(new File[0]))
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

		for (Finding s : ctx.getFindings()) {
			System.out.println(s);
		}

		return ctx.getFindings();
	}

	protected void expected(Set<Finding> findings, String... expectedFindings) {
		System.out.println("All findings:");
		for (Finding f : findings) {
			System.out.println(f.toString());
		}

		for (String expected : expectedFindings) {
			assertEquals(1, findings.stream().filter(f -> f.toString().equals(expected)).count(), "not found: \"" + expected + "\"");
			Optional<Finding> first = findings.stream().filter(f -> f.toString().equals(expected)).findFirst();
			findings.remove(first.get());
		}
		if (findings.size() > 0) {
			System.out.println("Additional Findings:");
			for (Finding f : findings) {
				System.out.println(f.toString());
			}
		}

		assertEquals(0, findings.size(), findings.stream().map(Finding::toString).collect(Collectors.joining()));
	}

	/**
	 * Verifies that a set of findings contains at least the given expected findings.
	 * @param findings A set of findings to check.
	 * @param expectedFindings A set of expected findings.
	 */
	protected void containsFindings(@NonNull Set<Finding> findings, String... expectedFindings) {
		System.out.println("All findings:");
		for (Finding f : findings)
			System.out.println(f.toString());

		Set<String> missingFindings = new HashSet<String>();
		for (String expected : expectedFindings) {
			boolean found = false;
			for (Finding finding : findings) {
				if (expected.equals(finding.toString())) {
					found = true;
					break;
				}
			}
			if (!found) {
				missingFindings.add(expected);
			}
		}
		if (!missingFindings.isEmpty()) {
			System.out.println("Missing findings:");
			for (String missing : missingFindings) {
				System.out.println(missing);
			}
		}
		assertTrue(missingFindings.isEmpty());
	}

}
