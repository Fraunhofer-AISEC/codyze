package de.fraunhofer.aisec.crymlin;

import de.fraunhofer.aisec.cpg.TranslationConfiguration;
import de.fraunhofer.aisec.cpg.TranslationManager;
import de.fraunhofer.aisec.cpg.TranslationResult;
import de.fraunhofer.aisec.crymlin.connectors.db.Neo4jDatabase;
import de.fraunhofer.aisec.crymlin.server.AnalysisContext;
import de.fraunhofer.aisec.crymlin.server.AnalysisServer;
import de.fraunhofer.aisec.crymlin.structures.Finding;
import de.fraunhofer.aisec.markmodel.MRule;
import de.fraunhofer.aisec.markmodel.Mark;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * These commands are only used by the Jython console.
 *
 * @author julian
 */
public class Commands {

  private static final Logger log = LoggerFactory.getLogger(Commands.class);

  // backref
  private final JythonInterpreter jythonInterpreter;

  public Commands(JythonInterpreter jythonInterpreter) {
    this.jythonInterpreter = jythonInterpreter;
  }

  /**
   * Starts the analysis of a single file or all files in a directory.
   *
   * @param url
   */
  public void analyze(String url) {
    List<File> files = new ArrayList<>();
    File f = new File(url);
    if (f.isDirectory()) {
      File[] list = f.listFiles();
      if (list != null) {
        files.addAll(Arrays.asList(list));
      } else {
        log.error("Null file list");
      }
    } else {
      files.add(f);
    }

    Neo4jDatabase.getInstance().connect(); // simply returns if already connected
    if (!Neo4jDatabase.getInstance().isConnected()) {
      return;
    }
    Neo4jDatabase.getInstance().purgeDatabase();

    TranslationManager translationManager =
        TranslationManager.builder()
            .config(
                TranslationConfiguration.builder()
                    .debugParser(true)
                    .failOnError(false)
                    .codeInNodes(true)
                    .defaultPasses()
                    .sourceFiles(files.toArray(new File[0]))
                    .build())
            .build();

    AnalysisServer server = AnalysisServer.getInstance();
    if (server == null) {
      log.error("Analysis server not available");
      return;
    }
    CompletableFuture<TranslationResult> analyze = server.analyze(translationManager);

    try {
      TranslationResult translationResult = analyze.get(10, TimeUnit.MINUTES);
      jythonInterpreter.setResult(translationResult);
    } catch (InterruptedException e) {
      log.error("Interrupted", e);
      Thread.currentThread().interrupt();
    } catch (ExecutionException e) {
      log.error("Exception", e);
    } catch (TimeoutException e) {
      analyze.cancel(true);
      translationManager.cancel(true);
      System.out.println("Analysis interrupted after timeout of 10 minutes.");
    }
  }

  /**
   * Loads MARK rules into the server. Must be called before analyze, otherwise no rules will be
   * evaluated.
   *
   * @param fileName
   */
  public void load_rules(String fileName) {
    AnalysisServer server = AnalysisServer.getInstance();
    if (server == null) {
      System.err.println("Server not initialized");
      return;
    }

    server.loadMarkRules(new File(fileName));
  }

  public void list_rules() {
    AnalysisServer server = AnalysisServer.getInstance();
    if (server == null) {
      System.err.println("Server not initialized");
      return;
    }

    Mark markModel = server.getMarkModel();
    for (MRule r : markModel.getRules()) {
      System.out.println(r.getName());
    }
  }

  public void show_findings() {
    TranslationResult lastResult = jythonInterpreter.getLastResult();
    if (lastResult == null) {
      System.err.println("No analysis run yet.");
      return;
    }
    AnalysisContext ctx = (AnalysisContext) lastResult.getScratch().get("ctx");

    for (Finding fi : ctx.getFindings()) {
      System.out.println(fi);
    }
  }

  /** Prints help to stdout. */
  public void help() {
    System.out.println(
        "Use the \"server\" object to control the analysis server.\n"
            + "\n"
            + "   server.load_rules(\"../mark-crymlin-eclipse-plugin/examples/PoC_MS1/Botan_CipherMode.mark\")\n"
            + "          Load MARK rules.\n"
            + "\n"
            + "   server.list_rules()\n"
            + "          List active MARK rules.\n"
            + "\n"
            + "   server.show_findings()\n"
            + "          Show results of MARK evaluation.\n"
            + "\n"
            + "   server.analyze(\"src/test/resources/good/Bouncycastle.java\")\n"
            + "   server.analyze(\"src/test/resources/symm_block_cipher.cpp\")\n"
            + "          Analyze a single source file. Remember to load MARK rules before analyzing.\n"
            + "\n"
            + "   server.analyze(\"src/test/resources/good\")\n"
            + "          Analyze all source files in a directory. Remember to load MARK rules before analyzing.\n"
            + "\n"
            + "\n"
            + "You may then start writing crymlin queries using the \"crymlin\" object.\n"
            + "\n"
            + "Examples: \n"
            + "   crymlin.recorddeclarations().toList()\n"
            + "          Returns array of vertices representing RecordDeclarations.\n"
            + "\n"
            + "   crymlin.recorddeclaration(\"good.Bouncycastle\").next()\n"
            + "          Returns vertex representing the RecordDeclarations of \"good.Bouncycastle\".\n"
            + "\n"
            + "   crymlin.recorddeclaration(\"good.Bouncycastle\").sourcecode().next()\n"
            + "          Returns source code of \"good.Bouncycastle\".\n"
            + "\n"
            + "   crymlin.translationunits().name().toList()\n"
            + "          Returns array of strings representing the names of TranslationUnits.\n"
            + "\n"
            + "   crymlin.translationunits().next()\n"
            + "          Returns the first TranslationUnit vertex (or null if none exists).\n"
            + "\n"
            + "   dir(crymlin.translationunits())\n"
            + "          Good ol' Python dir() to find out what properties/methods are available.\n");
  }
}
