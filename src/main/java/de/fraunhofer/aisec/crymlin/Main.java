package de.fraunhofer.aisec.crymlin;

import de.fraunhofer.aisec.crymlin.server.AnalysisServer;
import de.fraunhofer.aisec.crymlin.server.ServerConfiguration;
import java.util.logging.LogManager;
import org.slf4j.bridge.SLF4JBridgeHandler;

/** Start point of the standalone analysis server. */
public class Main {

  static {
    // bridge java.util.logging to slf4j
    LogManager.getLogManager().reset();
    SLF4JBridgeHandler.install();
  }

  public static void main(String... args) throws Exception {

    System.out.println("Analysis server starting ...");
    AnalysisServer server =
        AnalysisServer.builder()
            .config(ServerConfiguration.builder().launchLsp(false).launchConsole(true).build())
            .build();

    server.start();
  }
}
