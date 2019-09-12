package de.fraunhofer.aisec.crymlin;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import de.fraunhofer.aisec.cpg.Database;
import de.fraunhofer.aisec.cpg.TranslationConfiguration;
import de.fraunhofer.aisec.cpg.TranslationManager;
import de.fraunhofer.aisec.cpg.TranslationResult;
import de.fraunhofer.aisec.cpg.passes.VariableUsageResolver;
import de.fraunhofer.aisec.crymlin.server.AnalysisContext;
import de.fraunhofer.aisec.crymlin.server.AnalysisServer;
import de.fraunhofer.aisec.crymlin.server.ServerConfiguration;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.Test;

class ForbiddenTest {

  private static AnalysisServer server;
  private static TranslationResult result;

  private void performTest(String sourceFilename) throws Exception {

    ClassLoader classLoader = AnalysisServerBotanTest.class.getClassLoader();

    URL resource = classLoader.getResource(sourceFilename);
    assertNotNull(resource);
    File cppFile = new File(resource.getFile());
    assertNotNull(cppFile);

    resource = classLoader.getResource("unittests/forbidden.mark");
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
    TranslationManager translationManager =
        TranslationManager.builder()
            .config(
                TranslationConfiguration.builder()
                    .debugParser(true)
                    .failOnError(false)
                    .codeInNodes(true)
                    .registerPass(new VariableUsageResolver())
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

    List<String> findings = new ArrayList<>();
    ctx.getFindings().forEach(x -> findings.add(x.toString()));

    assertEquals(
        3, findings.stream().filter(s -> s.contains("Violation against forbidden call")).count());

    assertTrue(
        findings.contains(
            "line 42: Violation against forbidden call(s) BotanF.set_key(_,_) in Entity Forbidden. Call was b.set_key(nonce, iv);"));
    assertTrue(
        findings.contains(
            "line 37: Violation against forbidden call(s) BotanF.start(nonce,_) in Entity Forbidden. Call was b.start(nonce, b);"));
    assertTrue(
        findings.contains(
            "line 36: Violation against forbidden call(s) BotanF.start() in Entity Forbidden. Call was b.start();"));

    // Stop the analysis server
    server.stop();
  }

  @Test
  void testJava() throws Exception {
    performTest("unittests/forbidden.java");
  }

  @Test
  void testCpp() throws Exception {
    performTest("unittests/forbidden.cpp");
  }
}
