package de.fraunhofer.aisec.crymlin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import de.fhg.aisec.markmodel.MEntity;
import de.fhg.aisec.markmodel.MRule;
import de.fhg.aisec.markmodel.Mark;
import de.fraunhofer.aisec.cpg.Database;
import de.fraunhofer.aisec.cpg.TranslationConfiguration;
import de.fraunhofer.aisec.cpg.TranslationManager;
import de.fraunhofer.aisec.cpg.TranslationResult;
import de.fraunhofer.aisec.cpg.passes.EvaluationOrderGraphPass;
import de.fraunhofer.aisec.crymlin.server.AnalysisContext;
import de.fraunhofer.aisec.crymlin.server.AnalysisServer;
import de.fraunhofer.aisec.crymlin.server.ServerConfiguration;
import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class AnalysisServerBotanTest {

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

    // Start the analysis (BOTAN Symmetric Example by Oliver)
    result =
        server
            .analyze(
                newAnalysisRun(new File("../cpg/src/test/resources/botan/symm_block_cipher.cpp")))
            .get(120, TimeUnit.SECONDS);
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
    AnalysisContext ctx = (AnalysisContext) AnalysisServerBotanTest.result.getScratch().get("ctx");

    // We expect no methods (as there is no class)
    assertNotNull(ctx);
    assertTrue(ctx.methods.isEmpty());

    // Get analysis context from server
    AnalysisContext ctx2 = server.retrieveContext();
    assertNotNull(ctx2);

    // Make sure they are the same
    assertEquals(ctx, ctx2);
    assertSame(ctx, ctx2);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void translationunitsTest() throws Exception {
    List<String> tus = (List<String>) server.query("crymlin.translationunits().name().toList()");
    assertNotNull(tus);
    assertFalse(tus.isEmpty());
  }

  @Test
  public void markModelTest() throws Exception {
    Mark markModel = server.getMarkModel();
    assertNotNull(markModel);
    List<MRule> rules = markModel.getRules();
    assertEquals(2, rules.size());

    List<MEntity> ents = markModel.getEntities();
    assertEquals(6, ents.size());
  }

  @Test
  public void markEvaluationTest() throws Exception {
    List<String> findings = server.getFindings();
    assertNotNull(findings);

    for (String finding : findings) {
      System.out.println(finding);
    }
  }

  /**
   * Helper method for initializing an Analysis Run.
   *
   * @param sourceFiles
   * @return
   */
  private static TranslationManager newAnalysisRun(File... sourceFiles) {
    return TranslationManager.builder()
        .config(
            TranslationConfiguration.builder()
                .debugParser(true)
                .failOnError(false)
                // .registerPass(new SimpleForwardCfgPass()) // creates CFG   -> will block the OGM
                // when calling Database.persist() on the resulting graph.
                .registerPass(new EvaluationOrderGraphPass()) // creates EOG
                .sourceFiles(sourceFiles)
                .build())
        .build();
  }
}
