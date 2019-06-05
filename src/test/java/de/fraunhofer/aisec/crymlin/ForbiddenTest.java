package de.fraunhofer.aisec.crymlin;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import de.fraunhofer.aisec.cpg.Database;
import de.fraunhofer.aisec.cpg.TranslationConfiguration;
import de.fraunhofer.aisec.cpg.TranslationManager;
import de.fraunhofer.aisec.cpg.TranslationResult;
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

public class ForbiddenTest {

  private static AnalysisServer server;
  private static TranslationResult result;

  @BeforeAll
  public static void startup() throws Exception {

    ClassLoader classLoader = AnalysisServerBotanTest.class.getClassLoader();

    URL resource = classLoader.getResource("cpp/forbidden.cpp");
    assertNotNull(resource);
    File cppFile = new File(resource.getFile());
    assertNotNull(cppFile);

    resource = classLoader.getResource("mark.unittests/Forbidden.mark");
    assertNotNull(resource);
    File markPoC1 = new File(resource.getFile());
    assertNotNull(markPoC1);

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
                    .markFiles(markPoC1.getAbsolutePath())
                    .build())
            .build();
    server.start();

    // Start the analysis
    result =
        server
            .analyze(
                TranslationManager.builder()
                    .config(
                        TranslationConfiguration.builder()
                            .debugParser(true)
                            .failOnError(false)
                            .codeInNodes(true)
                            // no further passes needed for this simple test
                            // .registerPass(new TypeHierarchyResolver())
                            // .registerPass(new VariableUsageResolver())
                            // .registerPass(new CallResolver()) // creates CG
                            // .registerPass(new DataFlowPass())
                            // .registerPass(new CallResolver()) // creates CG
                            // .registerPass(new DataFlowPass())
                            // .registerPass(new EvaluationOrderGraphPass()) // creates CFG
                            .sourceFiles(cppFile)
                            .build())
                    .build())
            .get(5, TimeUnit.MINUTES);
  }

  @AfterAll
  public static void teardown() throws Exception {
    // Stop the analysis server
    server.stop();
  }

  @Test
  public void sanityTest() {
    AnalysisContext ctx = (AnalysisContext) result.getScratch().get("ctx");
    assertNotNull(ctx);
    assertTrue(ctx.methods.isEmpty());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void translationunitsTest() throws Exception {
    List<String> tus = (List<String>) server.query("crymlin.translationunits().name().toList()");
    assertNotNull(tus);
    assertEquals(1, tus.size());
    assertTrue(tus.get(0).endsWith("cpp/forbidden.cpp"));
  }

  @Test
  public void forbiddenTest() throws Exception {
    AnalysisContext ctx = (AnalysisContext) result.getScratch().get("ctx");
    assertNotNull(ctx.getFindings());
    List<String> findings = ctx.getFindings();
    assertEquals(
        2, findings.stream().filter(s -> s.contains("Violation against forbidden call")).count());
    assertTrue(
        findings.contains(
            "Violation against forbidden call Botan::Cipher_Mode::set_key(_,_) in Entity Forbidden. Call was processor->set_key(key, key);"));
    assertTrue(
        findings.contains(
            "Violation against forbidden call Botan::Cipher_Mode::start(nonce,_) in Entity Forbidden. Call was processor->start(a, a);"));
  }
}
