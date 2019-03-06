package de.fraunhofer.aisec.crymlin;

import de.fraunhofer.aisec.cpg.AnalysisManager;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/** Start point of the standalone analysis server. */
public class Main {

  public static void main(String... args)
      throws IOException, ExecutionException, InterruptedException {
    System.out.println("Analysis server starting ...");

    // TODO Initialize CPG
    AnalysisManager aServer = AnalysisManager.builder().build();
    aServer.analyze().get();

    // Initialize JythonInterpreter
    System.out.println("Launching query interpreter ...");
    JythonInterpreter interp = new JythonInterpreter();
    interp.connect();

    // Spawn an interactive console for gremlin experiments/controlling the server. Blocks forever.
    interp.spawnInteractiveConsole();
  }
}
