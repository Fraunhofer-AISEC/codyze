package de.fraunhofer.aisec.crymlin;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import de.fraunhofer.aisec.cpg.TranslationConfiguration;
import de.fraunhofer.aisec.cpg.TranslationManager;
import de.fraunhofer.aisec.cpg.TranslationResult;
import de.fraunhofer.aisec.crymlin.connectors.db.Database;
import de.fraunhofer.aisec.crymlin.connectors.db.OverflowDatabase;
import de.fraunhofer.aisec.crymlin.server.AnalysisServer;
import de.fraunhofer.aisec.crymlin.server.ServerConfiguration;
import java.io.File;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class CppLanguageFeatureTest {

  private static AnalysisServer server;
  private static TranslationResult result;

  @BeforeAll
  public static void startup() throws Exception {
    ClassLoader classLoader = CppLanguageFeatureTest.class.getClassLoader();

    URL resource = classLoader.getResource("mark_rule_eval.mark");
    assertNotNull(resource);
    File markPoC1 = new File(resource.getFile());
    assertNotNull(markPoC1);

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
            .config(
                ServerConfiguration.builder()
                    .launchConsole(false)
                    .launchLsp(false)
                    .markFiles(markPoC1.getAbsolutePath())
                    .build())
            .build();
    server.start();
  }

  @AfterAll
  public static void teardown() throws Exception {
    // Stop the analysis server
    server.stop();
  }

  @Test
  public void functionTest() throws Exception {
    ClassLoader classLoader = CppLanguageFeatureTest.class.getClassLoader();

    URL resource = classLoader.getResource("cpp/function.cpp");
    assertNotNull(resource);
    File cppFile = new File(resource.getFile());
    assertNotNull(cppFile);

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
      throw t;
    }
  }

  @Test
  public void functionPointerTest() throws Exception {
    ClassLoader classLoader = CppLanguageFeatureTest.class.getClassLoader();

    URL resource = classLoader.getResource("cpp/function_ptr.cpp");
    assertNotNull(resource);
    File cppFile = new File(resource.getFile());
    assertNotNull(cppFile);

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
      throw t;
    }
  }
}
