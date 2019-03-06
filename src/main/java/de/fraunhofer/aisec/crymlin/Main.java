package de.fraunhofer.aisec.crymlin;

import de.fraunhofer.aisec.cpg.AnalysisConfiguration;
import de.fraunhofer.aisec.cpg.AnalysisManager;
import de.fraunhofer.aisec.cpg.AnalysisResult;
import de.fraunhofer.aisec.cpg.Database;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/** Start point of the standalone analysis server. */
public class Main {

  public static void main(String... args)
      throws IOException, ExecutionException, InterruptedException {
    System.out.println("Analysis server starting ...");

    // TODO Initialize CPG
    AnalysisManager aServer =
        AnalysisManager.builder()
            .config(
                AnalysisConfiguration.builder()
                    .sourceFiles(new File("src/test/resources/good/Bouncycastle.java"))
                    .build())
            .build();

    // Run all passes. This will *not* yet persist the result
    AnalysisResult result = aServer.analyze().get();

    // Persist the result
    Database db = Database.getInstance();
    db.connect();
    result.persist(db);

    // Initialize JythonInterpreter
    System.out.println("Launching query interpreter ...");
    JythonInterpreter interp = new JythonInterpreter();
    interp.connect();

    // Spawn an interactive console for gremlin experiments/controlling the server. Blocks forever.
    // May be replaced by custom JLine console later (not important for the moment)
    interp.spawnInteractiveConsole();
  }
}
