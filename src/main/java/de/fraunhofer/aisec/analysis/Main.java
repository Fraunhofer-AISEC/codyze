
package de.fraunhofer.aisec.analysis;

import de.fraunhofer.aisec.analysis.server.AnalysisServer;
import de.fraunhofer.aisec.analysis.structures.AnalysisContext;
import de.fraunhofer.aisec.analysis.structures.Finding;
import de.fraunhofer.aisec.analysis.structures.ServerConfiguration;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/** Start point of the standalone analysis server. */
@Command(name = "codyze", mixinStandardHelpOptions = true, version = "1.0", description = "Codyze finds security flaws in source code", sortOptions = false)
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
			"--output" }, paramLabel = "<file>", description = "Write results to file. Use -- for stdout.", defaultValue = "findings.json", showDefaultValue = CommandLine.Help.Visibility.ON_DEMAND)
	private File outputFile;

	@Option(names = { "-t",
			"--timeout" }, paramLabel = "<minutes>", description = "Terminate analysis after timeout", defaultValue = "120", showDefaultValue = CommandLine.Help.Visibility.ALWAYS)
	private long timeout;

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
					.get(timeout, TimeUnit.MINUTES);

			writeFindings(ctx.getFindings());
		}

		return 0;
	}

	private void writeFindings(Set<Finding> findings) {
		StringBuilder sb = new StringBuilder();
		for (Finding f : findings) {
			JSONObject jFinding = new JSONObject(f);
			sb.append(jFinding.toString(2));
		}

		if (outputFile.getName().equals("--")) {
			System.out.println(sb.toString());
		} else {
			//TODO Write to output file
			throw new RuntimeException("Writing to file not implemented yet");
		}

	}
}

/**
 * Codyze runs in any of three modes:
 *
 * CLI: Non-interactive command line client. Accepts arguments from command line and runs analysis.
 *
 * LSP: Bind to stdout as a server for Language Server Protocol (LSP). This mode is for IDE support.
 *
 * TUI: The text based user interface (TUI) is an interactive console that allows exploring the analyzed source code by manual queries.
 */
class Mode {
	@Option(names = "-c", required = true, description = "Start in command line mode.")
	boolean cli;
	@Option(names = "-l", required = true, description = "Start in language server protocol (LSP) mode.")
	boolean lsp;
	@Option(names = "-t", required = true, description = "Start interactive console (Text-based User Interface).")
	boolean tui;
}