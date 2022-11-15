
package de.fraunhofer.aisec.codyze;

import de.fraunhofer.aisec.codyze.analysis.*;
import de.fraunhofer.aisec.codyze.config.*;
import de.fraunhofer.aisec.codyze.printer.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Model.ArgGroupSpec;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Unmatched;
import static picocli.CommandLine.Model.UsageMessageSpec.SECTION_KEY_OPTION_LIST;

import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * Start point of the standalone analysis server.
 */

/*
	Three places with cli options:
	1. ConfigFilePath
	2. CodyzeConfiguration
	3. CpgConfiguration
 */
@SuppressWarnings("java:S106")
public class Main {
	private static final Logger log = LoggerFactory.getLogger(Main.class);

	/*	Order of main:
	 * 	1. Parse config file option with ConfigFilePath
	 * 	2.1 If help is requested, print help
	 * 	2.2 If version is requested, print version
	 * 	2.3.1 Else parse and merge file and cli options into ConfigurationFile
	 * 	2.3.2 Call start() to start analysis setup
	 */
	public static void main(String... args) throws Exception {
		ConfigFilePath firstPass = new ConfigFilePath();
		CommandLine cmd = new CommandLine(firstPass);
		cmd.parseArgs(args); // first pass to get potential config file path
		if (cmd.isUsageHelpRequested()) {
			// print help message
			Help help = new Help();
			System.setProperty("source", Arrays.toString(help.configuration.getSource()));
			System.setProperty("mark", Arrays.toString(help.codyzeConfig.getMark()));

			CommandLine c = new CommandLine(help);
			c.getHelpSectionMap().put(SECTION_KEY_OPTION_LIST, new HelpRenderer());
			c.usage(System.out);
			System.exit(c.getCommandSpec().exitCodeOnUsageHelp());
		} else if (cmd.isVersionHelpRequested()) {
			// print version message
			CommandLine c = new CommandLine(new Help());
			c.printVersionHelp(System.out);
			System.exit(c.getCommandSpec().exitCodeOnVersionHelp());
		} else {
			int returnCode = 0;

			try {
				Configuration config = Configuration.initConfig(firstPass.configFile, firstPass.remainder.toArray(new String[0]));
				// Start analysis setup
				returnCode = start(config);
			}
			catch (CommandLine.MissingParameterException missingParameterException) {
				missingParameterException.getCommandLine().getErr().println(missingParameterException.getMessage());

				// print help message
				CommandLine c = new CommandLine(new Help());
				c.getHelpSectionMap().put(SECTION_KEY_OPTION_LIST, new HelpRenderer());
				c.usage(c.getErr());

				returnCode = missingParameterException.getCommandLine().getCommandSpec().exitCodeOnInvalidInput();
			}
			System.exit(returnCode);
		}
	}

	// Setup and start of actual analysis
	private static int start(Configuration configuration) throws ExecutionException, InterruptedException, TimeoutException {
		Instant start = Instant.now();

		AnalysisServer server = new AnalysisServer(configuration);

		server.start();
		log.info("Analysis server started in {} in ms.", Duration.between(start, Instant.now()).toMillis());

		if (!configuration.getExecutionMode().isLsp()) {
			log.info("Analyzing sources {} excluding {}", Arrays.toString(configuration.getSource()), Arrays.toString(configuration.getDisabledSource()));
			AnalysisContext ctx = server
					.analyze(configuration.getSource())
					.get(configuration.getTimeout(), TimeUnit.MINUTES);

			var findings = ctx.getFindings();

			writeFindings(findings, configuration);

			if (configuration.getExecutionMode().isCli()) {
				// Return code based on the existence of violations
				return findings.stream().anyMatch(Finding::isProblem) ? 1 : 0;
			}
		} else if (configuration.getExecutionMode().isLsp()) {
			// Block main thread. Work is done in
			Thread.currentThread().join();
		}

		return 0;
	}

	private static void writeFindings(Set<Finding> findings, Configuration configuration) {
		Printer printer;
		// whether to print sarif output or not
		if (!configuration.getSarifOutput())
			printer = new LegacyPrinter(findings);
		else
			printer = new SarifPrinter(findings);

		String outputFile = configuration.getOutput();
		// Whether to write in file or on stdout
		if (outputFile.equals("-"))
			printer.printToConsole();
		else
			printer.printToFile(outputFile);
	}

	// Stores path to config file given as cli option
	@Command(mixinStandardHelpOptions = true)
	public static class ConfigFilePath {
		@Option(names = {
				"--config" }, paramLabel = "<path>", fallbackValue = "codyze.yaml", arity = "0..1", description = "Parse configuration settings from this file. If no file path is specified, codyze will try to load the configuration file from ${FALLBACK-VALUE}")
		public File configFile;

		@Unmatched
		public List<String> remainder = new ArrayList<>();
	}

	// Combines all CLI Options from the different classes to be able to render a complete help message
	@Command(name = "codyze", versionProvider = ManifestVersionProvider.class, description = "Codyze finds security flaws in source code", sortOptions = false, usageHelpWidth = 100)
	static class Help {

		@CommandLine.Mixin
		private ConfigFilePath configFilePath;

		// ArgGroups only for display purposes
		@ArgGroup(heading = "@|bold,underline Codyze Options|@\n", exclusive = false, validate = true)
		private CodyzeConfiguration codyzeConfig = new CodyzeConfiguration();

		@ArgGroup(exclusive = false)
		private Configuration configuration = new Configuration();

		@ArgGroup(exclusive = false, heading = "Analysis Options\n")
		AnalysisMode analysis = new AnalysisMode();

		@ArgGroup(heading = "@|bold,underline CPG Options|@\n", exclusive = false)
		private CpgConfiguration cpgConfig = new CpgConfiguration();

		@ArgGroup(exclusive = false, heading = "Translation Options\n")
		private TranslationSettings translation = new TranslationSettings();
	}

	// Custom renderer to add nesting optically with indents in help message
	static class HelpRenderer implements CommandLine.IHelpSectionRenderer {

		private CommandLine.Help help;

		@Override
		public String render(CommandLine.Help help) {
			CommandSpec spec = help.commandSpec();
			this.help = help;

			Map<String, CommandSpec> mix = spec.mixins();
			StringBuilder sb = new StringBuilder();
			for (CommandSpec c : mix.values()) {
				sb.append(help.optionListExcludingGroups(c.options().stream().filter(optionSpec -> optionSpec.group() == null).collect(Collectors.toList())));
			}

			for (ArgGroupSpec group : spec.argGroups()) {
				addHierachy(group, sb);
			}

			return sb.toString();
		}

		private void addHierachy(ArgGroupSpec argGroupSpec, StringBuilder sb) {
			if (argGroupSpec.heading() != null) {
				sb.append("\n");
				sb.append(help.colorScheme().text(argGroupSpec.heading()).toString());
			}

			// render all options that are not in subgroups
			sb.append(help.optionListExcludingGroups(
				argGroupSpec.options().stream().filter(optionSpec -> optionSpec.group().equals(argGroupSpec)).collect(Collectors.toList())));

			for (ArgGroupSpec group : argGroupSpec.subgroups()) {
				addHierachy(group, sb);
			}
		}
	}
}