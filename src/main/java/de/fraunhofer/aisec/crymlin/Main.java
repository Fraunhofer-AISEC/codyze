package de.fraunhofer.aisec.crymlin;

import de.fraunhofer.aisec.crymlin.server.AnalysisServer;
import de.fraunhofer.aisec.crymlin.server.ServerConfiguration;
import java.util.logging.LogManager;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

/** Start point of the standalone analysis server. */
public class Main {

  private static final Logger log = LoggerFactory.getLogger(Main.class);

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

    Option markoption =
        new Option("mark", true, "Folder to mark files, or one mark file to be loaded");
    markoption.setRequired(false);
    options.addOption(markoption);

    CommandLineParser parser = new BasicParser();
    HelpFormatter formatter = new HelpFormatter();
    CommandLine cmd;

    boolean lsp = false;
    String markFolderName = null;

    try {
      cmd = parser.parse(options, args);
      lsp = cmd.hasOption("lsp");
      markFolderName = cmd.getOptionValue("mark"); // can be a folder or a file
    } catch (ParseException e) {
      System.out.println(e.getMessage());
      formatter.printHelp("cpganalysisserver", options);
      System.exit(1);
    }

    if (markFolderName != null) {
      log.info("Loading MARK files from " + markFolderName);
    } else {
      log.info("Do not load any MARK files.");
    }

    log.info("Analysis server starting ...");
    AnalysisServer server =
        AnalysisServer.builder()
            .config(
                ServerConfiguration.builder()
                    .launchLsp(lsp)
                    .launchConsole(!lsp)
                    .markFiles(markFolderName)
                    .build())
            .build();

    server.start();
  }
}
