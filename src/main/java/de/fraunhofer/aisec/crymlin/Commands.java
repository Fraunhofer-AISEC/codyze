package de.fraunhofer.aisec.crymlin;

import de.fhg.aisec.markmodel.MRule;
import de.fhg.aisec.markmodel.Mark;
import de.fraunhofer.aisec.cpg.TranslationConfiguration;
import de.fraunhofer.aisec.cpg.TranslationManager;
import de.fraunhofer.aisec.crymlin.passes.StatementsPerMethodPass;
import de.fraunhofer.aisec.crymlin.server.AnalysisServer;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * These commands are only used by the Jython console.
 *
 * @author julian
 */
public class Commands {

  /**
   * Starts the analysis of a single file or all files in a directory.
   *
   * @param url
   */
  public void analyze(String url) {
    List<File> files = new ArrayList<>();
    File f = new File(url);
    if (f.isDirectory()) {
      files.addAll(Arrays.asList(f.listFiles()));
    } else {
      files.add(f);
    }

    TranslationManager analyzer =
        TranslationManager.builder()
            .config(
                TranslationConfiguration.builder()
                    .sourceFiles(files.toArray(new File[0]))
                    .registerPass(new StatementsPerMethodPass())
                    .build())
            .build();

    AnalysisServer server = AnalysisServer.getInstance();
    if (server != null) {
      try {
        server.analyze(analyzer);
      } catch (InterruptedException | ExecutionException e) {
        e.printStackTrace();
      }
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
    if (markModel != null) {
      for (MRule r : markModel.getRules()) {
        System.out.println(r.getName());
      }
    }
  }

  public void show_findings() {
    AnalysisServer server = AnalysisServer.getInstance();
    if (server == null) {
      System.err.println("Server not initialized");
      return;
    }
    
    for (String fi : server.getFindings()) {
      System.out.println(fi);
    }
  }
  
  /** Prints help to stdout. */
  public void help() {
    System.out.println(
        "Use the \"server\" object to control the analysis server.\n"
            + "\n"
            + "   server.load_rules(\"../mark-crymlin-eclipse-plugin/examples/Test/Rules.mark\")\n"
            + "          Load MARK rules.\n"
            + "\n"
            + "   server.analyze(\"src/test/resources/good/Bouncycastle.java\")\n"
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
