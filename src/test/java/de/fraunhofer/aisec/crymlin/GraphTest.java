
package de.fraunhofer.aisec.crymlin;

import de.fraunhofer.aisec.analysis.server.AnalysisServer;
import de.fraunhofer.aisec.analysis.structures.AnalysisContext;
import de.fraunhofer.aisec.analysis.structures.ServerConfiguration;
import de.fraunhofer.aisec.cpg.TranslationConfiguration;
import de.fraunhofer.aisec.cpg.TranslationManager;
import de.fraunhofer.aisec.cpg.graph.declarations.FunctionDeclaration;
import de.fraunhofer.aisec.cpg.graph.declarations.MethodDeclaration;
import de.fraunhofer.aisec.crymlin.connectors.db.OverflowDatabase;
import de.fraunhofer.aisec.crymlin.connectors.db.TraversalConnection;
import de.fraunhofer.aisec.crymlin.dsl.CrymlinTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.BulkSet;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests structure of CPG generated from "real" source files.
 */
class GraphTest {
	static AnalysisContext result;
	static AnalysisServer server;

	@BeforeAll
	static void setup() throws Exception {
		ClassLoader classLoader = GraphTest.class.getClassLoader();

		URL resource = classLoader.getResource("unittests/order.java");
		assertNotNull(resource);
		File cppFile = new File(resource.getFile());
		assertNotNull(cppFile);

		// Start an analysis server
		server = AnalysisServer.builder().config(ServerConfiguration.builder().launchConsole(false).launchLsp(false).build()).build();
		server.start();

		// Start the analysis
		TranslationManager translationManager = TranslationManager.builder()
				.config(
					TranslationConfiguration.builder().debugParser(true).failOnError(false).codeInNodes(true).defaultPasses().sourceLocations(cppFile).build())
				.build();
		CompletableFuture<AnalysisContext> analyze = server.analyze(translationManager);
		try {
			result = analyze.get(5, TimeUnit.MINUTES);
		}
		catch (TimeoutException t) {
			analyze.cancel(true);
			throw t;
		}

		AnalysisContext ctx = result;
		assertNotNull(ctx);
		assertTrue(ctx.methods.isEmpty());
	}

	@AfterAll
	static void teardown() {
		// Stop the analysis server
		server.stop();
	}

	@Test
	void testMethods() {
		Set<Object> functions = result.getDatabase().getGraph().traversal().V().hasLabel(MethodDeclaration.class.getSimpleName()).values("name").toSet();
		System.out.println(
			"METHODS:  "
					+ functions.stream().map(Object::toString).collect(Collectors.joining(", ")));
		assertTrue(functions.contains("nok1"));
		assertTrue(functions.contains("nok2"));
		assertTrue(functions.contains("nok3"));
		assertTrue(functions.contains("nok4"));
		assertTrue(functions.contains("nok5"));
		assertTrue(functions.contains("ok"));
	}

	@Test
	void testLabelHierarchy() {
		Set<Object> functions = result.getDatabase()
				.getGraph()
				.traversal()
				.V()
				.hasLabel(
					FunctionDeclaration.class.getSimpleName(),
					OverflowDatabase.getSubclasses(FunctionDeclaration.class))
				.values("name")
				.toSet();
		assertFalse(functions.isEmpty());
	}

	@Test
	void crymlinDslTest() {
		try (TraversalConnection traversalConnection = new TraversalConnection(result.getDatabase())) {
			// Run crymlin queries directly in Java
			CrymlinTraversalSource crymlin = traversalConnection.getCrymlinTraversal();
			Optional<Long> count = crymlin.records()
					.count()
					.tryNext();
			assertTrue(count.isPresent());
		}
	}

	@Test
	void crymlinIfstmtTest() {
		try (TraversalConnection traversalConnection = new TraversalConnection(result.getDatabase())) {
			CrymlinTraversalSource crymlin = traversalConnection.getCrymlinTraversal();
			Optional<Long> count = crymlin.ifstmts()
					.count()
					.tryNext();
			// Expecting 2 if stmts
			assertEquals(2, count.get());
		}
	}

	@Test
	void crymlinNamespacesTest() {
		try (TraversalConnection traversalConnection = new TraversalConnection(result.getDatabase())) {
			CrymlinTraversalSource crymlin = traversalConnection.getCrymlinTraversal();
			Optional<Long> count = crymlin.namespaces()
					.count()
					.tryNext();
			// Expecting no namespaces (Java)
			assertEquals(0, count.get());
		}
	}

	@Test
	void crymlinNamespacesPatternTest() {
		try (TraversalConnection traversalConnection = new TraversalConnection(result.getDatabase())) {
			CrymlinTraversalSource crymlin = traversalConnection.getCrymlinTraversal();
			Optional<Long> count = crymlin.namespaces("codyze")
					.count()
					.tryNext();
			// Expecting no namespaces (Java)
			assertEquals(0, count.get());
		}
	}

	@Test
	void crymlinTypedefsTest() {
		try (TraversalConnection traversalConnection = new TraversalConnection(result.getDatabase())) {
			CrymlinTraversalSource crymlin = traversalConnection.getCrymlinTraversal();
			Optional<Long> count = crymlin.typedefs()
					.count()
					.tryNext();
			// Expecting no typedefs (Java)
			assertEquals(0, count.get());
		}
	}

	@Test
	void crymlinTypedefsPatternTest() {
		try (TraversalConnection traversalConnection = new TraversalConnection(result.getDatabase())) {
			CrymlinTraversalSource crymlin = traversalConnection.getCrymlinTraversal();
			Optional<Long> count = crymlin.typedefs("codyze")
					.count()
					.tryNext();
			// Expecting no typedefs (Java)
			assertEquals(0, count.get());
		}
	}

	@Test
	void crymlinReturnsTest() {
		try (TraversalConnection traversalConnection = new TraversalConnection(result.getDatabase())) {
			CrymlinTraversalSource crymlin = traversalConnection.getCrymlinTraversal();
			Optional<Long> count = crymlin.returns()
					.count()
					.tryNext();
			System.out.println(count.get());
			// 15 (virtual) returns
			assertEquals(15, count.get());
		}
	}

	@Test
	void crymlinVarsTest() {
		try (TraversalConnection traversalConnection = new TraversalConnection(result.getDatabase())) {
			CrymlinTraversalSource crymlin = traversalConnection.getCrymlinTraversal();
			Set<String> count = crymlin.vars()
					.name()
					.toSet();
			assertTrue(count.contains("p"));
			assertTrue(count.contains("p2"));
			assertTrue(count.contains("p3"));
			assertTrue(count.contains("p4"));
			assertTrue(count.contains("p5"));
		}
	}

	@Test
	void crymlinVarldeclTest() {
		try (TraversalConnection traversalConnection = new TraversalConnection(result.getDatabase())) {
			CrymlinTraversalSource crymlin = traversalConnection.getCrymlinTraversal();
			Optional<Long> count = crymlin.valdecl()
					.count()
					.tryNext();
			System.out.println(count.get());
			// 29 variable declarations
			assertEquals(29, count.get());
		}
	}

	@Test
	void crymlinFunctionsTest() {
		try (TraversalConnection traversalConnection = new TraversalConnection(result.getDatabase())) {
			CrymlinTraversalSource crymlin = traversalConnection.getCrymlinTraversal();
			Optional<Long> count = crymlin.functions()
					.count()
					.tryNext();
			assertEquals(17, count.get());
		}
	}

	@Test
	void crymlinFunctionsPatternTest() {
		try (TraversalConnection traversalConnection = new TraversalConnection(result.getDatabase())) {
			CrymlinTraversalSource crymlin = traversalConnection.getCrymlinTraversal();
			Set<String> count = crymlin.functions("nok")
					.name()
					.toSet();
			assertTrue(count.contains("nok1"));
			assertTrue(count.contains("nok2"));
			assertTrue(count.contains("nok3"));
			assertTrue(count.contains("nok4"));
			assertTrue(count.contains("nok5"));
			assertFalse(count.contains("ok"));
		}
	}

	/**
	 * Adding nodes to the graph. Note that <code>addV</code> will add nodes to the in-memory graph so future queries will see them.
	 *
	 * <p>
	 * Note that we need to use labels which actually exist in our CPG and we must provide them in Tinkerpop's multi-label syntax (<label 1>::<label 2>).
	 */
	@SuppressWarnings("unchecked")
	@Test
	void gremlinGraphMutationTest() {
		var db = new OverflowDatabase(ServerConfiguration.builder().disableOverflow(true).build());
		db.connect();

		try (TraversalConnection traversalConnection = new TraversalConnection(db)) {
			GraphTraversalSource g = traversalConnection.getGremlinTraversal();

			Long size = g.V()
					.count()
					.next();
			List<Object> t = g.addV()
					.property(T.label, "TranslationUnitDeclaration")
					.property("name", "some_value")
					.store("one")
					.addV()
					.property(T.label,
						"Declaration")
					.property("name", "another_value")
					.store("one")
					.cap("one")
					.toList();

			assertNotNull(t);

			List<String> labels = new ArrayList<>();
			for (Object x : t) {
				BulkSet<Vertex> v = (BulkSet<Vertex>) x;
				for (Vertex a : v) {
					labels.add(a.label());
				}
			}
			assertEquals(2, labels.size());
			assertEquals("TranslationUnitDeclaration", labels.get(0));
			assertEquals("Declaration", labels.get(1));
			Long sizeNew = g.V()
					.count()
					.next();

			// New graph is expected to be +2 nodes larger.
			assertEquals(2, sizeNew - size);

			// Even with a new traversalConnection object, the graph will remain larger
			GraphTraversalSource g2 = traversalConnection.getGremlinTraversal();
			assertNotEquals(g, g2);
			Long sizeAgain = g2.V()
					.count()
					.next();
			assertEquals(2, sizeAgain - size);
		}
	}

}
