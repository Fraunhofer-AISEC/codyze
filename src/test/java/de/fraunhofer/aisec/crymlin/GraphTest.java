package de.fraunhofer.aisec.crymlin;

import de.fraunhofer.aisec.cpg.TranslationConfiguration;
import de.fraunhofer.aisec.cpg.TranslationManager;
import de.fraunhofer.aisec.cpg.TranslationResult;
import de.fraunhofer.aisec.cpg.graph.FunctionDeclaration;
import de.fraunhofer.aisec.cpg.graph.MethodDeclaration;
import de.fraunhofer.aisec.crymlin.connectors.db.Database;
import de.fraunhofer.aisec.crymlin.connectors.db.OverflowDatabase;
import de.fraunhofer.aisec.crymlin.server.AnalysisContext;
import de.fraunhofer.aisec.crymlin.server.AnalysisServer;
import de.fraunhofer.aisec.crymlin.server.ServerConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** Tests structure of CPG generated from "real" source files. */
class GraphTest {
  static TranslationResult result;
  static AnalysisServer server;

  @BeforeAll
  static void setup() throws Exception {
    ClassLoader classLoader = GraphTest.class.getClassLoader();

    URL resource = classLoader.getResource("unittests/order.java");
    assertNotNull(resource);
    File cppFile = new File(resource.getFile());
    assertNotNull(cppFile);

    // Make sure we start with a clean (and connected) db
    try {
      Database db = OverflowDatabase.getInstance();
      db.connect();
      db.purgeDatabase();
    } catch (Throwable e) {
      e.printStackTrace();
      assumeFalse(true); // Assumption for this test not fulfilled. Do not fail but bail.
    }

    // Start an analysis server
    server =
        AnalysisServer.builder()
            .config(ServerConfiguration.builder().launchConsole(false).launchLsp(false).build())
            .build();
    server.start();

    // Start the analysis
    TranslationManager translationManager =
        TranslationManager.builder()
            .config(
                TranslationConfiguration.builder()
                    .debugParser(true)
                    .failOnError(false)
                    .codeInNodes(true)
                    .defaultPasses()
                    .sourceFiles(cppFile)
                    .build())
            .build();
    CompletableFuture<TranslationResult> analyze = server.analyze(translationManager);
    try {
      result = analyze.get(5, TimeUnit.MINUTES);
    } catch (TimeoutException t) {
      analyze.cancel(true);
      translationManager.cancel(true);
      throw t;
    }

    AnalysisContext ctx = (AnalysisContext) result.getScratch().get("ctx");
    assertNotNull(ctx);
    assertTrue(ctx.methods.isEmpty());
  }

  @AfterAll
  static void teardown() {
    // Stop the analysis server
    server.stop();
  }

  @Test
  void testMethods() throws IOException {
    Set<Object> functions =
        OverflowDatabase.getInstance()
            .getGraph()
            .traversal()
            .V()
            .hasLabel(MethodDeclaration.class.getSimpleName())
            .values("name")
            .toSet();
    System.out.println(
        "METHODS:  "
            + String.join(
                ", ", functions.stream().map(v -> v.toString()).collect(Collectors.toList())));
    assertTrue(functions.contains("nok1"));
    assertTrue(functions.contains("nok2"));
    assertTrue(functions.contains("nok3"));
    assertTrue(functions.contains("nok4"));
    assertTrue(functions.contains("nok5"));
    assertTrue(functions.contains("ok"));
  }

  @Test
  void testLabelHierarchy() throws IOException {
    Set<Object> functions =
        OverflowDatabase.getInstance()
            .getGraph()
            .traversal()
            .V()
            .hasLabel(FunctionDeclaration.class.getSimpleName(), OverflowDatabase.getSubclasses(FunctionDeclaration.class))
            .values("name")
            .toSet();
    assertFalse(functions.isEmpty());
  }
}
