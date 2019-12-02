
package de.fraunhofer.aisec.crymlin;

import de.fraunhofer.aisec.cpg.TranslationConfiguration;
import de.fraunhofer.aisec.cpg.TranslationManager;
import de.fraunhofer.aisec.cpg.TranslationResult;
import de.fraunhofer.aisec.cpg.graph.CallExpression;
import de.fraunhofer.aisec.cpg.graph.VariableDeclaration;
import de.fraunhofer.aisec.crymlin.connectors.db.Database;
import de.fraunhofer.aisec.crymlin.connectors.db.OverflowDatabase;
import de.fraunhofer.aisec.analysis.structures.AnalysisContext;
import de.fraunhofer.aisec.analysis.server.AnalysisServer;
import de.fraunhofer.aisec.analysis.structures.ServerConfiguration;
import de.fraunhofer.aisec.analysis.structures.TYPESTATE_ANALYSIS;
import de.fraunhofer.aisec.analysis.structures.Finding;
import de.fraunhofer.aisec.crymlin.dsl.CrymlinTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

class RealBotanTest {

	private @NonNull Set<Finding> performTest(String sourceFileName, String markFileName) throws Exception {
		ClassLoader classLoader = AnalysisServerBotanTest.class.getClassLoader();

		URL resource = classLoader.getResource(sourceFileName);
		assertNotNull(resource);
		File cppFile = new File(resource.getFile());
		assertNotNull(cppFile);

		resource = classLoader.getResource(markFileName);
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
		AnalysisServer server = AnalysisServer.builder()
				.config(
					ServerConfiguration.builder()
							.launchConsole(false)
							.launchLsp(false)
							.typestateAnalysis(TYPESTATE_ANALYSIS.NFA)
							.markFiles(markPoC1.getAbsolutePath())
							.build())
				.build();
		server.start();

		Path botan2IncludeFolder = Path.of(URI.create("file:///usr/include/botan-2")); // FIXME depends on Botan version, i.e. this is from Debian stretch with stretch-backports
		TranslationManager translationManager = TranslationManager.builder()
				.config(
					TranslationConfiguration.builder()
							.debugParser(true)
							.failOnError(false)
							.codeInNodes(true)
							.includePath(botan2IncludeFolder.toString())
							.defaultPasses()
							.loadIncludes(false)
							.sourceFiles(cppFile)
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

	@Test
	public void testSimple() throws Exception {
		// Just a very simple test to explore the graph
		Set<Finding> findings = performTest("real-examples/botan/streamciphers/bsex.cpp", "real-examples/botan/streamciphers/bsex.mark");
		GraphTraversalSource t = OverflowDatabase.getInstance()
				.getGraph()
				.traversal();

		List<Vertex> variables = t.V().hasLabel(VariableDeclaration.class.getSimpleName()).toList();

		assertTrue(variables.size() > 0);
	}

	@Test
	void testCorrecKeySize() throws Exception {
		@NonNull
		Set<Finding> findings = performTest("real-examples/botan/streamciphers/bsex.cpp", "real-examples/botan/streamciphers/bsex.mark");

		// Note that line numbers of the "range" are the actual line numbers -1. This is required for proper LSP->editor mapping
		Finding correctKeyLength = findings
				.stream()
				.filter(f -> f.getOnfailIdentifier().equals("CorrectPrivateKeyLength"))
				.findFirst()
				.get();
		assertFalse(correctKeyLength.isProblem());
	}

	@Test
	void testWrongKeySize() throws Exception {
		@NonNull
		Set<Finding> findings = performTest("real-examples/botan/streamciphers/bsex.cpp", "real-examples/botan/streamciphers/bsex.mark");

		// Note that line numbers of the "range" are the actual line numbers -1. This is required for proper LSP->editor mapping
		Finding wrongKeyLength = findings
				.stream()
				.filter(f -> f.getOnfailIdentifier().equals("WrongPrivateKeyLength"))
				.findFirst()
				.get();
		assertTrue(wrongKeyLength.isProblem());
	}

}
