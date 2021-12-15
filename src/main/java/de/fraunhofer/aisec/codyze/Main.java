
package de.fraunhofer.aisec.codyze;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import de.fraunhofer.aisec.codyze.analysis.*;
import de.fraunhofer.aisec.cpg.frontends.golang.GoLanguageFrontend;
import de.fraunhofer.aisec.cpg.frontends.python.PythonLanguageFrontend;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Model.ArgGroupSpec;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Help;
import picocli.CommandLine.Model.OptionSpec;

import java.util.List;
import java.util.Set;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import picocli.CommandLine.Unmatched;

import static picocli.CommandLine.Model.UsageMessageSpec.SECTION_KEY_OPTION_LIST;

/**
 * Start point of the standalone analysis server.
 */
@SuppressWarnings("java:S106")
public class Main {

	private static final Logger log = LoggerFactory.getLogger(Main.class);

	// TODO: Idea -> Configuration classes populated by config -> then populated by picocli (with options)

	// Order of main:
	// 1. Parse config file option with FirstPass
	// 2a. If help is requested, print help
	// 2b. If version is requested, print version
	// 2c. If config file is available, parse it into ConfigurationFile
	// 3. Parse cli options into CodyzeConfiguration and CpgConfiguration
	// 4. Call run() to start analysis setup
	public static void main(String... args) throws Exception {
		FirstPass firstPass = new FirstPass();
		CommandLine cmd = new CommandLine(firstPass);
		cmd.parseArgs(args); // first pass
		if (cmd.isUsageHelpRequested()) {
			CommandLine a = new CommandLine(new FinalPass());
			a.getHelpSectionMap().put(SECTION_KEY_OPTION_LIST, new HelpRenderer());
			a.usage(System.out);
			System.exit(0);
		} else if (cmd.isVersionHelpRequested()) {
			new CommandLine(new FinalPass()).printVersionHelp(System.out);
			System.exit(0);
		} else {
			ConfigurationFile config = null;
			if (firstPass.configFile != null && firstPass.configFile.isFile()) {
				try {
					config = parseFile(firstPass.configFile);
				}
				catch (UnrecognizedPropertyException e) {
					printErrorMessage(e);
					System.out.println("Continue without configurations from configuration file.");
					config = new ConfigurationFile();
					config.setCodyze(new CodyzeConfiguration());
					config.setCpg(new CpgConfiguration());
				}
			}
			if (config == null)
				config = new ConfigurationFile();
			if (config.getCodyzeConfig() == null)
				config.setCodyze(new CodyzeConfiguration());
			if (config.getCpgConfig() == null)
				config.setCpg(new CpgConfiguration());

			new CommandLine(config.getCodyzeConfig()).setUnmatchedArgumentsAllowed(true).execute(args);
			new CommandLine(config.getCpgConfig()).setUnmatchedArgumentsAllowed(true).execute(args);
			int exitCode = new FinalPass(config.getCodyzeConfig(), config.getCpgConfig()).call();
			System.exit(exitCode);
		}
	}

	private static ConfigurationFile parseFile(File configFile) throws IOException {
		// parse yaml configuration file with jackson
		YAMLMapper mapper = new YAMLMapper();
		mapper.setPropertyNamingStrategy(new PropertyNamingStrategies.KebabCaseStrategy());
		var configuration = mapper.readValue(configFile, ConfigurationFile.class);
		return configuration;
	}

	private static void printErrorMessage(UnrecognizedPropertyException e) {
		System.out.printf("Could not parse configuration file correctly " +
				"because '%s' is not a valid argument name for %s configurations.%n" +
				"Valid argument names are%n%s%n",
			e.getPropertyName(), e.getPath().get(0).getFieldName(), e.getKnownPropertyIds());
	}

	// Three places with cli options:
	// 1. FirstPass
	// 2. CodyzeConfiguration
	// 3. CpgConfiguration
	@Command(name = "codyze", version = "2.0.0-beta1", description = "Codyze finds security flaws in source code", sortOptions = false, usageHelpWidth = 100)
	static class FinalPass implements Callable<Integer> {
		private static final Logger log = LoggerFactory.getLogger(Main.class);

		@CommandLine.Mixin
		private FirstPass fp;

		// ArgGroup only for display purposes
		@ArgGroup(validate = false, heading = "Codyze Options\n")
		private CodyzeConfiguration codyzeConfig;

		// ArgGroup only for display purposes
		@ArgGroup(validate = false, heading = "CPG Options\n")
		private CpgConfiguration cpgConfig;

		public FinalPass(CodyzeConfiguration codyzeConfig, CpgConfiguration cpgConfig) {
			this.codyzeConfig = codyzeConfig;
			this.cpgConfig = cpgConfig;
		}

		public FinalPass() {
		}

		@Override
		public Integer call() throws Exception {
			Instant start = Instant.now();

			// we need to force load includes for unity builds, otherwise nothing will be parsed
			if (cpgConfig.isUseUnityBuild()) {
				cpgConfig.getTranslationSettings().analyzeIncludes = true;
			}

			var config = ServerConfiguration.builder()
					.launchLsp(codyzeConfig.getExecutionMode().lsp)
					.launchConsole(codyzeConfig.getExecutionMode().tui)
					.typestateAnalysis(codyzeConfig.getTypestateAnalysis().tsMode)
					.disableGoodFindings(codyzeConfig.isNoGoodFindings())
					.analyzeIncludes(cpgConfig.getTranslationSettings().analyzeIncludes)
					.includePath(cpgConfig.getTranslationSettings().includesPath)
					.useUnityBuild(cpgConfig.isUseUnityBuild())
					.markFiles(Arrays.stream(codyzeConfig.getMark()).map(File::getAbsolutePath).toArray(String[]::new));

			if (cpgConfig.getAdditionalLanguages().contains(Language.PYTHON) || cpgConfig.isEnablePython()) {
				config.registerLanguage(PythonLanguageFrontend.class, PythonLanguageFrontend.PY_EXTENSIONS);
			}

			if (cpgConfig.getAdditionalLanguages().contains(Language.GO) || cpgConfig.isEnableGo()) {
				config.registerLanguage(GoLanguageFrontend.class, GoLanguageFrontend.GOLANG_EXTENSIONS);
			}

			AnalysisServer server = AnalysisServer.builder()
					.config(config
							.build())
					.build();

			server.start();
			log.info("Analysis server started in {} in ms.", Duration.between(start, Instant.now()).toMillis());

			if (!codyzeConfig.getExecutionMode().lsp && codyzeConfig.getSource() != null) {
				log.info("Analyzing {}", codyzeConfig.getSource());
				AnalysisContext ctx = server.analyze(codyzeConfig.getSource().getAbsolutePath())
						.get(codyzeConfig.getTimeout(), TimeUnit.MINUTES);

				var findings = ctx.getFindings();

				writeFindings(findings);

				if (codyzeConfig.getExecutionMode().cli) {
					// Return code based on the existence of violations
					return findings.stream().anyMatch(Finding::isProblem) ? 1 : 0;
				}
			} else if (codyzeConfig.getExecutionMode().lsp) {
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
				log.error("Could not serialize findings: {}", e.getMessage());
			}

			if (codyzeConfig.getOutput().equals("-")) {
				System.out.println(output);
			} else {
				try (PrintWriter out = new PrintWriter(new File(codyzeConfig.getOutput()))) {
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
 *
 */
@Command(mixinStandardHelpOptions = true)
class FirstPass {
	@Option(names = {
			"--config" }, paramLabel = "<path>", description = "Parse configuration settings from file")
	File configFile;

	@Unmatched
	List<String> remainder;
}

// Custom renderer to add nesting optically with indents in help message
class HelpRenderer implements CommandLine.IHelpSectionRenderer {

	private CommandSpec spec;
	private Help help;

	@Override
	public String render(CommandLine.Help help) {

		spec = help.commandSpec();
		this.help = help;

		Map<String, CommandSpec> mix = spec.mixins();
		StringBuilder sb = new StringBuilder();
		for (CommandSpec c : mix.values()) {
			Help h = new Help(c, help.colorScheme());
			sb.append(h.optionList());
		}

		for (ArgGroupSpec group : spec.argGroups()) {
			sb.append("\n");
			addHierachy(group, sb, "");
		}

		return sb.toString();
	}

	private void addHierachy(ArgGroupSpec argGroupSpec, StringBuilder sb, String indent) {
		sb.append(indent);
		sb.append(argGroupSpec.heading());

		for (OptionSpec o : argGroupSpec.options()) {
			sb.append(indent);
			sb.append(help.optionListExcludingGroups(List.of(o)));

		}

		for (ArgGroupSpec group : argGroupSpec.subgroups()) {
			addHierachy(group, sb, indent + "    ");
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
class ExecutionMode {
	@Option(names = "-c", required = true, description = "Start in command line mode.")
	boolean cli;
	@Option(names = "-l", required = true, description = "Start in language server protocol (LSP) mode.")
	boolean lsp;
	@Option(names = "-t", required = true, description = "Start interactive console (Text-based User Interface).")
	boolean tui;
}

class AnalysisMode {
	AnalysisMode() {
	}

	@Option(names = "--typestate", paramLabel = "<NFA|WPDS>", type = TypestateMode.class, description = "Typestate analysis mode\nNFA:  Non-deterministic finite automaton (faster, intraprocedural)\nWPDS: Weighted pushdown system (slower, interprocedural)\n\t(Default: ${DEFAULT-VALUE})")
	protected static TypestateMode tsMode = TypestateMode.NFA;

	public void setTsMode(TypestateMode tsMode) {
		this.tsMode = tsMode;
	}
}

class TranslationSettings {
	@Option(names = {
			"--analyze-includes" }, description = "Enables parsing of include files. By default, if --includes are given, the parser will resolve symbols/templates from these include, but not load their parse tree. This will enforced to true, if unity builds are used.")
	protected boolean analyzeIncludes = false;

	@Option(names = { "--includes" }, description = "Path(s) containing include files. Path must be separated by : (Mac/Linux) or ; (Windows)", split = ":|;")
	protected File[] includesPath;

	public void setAnalyzeIncludes(boolean analyzeIncludes) {
		this.analyzeIncludes = analyzeIncludes;
	}

	public void setIncludesPath(File[] includesPath) {
		this.includesPath = includesPath;
	}
}
