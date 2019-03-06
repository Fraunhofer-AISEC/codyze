package de.fraunhofer.aisec.crymlin;

import de.fraunhofer.aisec.crymlin.server.AnalysisServer;
import de.fraunhofer.aisec.crymlin.server.ServerConfiguration;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/** Start point of the standalone analysis server. */
public class Main {

  public static void main(String... args)
      throws IOException, ExecutionException, InterruptedException {

    System.out.println("Analysis server starting ...");
    AnalysisServer.builder()
        .config(ServerConfiguration.builder().launchConsole(true).build())
        .build();
  }
}
