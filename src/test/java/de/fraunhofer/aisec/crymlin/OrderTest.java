package de.fraunhofer.aisec.crymlin;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import de.fraunhofer.aisec.cpg.Database;
import de.fraunhofer.aisec.cpg.TranslationConfiguration;
import de.fraunhofer.aisec.cpg.TranslationManager;
import de.fraunhofer.aisec.cpg.TranslationResult;
import de.fraunhofer.aisec.cpg.passes.CallResolver;
import de.fraunhofer.aisec.cpg.passes.DataFlowPass;
import de.fraunhofer.aisec.cpg.passes.EvaluationOrderGraphPass;
import de.fraunhofer.aisec.cpg.passes.TypeHierarchyResolver;
import de.fraunhofer.aisec.cpg.passes.VariableUsageResolver;
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

class OrderTest {

  private static AnalysisServer server;
  private static TranslationResult result;

  @BeforeAll
  static void startup() throws Exception {

    ClassLoader classLoader = AnalysisServerBotanTest.class.getClassLoader();

    URL resource = classLoader.getResource("java/order.java");
    assertNotNull(resource);
    File cppFile = new File(resource.getFile());
    assertNotNull(cppFile);

    resource = classLoader.getResource("mark.unittests/Order.mark");
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
                            .registerPass(new TypeHierarchyResolver())
                            .registerPass(new VariableUsageResolver())
                            .registerPass(new CallResolver()) // creates CG
                            .registerPass(new DataFlowPass())
                            .registerPass(new CallResolver()) // creates CG
                            .registerPass(new DataFlowPass())
                            .registerPass(new EvaluationOrderGraphPass()) // creates CFG
                            .sourceFiles(cppFile)
                            .build())
                    .build())
            .get(20, TimeUnit.MINUTES);
  }

  @AfterAll
  static void teardown() throws Exception {
    // Stop the analysis server
    server.stop();
  }

  @Test
  void sanityTest() {
    AnalysisContext ctx = (AnalysisContext) result.getScratch().get("ctx");
    assertNotNull(ctx);
    assertTrue(ctx.methods.isEmpty());
  }

  @SuppressWarnings("unchecked")
  @Test
  void translationunitsTest() throws Exception {
    List<String> tus = (List<String>) server.query("crymlin.translationunits().name().toList()");
    assertNotNull(tus);
    assertEquals(1, tus.size());
    assertTrue(tus.get(0).endsWith("java/order.java"));
  }

  @Test
  void orderTest() throws Exception {
    AnalysisContext ctx = (AnalysisContext) result.getScratch().get("ctx");
    assertNotNull(ctx.getFindings());
    List<String> findings = ctx.getFindings();
    for (String s : findings) {
      System.out.println(s);
    }

    assertEquals(8, findings.stream().filter(s -> s.contains("Violation against Order")).count());

    assertTrue(
        findings.contains(
            "Violation against Order: p.set_key(key); (init) is not allowed. Expected one of: cm.start"));
    assertTrue(
        findings.contains(
            "Violation against Order: p.start(iv.bits_of()); is not allowed. Base contains errors already."));
    assertTrue(
        findings.contains(
            "Violation against Order: p.finish(buf); is not allowed. Base contains errors already."));
    assertTrue(
        findings.contains(
            "Violation against Order: p.set_key(key); is not allowed. Base contains errors already."));
    assertTrue(findings.contains("Violation against Order: Base p2 is not correctly terminated."));
    assertTrue(
        findings.contains(
            "Violation against Order: p3.finish(buf); (finish) is not allowed. Expected one of: cm.start"));
    assertTrue(
        findings.contains(
            "Violation against Order: p4.start(iv); (start) is not allowed. Expected one of: END"));
    assertTrue(
        findings.contains(
            "Violation against Order: p4.finish(buf); is not allowed. Base contains errors already."));
  }
}
