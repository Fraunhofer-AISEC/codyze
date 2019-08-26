package de.fraunhofer.aisec.crymlin;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import de.fraunhofer.aisec.cpg.Database;
import de.fraunhofer.aisec.cpg.TranslationConfiguration;
import de.fraunhofer.aisec.cpg.TranslationManager;
import de.fraunhofer.aisec.cpg.TranslationResult;
import de.fraunhofer.aisec.crymlin.server.AnalysisServer;
import de.fraunhofer.aisec.crymlin.server.ServerConfiguration;
import de.fraunhofer.aisec.mark.XtextParser;
import de.fraunhofer.aisec.mark.markDsl.Expression;
import de.fraunhofer.aisec.mark.markDsl.MarkModel;
import de.fraunhofer.aisec.markmodel.MRule;
import de.fraunhofer.aisec.markmodel.Mark;
import de.fraunhofer.aisec.markmodel.MarkInterpreter;
import de.fraunhofer.aisec.markmodel.MarkModelLoader;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MarkCppTest {

  private static HashMap<String, MarkModel> markModels;

  @BeforeAll
  public static void startup() throws Exception {
    URL resource =
        MarkCppTest.class.getClassLoader().getResource("mark_cpp");
    assertNotNull(resource);

    File markFile = new File(resource.getFile());
    assertNotNull(markFile);

    File[] directoryContent = markFile.listFiles((current, name) -> name.endsWith(".mark"));

    if (directoryContent == null) {
      directoryContent = new File[] {markFile};
    }

    assertNotNull(directoryContent);
    assertTrue(directoryContent.length > 0);

    XtextParser parser = new XtextParser();
    for (File mf : directoryContent) {
      parser.addMarkFile(mf);
    }

    markModels = parser.parse();
    assertFalse(markModels.isEmpty());
  }

  @BeforeEach
  public void clearDatabase() {
    // Make sure we start with a clean (and connected) db
    try {
      Database db = Database.getInstance();
      db.connect();
      db.purgeDatabase();
    } catch (Throwable e) {
      e.printStackTrace();
      assumeFalse(true); // Assumption for this test not fulfilled. Do not fail but bail.
    }
  }

  @AfterAll
  public static void teardown() throws Exception {

  }

  @Test
  public void _01_assign() throws Exception {
    ClassLoader classLoader = MarkCppTest.class.getClassLoader();

    URL resource = classLoader.getResource("mark_cpp/01_assign.cpp");
    assertNotNull(resource);
    File cppFile = new File(resource.getFile());
    assertNotNull(cppFile);

    resource = classLoader.getResource("mark_cpp/01_assign.mark");
    assertNotNull(resource);
    File markFile = new File(resource.getFile());
    assertNotNull(markFile);

    // Start an analysis server
    AnalysisServer server =
        AnalysisServer.builder()
            .config(
                ServerConfiguration.builder()
                    .launchConsole(false)
                    .launchLsp(false)
                    .markFiles(markFile.getAbsolutePath())
                    .build())
            .build();
    server.start();

    // Start the analysis
    TranslationResult result =
        server
            .analyze(
                TranslationManager.builder()
                    .config(
                        TranslationConfiguration.builder()
                            .debugParser(true)
                            .failOnError(false)
                            .codeInNodes(true)
                            .defaultPasses()
                            .sourceFiles(cppFile)
                            .build())
                    .build())
            .get(5, TimeUnit.MINUTES);
  }

  @Test
  public void _02_arg() throws Exception {
    ClassLoader classLoader = MarkCppTest.class.getClassLoader();

    URL resource = classLoader.getResource("mark_cpp/02_arg.cpp");
    assertNotNull(resource);
    File cppFile = new File(resource.getFile());
    assertNotNull(cppFile);

    resource = classLoader.getResource("mark_cpp/02_arg.mark");
    assertNotNull(resource);
    File markFile = new File(resource.getFile());
    assertNotNull(markFile);

    // Start an analysis server
    AnalysisServer server =
        AnalysisServer.builder()
            .config(
                ServerConfiguration.builder()
                    .launchConsole(false)
                    .launchLsp(false)
                    .markFiles(markFile.getAbsolutePath())
                    .build())
            .build();
    server.start();

    // Start the analysis
    TranslationResult result =
        server
            .analyze(
                TranslationManager.builder()
                    .config(
                        TranslationConfiguration.builder()
                            .debugParser(true)
                            .failOnError(false)
                            .codeInNodes(true)
                            .defaultPasses()
                            .sourceFiles(cppFile)
                            .build())
                    .build())
            .get(5, TimeUnit.MINUTES);
  }
}
