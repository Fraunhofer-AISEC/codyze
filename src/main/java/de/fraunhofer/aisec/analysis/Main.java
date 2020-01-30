
package de.fraunhofer.aisec.analysis;

import de.fraunhofer.aisec.analysis.server.AnalysisServer;
import de.fraunhofer.aisec.analysis.structures.AnalysisContext;
import de.fraunhofer.aisec.analysis.structures.Finding;
import de.fraunhofer.aisec.analysis.structures.ServerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/** Start point of the standalone analysis server. */
@Command(name = "codyze", mixinStandardHelpOptions = true, version = "1.0", description = "Codyze checks source code security best practices", sortOptions = false)
public class Main implements Callable<Integer> {
	private static final Logger log = LoggerFactory.getLogger(Main.class);

	@CommandLine.ArgGroup(exclusive = true, multiplicity = "1", heading = "Execution mode\n")
	private Mode mode;

	@Option(names = { "-i", "--interproc" }, description = "Enables interprocedural analysis (more precise but slower).")
	private boolean interproc;

	@Option(names = { "-s", "--source" }, paramLabel = "<path>", description = "Source file or folder to analyze.")
	private File analysisInput;

	@Option(names = { "-m",
			"--mark" }, paramLabel = "<path>", description = "Load MARK policy files from folder", defaultValue = "./", showDefaultValue = CommandLine.Help.Visibility.ON_DEMAND)
	private File markFolderName;

	@Option(names = { "-o",
			"--output" }, paramLabel = "<file>", description = "Write results to file", defaultValue = "findings.json", showDefaultValue = CommandLine.Help.Visibility.ON_DEMAND)
	private File outputFile;

	public static void main(String... args) {
		int exitCode = new CommandLine(new Main()).execute(args);
		System.exit(exitCode);
	}

	@Override
	public Integer call() throws Exception {
		Instant start = Instant.now();

		AnalysisServer server = AnalysisServer.builder()
				.config(ServerConfiguration.builder()
						.launchLsp(mode.lsp)
						.launchConsole(mode.tui)
						.markFiles(markFolderName.getAbsolutePath())
						.build())
				.build();

		server.start();
		log.info("Analysis server started in {} in ms.", Duration.between(start, Instant.now()).toMillis());

		if (analysisInput != null) {
			log.info("Analyzing {}", analysisInput);
			AnalysisContext ctx = server.analyze(analysisInput.getAbsolutePath())
					.get(100, TimeUnit.MINUTES);
			for (Finding f : ctx.getFindings()) {
				System.out.println(f.toString());
			}
		}

		return 0;
	}
}

class Mode {
	@Option(names = "-c", required = true, description = "Start in command line mode.")
	boolean cli;
	@Option(names = "-l", required = true, description = "Start in language server protocol (LSP) mode.")
	boolean lsp;
	@Option(names = "-t", required = true, description = "Start interactive console (Text-based User Interface).")
	boolean tui;
}