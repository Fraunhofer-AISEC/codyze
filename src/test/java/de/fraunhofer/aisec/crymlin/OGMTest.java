
package de.fraunhofer.aisec.crymlin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.fraunhofer.aisec.cpg.TranslationConfiguration;
import de.fraunhofer.aisec.cpg.TranslationManager;
import de.fraunhofer.aisec.cpg.TranslationResult;
import de.fraunhofer.aisec.cpg.graph.IfStatement;
import de.fraunhofer.aisec.cpg.graph.MethodDeclaration;
import de.fraunhofer.aisec.cpg.graph.Node;
import de.fraunhofer.aisec.cpg.graph.RecordDeclaration;
import de.fraunhofer.aisec.cpg.graph.TranslationUnitDeclaration;
import de.fraunhofer.aisec.crymlin.connectors.db.OverflowDatabase;
import de.fraunhofer.aisec.crymlin.server.AnalysisServer;
import de.fraunhofer.aisec.crymlin.server.ServerConfiguration;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.apache.commons.compress.utils.Lists;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Tests for the Object-Graph-Mapper.
 *
 * <p>
 * We expect Neo4J and OverflowDB to behave exactly the same, so we can replace one by the other. These tests verify that the graph structure is as expected.
 */
public class OGMTest {

	private static TranslationResult result;

	@BeforeAll
	static void setup() throws ExecutionException, InterruptedException {
		URL resource = OGMTest.class.getClassLoader().getResource("unittests/order.java");
		assertNotNull(resource);
		File sourceFile = new File(resource.getFile());

		TranslationConfiguration config = TranslationConfiguration.builder().sourceFiles(sourceFile).defaultPasses().debugParser(true).failOnError(true).build();

		TranslationManager tm = TranslationManager.builder().config(config).build();
		// Start an analysis server
		AnalysisServer server = AnalysisServer.builder().config(ServerConfiguration.builder().launchConsole(false).launchLsp(false).build()).build();
		server.start();

		result = server.analyze(tm).get();
	}

	@Test
	void allVerticesToNodes() throws Exception {
		// Get all vertices from graph ...
		Graph graph = OverflowDatabase.getInstance().getGraph();
		Iterator<Vertex> vIt = graph.vertices();
		while (vIt.hasNext()) {
			// ... and convert back to node
			Vertex v = vIt.next();
			Node n = OverflowDatabase.<Node> getInstance().vertexToNode(v);
			assertNotNull(n);

			// Vertices will always have an auto-generated ID ...
			assertNotNull(v.id());

			// ... but this is different from "id" property of the underlying Node object. This will
			// always be null (if not set explicitly)
			assertNull(n.getId());

			if (v.label().equals("RecordDeclaration")) {
				// We expect properties that were created by a Converter to be converted back into property
				// object
				assertTrue(n.getRegion().getStartLine() > -1);
			}
			System.out.println(n.toString());
		}
	}

	/** Test proper edges around an <code>IfStatement</code> */
	@Test
	void testIfGraph() {
		Vertex ifStmt = OverflowDatabase.getInstance().getGraph().traversal().V().hasLabel(IfStatement.class.getSimpleName(),
			OverflowDatabase.getSubclasses(IfStatement.class)) // Get If stmt
				.has("code", "if (3 < 4) {" + System.lineSeparator() + "      p3.start(iv);" + System.lineSeparator() + "    }").next();

		ArrayList<Edge> eogEdges = Lists.newArrayList(ifStmt.edges(Direction.OUT, "EOG"));
		assertEquals(1, eogEdges.size());

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

		Vertex conditionExpr = OverflowDatabase.getInstance().getGraph().traversal().V().hasLabel(IfStatement.class.getSimpleName(),
			OverflowDatabase.getSubclasses(IfStatement.class)) // Get If stmt
				.has("code", "if (3 < 4) {" + System.lineSeparator() + "      p3.start(iv);" + System.lineSeparator() + "    }").outE("CONDITION").inV().next();
		ArrayList<Edge> rhsEdges = Lists.newArrayList(conditionExpr.edges(Direction.OUT, "RHS"));
		assertEquals(1, rhsEdges.size());

		ArrayList<Edge> lhsEdges = Lists.newArrayList(conditionExpr.edges(Direction.OUT, "LHS"));
		assertEquals(1, lhsEdges.size());

		ArrayList<Edge> oegEdges = Lists.newArrayList(conditionExpr.edges(Direction.OUT, "EOG"));
		assertEquals(1, eogEdges.size());
	}

	@Test
	void countTranslationUnits() throws Exception {
		// Get all TranslationUnitDeclarations (including subclasses)
		GraphTraversal<Vertex, Vertex> traversal = OverflowDatabase.getInstance().getGraph().traversal().V().hasLabel(TranslationUnitDeclaration.class.getSimpleName(),
			OverflowDatabase.getSubclasses(TranslationUnitDeclaration.class));
		long tuCount = traversal.count().next();
		assertEquals(1, tuCount, "Expected exactly 1 TranslationUnitDeclarations");
	}

	@Test
	void countRecordDeclarations() throws Exception {
		// Get all RecordDeclaration (including subclasses)
		GraphTraversal<Vertex, Vertex> traversal = OverflowDatabase.getInstance().getGraph().traversal().V().hasLabel(RecordDeclaration.class.getSimpleName(),
			OverflowDatabase.getSubclasses(RecordDeclaration.class));
		long rdCount = traversal.count().next();
		assertEquals(2, rdCount, "Expected exactly 2 RecordDeclarations");
	}

	@Test
	void countMethodDeclarations() throws Exception {
		// Get all MethodDeclaration (including subclasses)
		GraphTraversal<Vertex, Vertex> traversal = OverflowDatabase.getInstance().getGraph().traversal().V().hasLabel(MethodDeclaration.class.getSimpleName(),
			OverflowDatabase.getSubclasses(MethodDeclaration.class));
		long mdCount = traversal.count().next();
		assertEquals(15, mdCount, "Expected exactly 15 MethodDeclarations");
	}

	@Test
	void countEogEdges() throws Exception {
		// Get all EOG edges

		GraphTraversal<Vertex, Edge> traversal = OverflowDatabase.getInstance().getGraph().traversal().V().hasLabel(MethodDeclaration.class.getSimpleName()).has("name",
			"ok").outE("EOG");
		long mdCount = traversal.count().next();
		assertEquals(1, mdCount, "Expected exactly 1 EOG edge from MethodDeclaration of method \"ok\" to first literal");
	}

	@Test
	void retrieveAllTranslationUnits() throws Exception {
		List<TranslationUnitDeclaration> original = result.getTranslationUnits();
		OverflowDatabase.<Node> getInstance().saveAll(original);

		GraphTraversal<Vertex, Vertex> traversal = OverflowDatabase.getInstance().getGraph().traversal().V().filter(
			t -> t.get().label().contains("TranslationUnitDeclaration"));

		List<TranslationUnitDeclaration> restored = new ArrayList<>();
		while (traversal.hasNext()) {
			Vertex v = traversal.next();

			Node n = OverflowDatabase.<Node> getInstance().vertexToNode(v);
			assert n instanceof TranslationUnitDeclaration : "n is not instanceof TranslationUnitDeclaration but " + n.getClass().getName();
			restored.add((TranslationUnitDeclaration) n);
		}
	}

	static class OverflowTest {

		@Test
		@Disabled // Requires a few minutes. Should be run manually.
		void overflowTest() throws Exception {
			URL resource = OGMTest.class.getClassLoader().getResource("unittests/qrc_bitcoin.cpp");
			assertNotNull(resource);
			File sourceFile = new File(resource.getFile());

			TranslationConfiguration config = TranslationConfiguration.builder().sourceFiles(sourceFile).defaultPasses().debugParser(true).failOnError(true).build();

			TranslationManager tm = TranslationManager.builder().config(config).build();
			// Start an analysis server
			AnalysisServer server = AnalysisServer.builder().config(ServerConfiguration.builder().launchConsole(false).launchLsp(false).build()).build();
			server.start();

			result = server.analyze(tm).get();
			OverflowDatabase.getInstance().saveAll(result.getTranslationUnits());
		}
	}
}
