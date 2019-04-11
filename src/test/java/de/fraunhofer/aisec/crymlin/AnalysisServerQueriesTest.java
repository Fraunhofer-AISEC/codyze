package de.fraunhofer.aisec.crymlin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import de.fraunhofer.aisec.cpg.Database;
import de.fraunhofer.aisec.cpg.TranslationConfiguration;
import de.fraunhofer.aisec.cpg.TranslationManager;
import de.fraunhofer.aisec.cpg.TranslationResult;
import de.fraunhofer.aisec.cpg.passes.CallResolver;
import de.fraunhofer.aisec.cpg.passes.SimpleForwardCfgPass;
import de.fraunhofer.aisec.crymlin.passes.StatementsPerMethodPass;
import de.fraunhofer.aisec.crymlin.server.AnalysisContext;
import de.fraunhofer.aisec.crymlin.server.AnalysisServer;
import de.fraunhofer.aisec.crymlin.server.ServerConfiguration;
import de.fraunhofer.aisec.crymlin.structures.Method;
import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class AnalysisServerQueriesTest {

  private static AnalysisServer server;
  private static TranslationResult result;

  @BeforeAll
  public static void startup() throws Exception {
    // Make sure we start with a clean (and connected) db
    try {
      Database db = Database.getInstance();
      db.connect();
      db.purgeDatabase();
    } catch (Throwable e) {
      e.printStackTrace();
      assumeFalse(true); // Assumption for this test not fulfilled. Do not fail but bail.
    }

    String markModelFiles =
        "../mark-crymlin-eclipse-plugin/examples/PoC_MS1/"; // Directory containing MARK files

    // Start an analysis server
    server =
        AnalysisServer.builder()
            .config(
                ServerConfiguration.builder()
                    .launchConsole(false)
                    .launchLsp(false)
                    .markFiles(markModelFiles)
                    .build())
            .build();
    server.start();

    // Start the analysis
    result =
        server
            .analyze(newJavaAnalysisRun(new File("src/test/resources/good/Bouncycastle.java")))
            .get(5, TimeUnit.MINUTES);
  }

  @AfterAll
  public static void teardown() throws Exception {
    // Stop the analysis server
    server.stop();
  }

  /** Test analysis context - additional in-memory structures used for analysis. */
  @Test
  public void contextTest() {
    // Get analysis context from scratch
    AnalysisContext ctx =
        (AnalysisContext) AnalysisServerQueriesTest.result.getScratch().get("ctx");

    // We expect at least some methods
    assertNotNull(ctx);
    assertFalse(ctx.methods.isEmpty());
    Method meth = ctx.methods.entrySet().stream().findFirst().get().getValue();
    assertFalse(meth.getStatements().isEmpty());

    // Get analysis context from server
    AnalysisContext ctx2 = server.retrieveContext();
    assertNotNull(ctx2);

    // Make sure they are the same
    assertEquals(ctx, ctx2);
    assertSame(ctx, ctx2);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void recorddeclarationsTest() throws Exception {
    List<Vertex> classes = (List<Vertex>) server.query("crymlin.recorddeclarations().toList()");
    assertNotNull(classes);
    assertFalse(classes.isEmpty());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void recorddeclarationTest() throws Exception {
    List<Vertex> classes =
        (List<Vertex>) server.query("crymlin.recorddeclaration(\"good.Bouncycastle\").toList()");
    assertNotNull(classes);
    assertFalse(classes.isEmpty());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void translationunitsTest() throws Exception {
    List<String> tus = (List<String>) server.query("crymlin.translationunits().name().toList()");
    assertNotNull(tus);
    assertFalse(tus.isEmpty());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void statementsTest() throws Exception {
    List<Vertex> result = (List<Vertex>) server.query("crymlin.methods().statements().toList()");
    List<Vertex> tus = (List<Vertex>) result;
    assertNotNull(tus);
    assertFalse(tus.isEmpty());
    for (Vertex x : tus) {
      System.out.println(x.property("code").value());
    }
  }

  /**
   * Helper method for initializing an Analysis Run.
   *
   * @param sourceFiles
   * @return
   */
  private static TranslationManager newJavaAnalysisRun(File... sourceFiles) {
    return TranslationManager.builder()
        .config(
            TranslationConfiguration.builder()
                .debugParser(true)
                .failOnError(false)
                .registerPass(new SimpleForwardCfgPass()) // creates CFG
                .registerPass(new CallResolver()) // creates CG
                .registerPass(new StatementsPerMethodPass())
                .sourceFiles(sourceFiles)
                .build())
        .build();
  }
}
