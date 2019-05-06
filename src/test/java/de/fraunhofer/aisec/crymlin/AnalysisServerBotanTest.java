package de.fraunhofer.aisec.crymlin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import de.fhg.aisec.markmodel.MEntity;
import de.fhg.aisec.markmodel.MRule;
import de.fhg.aisec.markmodel.Mark;
import de.fraunhofer.aisec.cpg.Database;
import de.fraunhofer.aisec.cpg.TranslationConfiguration;
import de.fraunhofer.aisec.cpg.TranslationManager;
import de.fraunhofer.aisec.cpg.TranslationResult;
import de.fraunhofer.aisec.cpg.passes.ControlFlowGraphPass;
import de.fraunhofer.aisec.crymlin.server.AnalysisContext;
import de.fraunhofer.aisec.crymlin.server.AnalysisServer;
import de.fraunhofer.aisec.crymlin.server.ServerConfiguration;
import java.io.File;
import java.net.URL;
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

    ClassLoader classLoader = AnalysisServerBotanTest.class.getClassLoader();

    URL resource = classLoader.getResource("symm_block_cipher.cpp");
    assertNotNull(resource);
    File cppFile = new File(resource.getFile());
    assertNotNull(cppFile);

    resource = classLoader.getResource("mark/PoC_MS1/Botan_AutoSeededRNG.mark");
    assertNotNull(resource);
    File markPoC1 = new File(resource.getFile());
    assertNotNull(markPoC1);
    String markModelFiles = markPoC1.getParent();

    // Make sure we start with a clean (and connected) db
    try {
      Database db = Database.getInstance();
      db.connect();
      db.purgeDatabase();
    } catch (Throwable e) {
      e.printStackTrace();
      assumeFalse(true); // Assumption for this test not fulfilled. Do not fail but bail.
    }

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
    result = server.analyze(newAnalysisRun(cppFile)).get(5, TimeUnit.MINUTES);
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
    assertEquals(7, rules.size());

    List<MEntity> ents = markModel.getEntities();
    assertEquals(9, ents.size());
  }

  @Test
  public void markEvaluationTest() throws Exception {
    AnalysisContext ctx = (AnalysisContext) AnalysisServerBotanTest.result.getScratch().get("ctx");
    assertNotNull(ctx);
    List<String> findings = ctx.getFindings();
    assertNotNull(findings);

    System.out.println("Findings");
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
                .registerPass(new ControlFlowGraphPass()) // creates CFG
                // when calling Database.persist() on the resulting graph.
                // .registerPass(new EvaluationOrderGraphPass()) // creates EOG
                .sourceFiles(sourceFiles)
                .build())
        .build();
  }
}
