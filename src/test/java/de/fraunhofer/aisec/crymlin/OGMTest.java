
package de.fraunhofer.aisec.crymlin;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import de.fraunhofer.aisec.analysis.server.AnalysisServer;
import de.fraunhofer.aisec.analysis.structures.AnalysisContext;
import de.fraunhofer.aisec.analysis.structures.ServerConfiguration;
import de.fraunhofer.aisec.cpg.TranslationConfiguration;
import de.fraunhofer.aisec.cpg.TranslationManager;
import de.fraunhofer.aisec.cpg.graph.*;
import de.fraunhofer.aisec.cpg.sarif.PhysicalLocation;
import de.fraunhofer.aisec.crymlin.connectors.db.OverflowDatabase;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Object-Graph-Mapper.
 *
 * <p>
 * We expect Neo4J and OverflowDB to behave exactly the same, so we can replace one by the other. These tests verify that the graph structure is as expected.
 */
public class OGMTest {

	private static AnalysisContext result;
	private static AnalysisServer server;

	@BeforeAll
	static void setup() throws ExecutionException, InterruptedException {
		URL resource = OGMTest.class.getClassLoader().getResource("unittests/order.java");
		assertNotNull(resource);
		File sourceFile = new File(resource.getFile());

		TranslationConfiguration config = TranslationConfiguration.builder().sourceLocations(sourceFile).defaultPasses().debugParser(true).failOnError(true).build();

		TranslationManager tm = TranslationManager.builder().config(config).build();
		// Start an analysis server
		server = AnalysisServer.builder().config(ServerConfiguration.builder().launchConsole(false).launchLsp(false).build()).build();
		server.start();

		result = server.analyze(tm).get();
	}

	@Test
	void allVerticesToNodes() throws Exception {
		var db = result.getDatabase();

		// Get all vertices from graph ...
		Graph graph = db.getGraph();
		Iterator<Vertex> vIt = graph.vertices();
		int vertices = 0;
		int verticesWithLabel = 0;
		while (vIt.hasNext()) {
			// ... and convert back to node
			Vertex v = vIt.next();
			Node n = db.vertexToNode(v);
			assertNotNull(n);

			// Vertices will always have an auto-generated ID ...
			assertNotNull(v.id());

			// ... which should be the same as the one of the converted Node object.
			assertEquals(v.id(), n.getId());

			// Vertices will have a "labels" property
			if (v.label() != null) {
				String vLabel = v.label();
				assertNotNull(vLabel, "Null label");

				// ... which matches the class of the Node object.
				assertEquals(vLabel, n.getClass().getSimpleName());
				verticesWithLabel++;
			}

			// Number of edges should be same, no matter whether we get them directly from the vertex or via graph traversal.
			long vEdgeCount = Iterators.size(v.edges(Direction.OUT));
			long gEdgeCount = db.getGraph().traversal().V(v.id()).outE().count().next();
			assertEquals(vEdgeCount, gEdgeCount);

			// Number of transitive edges should be same, no matter whether we get them directly from the vertex or via graph traversal.
			Iterator<Edge> eIt = v.edges(Direction.OUT);
			long vTranEdgeCount = 0;
			while (eIt.hasNext()) {
				vTranEdgeCount += Iterators.size(eIt.next()
						.inVertex()
						.edges(Direction.OUT));
			}
			long gTranEdgeCount = db
					.getGraph()
					.traversal()
					.V(v.id())
					.outE()
					.inV()
					.outE()
					.count()
					.next();
			assertEquals(vTranEdgeCount, gTranEdgeCount);

			if (v.label().equals("RecordDeclaration")) {
				// We expect properties that were created by a Converter to be converted back into property
				// object
				PhysicalLocation location = n.getLocation();

				//				assertNotNull(location);
				if (location == null) {
					continue;
				}
				assertTrue(location.getRegion().getStartLine() > -1);
			}
			vertices++;
		}

		// All vertices have a label.
		assertEquals(vertices, verticesWithLabel, "Only " + verticesWithLabel + " of " + vertices + " vertices have a type label");
	}

	/** Test proper edges around an <code>IfStatement</code> */
	@Test
	void testIfGraph() {
		var db = result.getDatabase();

		Vertex ifStmt = db
				.getGraph()
				.traversal()
				.V()
				.hasLabel(
					IfStatement.class.getSimpleName(),
					OverflowDatabase.getSubclasses(IfStatement.class)) // Get If stmt
				.has(
					"code",
					"if (3 < 4) {"
							+ System.lineSeparator()
							+ "      p3.start(iv);"
							+ System.lineSeparator()
							+ "    }")
				.next();

		ArrayList<Edge> eogEdges = Lists.newArrayList(ifStmt.edges(Direction.OUT, "EOG"));
		assertEquals(2, eogEdges.size());

		ArrayList<Edge> conditionEdges = Lists.newArrayList(ifStmt.edges(Direction.OUT, "CONDITION"));
		assertEquals(1, conditionEdges.size());

		// Make sure edge names are converted from camelCase to CAMEL_CASE to stay compliant with Neo4j
		// OGM!
		ArrayList<Edge> wrongThenEdges = Lists.newArrayList(ifStmt.edges(Direction.OUT, "THENSTATEMENT"));
		assertEquals(0, wrongThenEdges.size());

		ArrayList<Edge> thenEdges = Lists.newArrayList(ifStmt.edges(Direction.OUT, "THEN_STATEMENT"));
		assertEquals(1, thenEdges.size());

		ArrayList<Edge> elseEdges = Lists.newArrayList(ifStmt.edges(Direction.OUT, "ELSE_STATEMENT"));
		assertEquals(0, elseEdges.size());

		Vertex conditionExpr = db
				.getGraph()
				.traversal()
				.V()
				.hasLabel(
					IfStatement.class.getSimpleName(),
					OverflowDatabase.getSubclasses(IfStatement.class)) // Get If stmt
				.has(
					"code",
					"if (3 < 4) {"
							+ System.lineSeparator()
							+ "      p3.start(iv);"
							+ System.lineSeparator()
							+ "    }")
				.outE("CONDITION")
				.inV()
				.next();
		ArrayList<Edge> rhsEdges = Lists.newArrayList(conditionExpr.edges(Direction.OUT, "RHS"));
		assertEquals(1, rhsEdges.size());

		ArrayList<Edge> lhsEdges = Lists.newArrayList(conditionExpr.edges(Direction.OUT, "LHS"));
		assertEquals(1, lhsEdges.size());

		ArrayList<Edge> oegEdges = Lists.newArrayList(conditionExpr.edges(Direction.OUT, "EOG"));
		assertEquals(1, oegEdges.size());
	}

	@Test
	void countTranslationUnits() throws Exception {
		// Get all TranslationUnitDeclarations (including subclasses)
		GraphTraversal<Vertex, Vertex> traversal = result.getDatabase()
				.getGraph()
				.traversal()
				.V()
				.hasLabel(
					TranslationUnitDeclaration.class.getSimpleName(),
					OverflowDatabase.getSubclasses(TranslationUnitDeclaration.class));
		long tuCount = traversal.count().next();
		assertEquals(1, tuCount, "Expected exactly 1 TranslationUnitDeclarations");
	}

	@Test
	void countRecordDeclarations() throws Exception {
		// Get all RecordDeclaration (including subclasses)
		GraphTraversal<Vertex, Vertex> traversal = result.getDatabase()
				.getGraph()
				.traversal()
				.V()
				.hasLabel(
					RecordDeclaration.class.getSimpleName(),
					OverflowDatabase.getSubclasses(RecordDeclaration.class));
		long rdCount = traversal.count().next();
		assertEquals(2, rdCount, "Expected exactly 2 RecordDeclarations");
	}

	@Test
	void countMethodDeclarations() throws Exception {
		// Get all MethodDeclaration (including subclasses)
		GraphTraversal<Vertex, Vertex> traversal = result.getDatabase()
				.getGraph()
				.traversal()
				.V()
				.hasLabel(
					MethodDeclaration.class.getSimpleName(),
					OverflowDatabase.getSubclasses(MethodDeclaration.class));
		long mdCount = traversal.count().next();
		assertEquals(17, mdCount, "Expected exactly 17 MethodDeclarations");
	}

	@Test
	void countEogEdges() throws Exception {
		// Get all EOG edges

		GraphTraversal<Vertex, Edge> traversal = result.getDatabase()
				.getGraph()
				.traversal()
				.V()
				.hasLabel(MethodDeclaration.class.getSimpleName())
				.has("name",
					"ok")
				.outE("EOG");
		long mdCount = traversal.count().next();
		assertEquals(
			1,
			mdCount,
			"Expected exactly 1 EOG edge from MethodDeclaration of method \"ok\" to first literal");
	}

	@Test
	void retrieveAllTranslationUnits() throws Exception {
		var db = result.getDatabase();

		List<TranslationUnitDeclaration> original = server.getTranslationResult().getTranslationUnits();
		db.saveAll(original);

		GraphTraversal<Vertex, Vertex> traversal = db
				.getGraph()
				.traversal()
				.V()
				.filter(
					t -> t.get().label().contains("TranslationUnitDeclaration"));

		List<TranslationUnitDeclaration> restored = new ArrayList<>();
		while (traversal.hasNext()) {
			Vertex v = traversal.next();

			Node n = db.vertexToNode(v);
			assertNotNull(n);
			assertTrue(n instanceof TranslationUnitDeclaration, "n is not instanceof TranslationUnitDeclaration but " + n.getClass().getName());
			restored.add((TranslationUnitDeclaration) n);
		}
		assertEquals(1, restored.size(), "Expected exactly one TranslationUnit");
	}

	@Test
	void getContainingFunction() throws Exception {
		var db = result.getDatabase();

		Vertex p2Start = db.getGraph().traversal().V().has("code", "p2.start(iv);").next();
		Vertex containingFunction = db
				.getGraph()
				.traversal()
				.V(p2Start.id())
				.until(__.hasLabel("MethodDeclaration"))
				.repeat(
					__.inE().has("sub-graph", new P<>(String::contains, "AST")).outV())
				.next();
		assertEquals("nok2", containingFunction.property("name").value());
	}
}
