package de.fraunhofer.aisec.crymlin;

import de.fraunhofer.aisec.crymlin.server.AnalysisServer;
import de.fraunhofer.aisec.crymlin.server.ServerConfiguration;

/** Start point of the standalone analysis server. */
public class Main {

  public static void main(String... args) throws Exception {

    System.out.println("Analysis server starting ...");
    AnalysisServer server =
        AnalysisServer.builder()
            .config(ServerConfiguration.builder().launchConsole(true).build())
            .build();

    server.start();
  }
}
