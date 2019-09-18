package de.fraunhofer.aisec.crymlin;

import static org.junit.jupiter.api.Assertions.*;

import de.fraunhofer.aisec.cpg.TranslationConfiguration;
import de.fraunhofer.aisec.cpg.TranslationManager;
import de.fraunhofer.aisec.cpg.TranslationResult;
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
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class OGMTest {

  private static TranslationResult result;

  @BeforeAll
  static void setup() throws ExecutionException, InterruptedException {
    URL resource = OGMTest.class.getClassLoader().getResource("unittests/order.java");
    assertNotNull(resource);
    File sourceFile = new File(resource.getFile());

    TranslationConfiguration config =
        TranslationConfiguration.builder()
            .sourceFiles(sourceFile)
            .defaultPasses()
            .debugParser(true)
            .failOnError(true)
            .build();

    TranslationManager tm = TranslationManager.builder().config(config).build();
    // Start an analysis server
    AnalysisServer server =
        AnalysisServer.builder()
            .config(ServerConfiguration.builder().launchConsole(false).launchLsp(false).build())
            .build();
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
      Node n = OverflowDatabase.<Node>getInstance().vertexToNode(v);
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

  @Test
  void countTranslationUnits() throws Exception {
    // Get all TranslationUnitDeclarations (including subclasses)
    GraphTraversal<Vertex, Vertex> traversal =
        OverflowDatabase.getInstance()
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
    GraphTraversal<Vertex, Vertex> traversal =
        OverflowDatabase.getInstance()
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
    GraphTraversal<Vertex, Vertex> traversal =
        OverflowDatabase.getInstance()
            .getGraph()
            .traversal()
            .V()
            .hasLabel(
                MethodDeclaration.class.getSimpleName(),
                OverflowDatabase.getSubclasses(MethodDeclaration.class));
    long mdCount = traversal.count().next();
    assertEquals(13, mdCount, "Expected exactly 13 MethodDeclarations");
  }

  @Test
  void countEogEdges() throws Exception {
    // Get all EOG edges

    // TODO Julian->Samuel: EOG edges are duplicated. We see 13 edges between MethoDeclaration "ok"
    // and Literal "2" here.

    GraphTraversal<Vertex, Edge> traversal =
        OverflowDatabase.getInstance()
            .getGraph()
            .traversal()
            .V()
            .hasLabel(MethodDeclaration.class.getSimpleName())
            .has("name", "ok")
            .outE("EOG");
    long mdCount = traversal.count().next();
    assertEquals(
        1,
        mdCount,
        "Expected exactly 1 EOG edge from MethodDeclaration of method \"ok\" to first literal");
  }

  @Test
  void retrieveAllTranslationUnits() throws Exception {
    List<TranslationUnitDeclaration> original = result.getTranslationUnits();
    OverflowDatabase.<Node>getInstance().saveAll(original);

    GraphTraversal<Vertex, Vertex> traversal =
        OverflowDatabase.getInstance()
            .getGraph()
            .traversal()
            .V()
            .filter(t -> t.get().label().contains("TranslationUnitDeclaration"));

    List<TranslationUnitDeclaration> restored = new ArrayList<>();
    while (traversal.hasNext()) {
      Vertex v = traversal.next();

      Node n = OverflowDatabase.<Node>getInstance().vertexToNode(v);
      assert n instanceof TranslationUnitDeclaration
          : "n is not instanceof TranslationUnitDeclaration but " + n.getClass().getName();
      restored.add((TranslationUnitDeclaration) n);
    }

    // TODO looks like it is identical, but somehow it does not pass the "equals test yet
    //    restored.get(0).equals(original.get(0));
    //    Neo4jDatabase.getInstance().purgeDatabase();
    //    Neo4jDatabase.getInstance().saveAll(restored);
  }
}
