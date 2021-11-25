
package de.fraunhofer.aisec.codyze;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.dataformat.toml.TomlMapper;
import de.fraunhofer.aisec.codyze.analysis.*;
import de.fraunhofer.aisec.cpg.frontends.golang.GoLanguageFrontend;
import de.fraunhofer.aisec.cpg.frontends.python.PythonLanguageFrontend;
import org.checkerframework.checker.units.qual.C;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import picocli.CommandLine.Unmatched;

/**
 * Start point of the standalone analysis server.
 */
@SuppressWarnings("java:S106")
public class MainTest {


	// TODO: Idea -> Configuration classes populated by config -> then populated by picocli (with options)

	public static void main(String... args) throws Exception {
		FirstPass firstPass = new FirstPass();
		CommandLine cmd = new CommandLine(firstPass);
		cmd.parseArgs(args); // first pass
		if (cmd.isUsageHelpRequested()) {
			new CommandLine(new Main()).usage(System.out);
		} else if (cmd.isVersionHelpRequested()) {
			new CommandLine(new Main()).printVersionHelp(System.out);
		} else {
			// final pass
			ConfigurationFile config;
			if (firstPass.configFile != null && firstPass.configFile.isFile()) {
				try {
					config = parseFile(firstPass.configFile);
				}
				catch (UnrecognizedPropertyException e) {
					printErrorMessage(e);
					System.out.println("Continue without configurations from configuration file.");
					config = new ConfigurationFile();
					config.setCodyze(new CodyzeConfigurationFile());
					config.setCpg(new CpgConfigurationFile());
				}
			} else {
				config = new ConfigurationFile();
				config.setCodyze(new CodyzeConfigurationFile());
				config.setCpg(new CpgConfigurationFile());
			}

			ConfigTest c = new ConfigTest();
			int a = new CommandLine(c).execute(args);
			int exitCode = new CommandLine(new FinalPass(config.getCodyzeConfig(), config.getCpgConfig()))
					.setDefaultValueProvider(new ConfigProvider(config.getCodyzeConfig(), config.getCpgConfig()))
					.execute(args);
			System.exit(exitCode);
		}
	}

	private static ConfigurationFile parseFile(File configFile) throws IOException {
		// parse toml configuration file with jackson
		TomlMapper mapper = new TomlMapper();
		mapper.setPropertyNamingStrategy(new PropertyNamingStrategies.KebabCaseStrategy());
		var configuration = mapper.readValue(configFile, ConfigurationFile.class);
		return configuration;
	}

	private static void printErrorMessage(UnrecognizedPropertyException e) {
		System.out.printf("Could not parse configuration file correctly " +
				"because '%s' is not a valid argument name for %s configurations.%n" +
				"Valid argument names are%n%s",
			e.getPropertyName(), e.getPath().get(0).getFieldName(), e.getKnownPropertyIds());
	}

	@Command(name = "codyze", mixinStandardHelpOptions = true, version = "1.5.0", description = "Codyze finds security flaws in source code", sortOptions = false, usageHelpWidth = 100)
	static class FinalPass implements Callable<Integer> {
		//		private static final Logger log = LoggerFactory.getLogger(Main.class);
		//		@CommandLine.ArgGroup(exclusive = false, heading = "Analysis settings\n")
		//		private final AnalysisMode analysisMode = new AnalysisMode();
		//		@CommandLine.ArgGroup(exclusive = false, heading = "Translation settings\n")
		//		private final TranslationSettings translationSettings = new TranslationSettings();
		//		@Spec
		//		CommandSpec spec;
		//		@CommandLine.ArgGroup(exclusive = true, multiplicity = "1", heading = "Execution mode\n")
		//		private ExecutionMode executionMode;
		//		@Option(names = { "-s", "--source" }, paramLabel = "<path>", description = "Source file or folder to analyze.")
		//		private File analysisInput;
		//		@Option(names = { "-m",
		//				"--mark" }, paramLabel = "<path>", description = "Loads MARK policy files", defaultValue = "./", showDefaultValue = CommandLine.Help.Visibility.ON_DEMAND, split = ",")
		//		private File[] markFolderNames;
		//		@Option(names = { "-o",
		//				"--output" }, paramLabel = "<file>", description = "Write results to file. Use - for stdout.", defaultValue = "findings.json", showDefaultValue = CommandLine.Help.Visibility.ON_DEMAND)
		//		private String outputFile;
		//		// TODO: Maybe default value?
		//		@Option(names = {
		//				"--config" }, paramLabel = "<path>", description = "Parse configuration settings from file")
		//		private File configFile;
		//		@Option(names = {
		//				"--timeout" }, paramLabel = "<minutes>", description = "Terminate analysis after timeout", defaultValue = "120", showDefaultValue = CommandLine.Help.Visibility.ALWAYS)
		//		private long timeout;
		//		@Option(names = {
		//				"--no-good-findings" }, description = "Disable output of \"positive\" findings which indicate correct implementations", showDefaultValue = CommandLine.Help.Visibility.ON_DEMAND)
		//		private boolean disableGoodFindings;
		//		@Option(names = {
		//				"--enable-python-support" }, description = "Enables the experimental Python support. Additional files need to be placed in certain locations. Please follow the CPG README.")
		//		private boolean enablePython;
		//		@Option(names = {
		//				"--enable-go-support" }, description = "Enables the experimental Go support. Additional files need to be placed in certain locations. Please follow the CPG README.")
		//		private boolean enableGo;

		private ConfigTest c;

		private CodyzeConfigurationFile codyzeConfig;
		private CpgConfigurationFile cpgConfig;

		public FinalPass(CodyzeConfigurationFile codyzeConfig, CpgConfigurationFile cpgConfig) {
			this.codyzeConfig = codyzeConfig;
			this.cpgConfig = cpgConfig;
		}

		@Override
		public Integer call() throws Exception {
			Instant start = Instant.now();

			// TODO: Maybe not needed, because default value is set in Option
			if (c.analysisMode.tsMode == null) {
				c.analysisMode.tsMode = TypestateMode.NFA;
			}

			var config = ServerConfiguration.builder()
					.launchLsp(c.executionMode.lsp)
					.launchConsole(c.executionMode.tui)
					.typestateAnalysis(c.analysisMode.tsMode)
					.disableGoodFindings(c.disableGoodFindings)
					.analyzeIncludes(c.translationSettings.analyzeIncludes)
					.includePath(c.translationSettings.includesPath)
					.markFiles(Arrays.stream(c.markFolderNames).map(File::getAbsolutePath).toArray(String[]::new));

			if (c.enablePython) {
				config.registerLanguage(PythonLanguageFrontend.class, PythonLanguageFrontend.PY_EXTENSIONS);
			}

			if (c.enableGo) {
				config.registerLanguage(GoLanguageFrontend.class, GoLanguageFrontend.GOLANG_EXTENSIONS);
			}

			AnalysisServer server = AnalysisServer.builder()
					.config(config
							.build())
					.build();

			server.start();
			c.log.info("Analysis server started in {} in ms.", Duration.between(start, Instant.now()).toMillis());

			if (!c.executionMode.lsp && c.analysisInput != null) {
				c.log.info("Analyzing {}", c.analysisInput);
				AnalysisContext ctx = server.analyze(c.analysisInput.getAbsolutePath())
						.get(c.timeout, TimeUnit.MINUTES);

				var findings = ctx.getFindings();

				writeFindings(findings);

				if (c.executionMode.cli) {
					// Return code based on the existence of violations
					return findings.stream().anyMatch(Finding::isProblem) ? 1 : 0;
				}
			} else if (c.executionMode.lsp) {
				// Block main thread. Work is done in
				Thread.currentThread().join();
			}

			return 0;
		}

		private void writeFindings(Set<Finding> findings) {
			var mapper = new ObjectMapper();
			String output = null;
			try {
				output = mapper.writeValueAsString(findings);
			}
			catch (JsonProcessingException e) {
				c.log.error("Could not serialize findings: {}", e.getMessage());
			}

			if (c.outputFile.equals("-")) {
				System.out.println(output);
			} else {
				try (PrintWriter out = new PrintWriter(new File(c.outputFile))) {
					out.println(output);
				}
				catch (FileNotFoundException e) {
					System.out.println(e.getMessage());
				}
			}

		}
	}

}

/**
 * Codyze runs in any of three modes:
 * <p>
 * CLI: Non-interactive command line client. Accepts arguments from command line and runs analysis.
 * <p>
 * LSP: Bind to stdout as a server for Language Server Protocol (LSP). This mode is for IDE support.
 * <p>
 * TUI: The text based user interface (TUI) is an interactive console that allows exploring the analyzed source code by manual queries.
 */
class ExecutionModeTest {
	@Option(names = "-c", required = true, description = "Start in command line mode.")
	boolean cli;
	@Option(names = "-l", required = true, description = "Start in language server protocol (LSP) ModeTest.")
	boolean lsp;
	@Option(names = "-t", required = true, description = "Start interactive console (Text-based User Interface).")
	boolean tui;
}

class AnalysisModeTest {

	@Option(names = "--typestate", paramLabel = "<NFA|WPDS>", defaultValue = "NFA", type = TypestateMode.class, description = "Typestate analysis mode\nNFA:  Non-deterministic finite automaton (faster, intraprocedural)\nWPDS: Weighted pushdown system (slower, interprocedural)")
	//@CommandLine.ArgGroup(exclusive = true, multiplicity = "1", heading = "Typestate Analysis\n")
	protected TypestateMode tsMode = TypestateMode.NFA;
}

class TranslationSettingsTest {
	@Option(names = {
			"--analyze-includes" }, description = "Enables parsing of include files. By default, if --includes are given, the parser will resolve symbols/templates from these include, but not load their parse tree.")
	protected boolean analyzeIncludes = false;

	@Option(names = { "--includes" }, description = "Path(s) containing include files. Path must be separated by : (Mac/Linux) or ; (Windows)", split = ":|;")
	protected File[] includesPath;
}

@Command(mixinStandardHelpOptions = true)
class FirstPass {
	@Option(names = {
			"--config" }, paramLabel = "<path>", description = "Parse configuration settings from file")
	File configFile;

	@Unmatched
	List<String> remainder;
}
