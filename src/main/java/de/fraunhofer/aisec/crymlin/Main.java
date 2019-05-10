package de.fraunhofer.aisec.crymlin;

import de.fraunhofer.aisec.crymlin.server.AnalysisServer;
import de.fraunhofer.aisec.crymlin.server.ServerConfiguration;
import java.util.logging.LogManager;
import org.apache.commons.cli.*;
import org.slf4j.bridge.SLF4JBridgeHandler;

/** Start point of the standalone analysis server. */
public class Main {

  static {
    // bridge java.util.logging to slf4j
    LogManager.getLogManager().reset();
    SLF4JBridgeHandler.install();
  }

  public static void main(String... args) throws Exception {

    Options options = new Options();

    Option input = new Option("lsp", false, "Start in lsp mode");
    input.setRequired(false);
    options.addOption(input);

    CommandLineParser parser = new BasicParser();
    HelpFormatter formatter = new HelpFormatter();
    CommandLine cmd;

    boolean lsp = false;

    try {
      cmd = parser.parse(options, args);
      lsp = cmd.hasOption("lsp");
    } catch (ParseException e) {
      System.out.println(e.getMessage());
      formatter.printHelp("cpganalysisserver", options);
      System.exit(1);
    }

    System.out.println("Analysis server starting ...");
    AnalysisServer server =
        AnalysisServer.builder()
            .config(ServerConfiguration.builder().launchLsp(lsp).launchConsole(!lsp).build())
            .build();

    server.start();
  }
}
