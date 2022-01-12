
package de.fraunhofer.aisec.codyze;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.fraunhofer.aisec.codyze.analysis.*;
import de.fraunhofer.aisec.codyze.config.*;
import de.fraunhofer.aisec.codyze.sarif.SarifInstantiator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Model.ArgGroupSpec;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.Unmatched;

import static picocli.CommandLine.Model.UsageMessageSpec.SECTION_KEY_OPTION_LIST;

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
			CommandLine c = new CommandLine(new Help());
			c.getHelpSectionMap().put(SECTION_KEY_OPTION_LIST, new HelpRenderer());
			c.usage(System.out);
			System.exit(c.getCommandSpec().exitCodeOnUsageHelp());
		} else if (cmd.isVersionHelpRequested()) {
			// print version message
			CommandLine c = new CommandLine(new Help());
			c.printVersionHelp(System.out);
			System.exit(c.getCommandSpec().exitCodeOnVersionHelp());
		} else {
			Configuration config = Configuration.initConfig(firstPass.configFile, args);

			// Start analysis setup
			System.exit(start(config));
		}
	}

	// Setup and start of actual analysis
	private static int start(Configuration configuration) throws ExecutionException, InterruptedException, TimeoutException {
		Instant start = Instant.now();

		CpgConfiguration cpgConfig = configuration.getCpg();
		CodyzeConfiguration codyzeConfig = configuration.getCodyze();

		// we need to force load includes for unity builds, otherwise nothing will be parsed
		if (cpgConfig.isUseUnityBuild()) {
			cpgConfig.setAnalyzeIncludes(true);
		}

		AnalysisServer server = AnalysisServer.builder()
				.config(configuration
						.buildServerConfiguration())
				.build();

		server.start();
		log.info("Analysis server started in {} in ms.", Duration.between(start, Instant.now()).toMillis());

		if (!codyzeConfig.isLsp() && codyzeConfig.getSource() != null) {
			log.info("Analyzing {}", codyzeConfig.getSource());
			AnalysisContext ctx = server.analyze(codyzeConfig.getSource().getAbsolutePath())
					.get(codyzeConfig.getTimeout(), TimeUnit.MINUTES);

			var findings = ctx.getFindings();

			writeFindings(findings, codyzeConfig);

			if (codyzeConfig.isCli()) {
				// Return code based on the existence of violations
				return findings.stream().anyMatch(Finding::isProblem) ? 1 : 0;
			}
		} else if (codyzeConfig.isLsp()) {
			// Block main thread. Work is done in
			Thread.currentThread().join();
		}

		return 0;
	}

	private static void writeFindings(Set<Finding> findings, CodyzeConfiguration codyzeConfig) {
		// Option to generate legacy output
		String output = null;
		SarifInstantiator si = new SarifInstantiator();
		if (!codyzeConfig.isSarifOutput()) {
			var mapper = new ObjectMapper();
			try {
				output = mapper.writeValueAsString(findings);
			}
			catch (JsonProcessingException e) {
				log.error("Could not serialize findings: {}", e.getMessage());
			}
		} else {
			si.pushRun(findings);
			output = si.toString();
		}

		// Whether to write in file or on stdout
		String outputFile = codyzeConfig.getOutput();
		if (outputFile.equals("-")) {
			System.out.println(output);
		} else {
			if (!codyzeConfig.isSarifOutput()) {
				try (PrintWriter out = new PrintWriter(outputFile)) {
					out.println(output);
				}
				catch (FileNotFoundException e) {
					System.out.println(e.getMessage());
				}
			} else
				si.generateOutput(new File(outputFile));
		}
	}

	// Stores path to config file given as cli option
	@Command(mixinStandardHelpOptions = true)
	static class ConfigFilePath {
		@Option(names = {
				"--config" }, paramLabel = "<path>", description = "Parse configuration settings from file")
		File configFile;

		@Unmatched
		List<String> remainder;
	}

	// Combines all CLI Options from the different classes to be able to render a complete help message
	@Command(name = "codyze", version = "2.0.0-beta1", description = "Codyze finds security flaws in source code", sortOptions = false, usageHelpWidth = 100)
	static class Help {

		@CommandLine.Mixin
		private ConfigFilePath configFilePath;

		// ArgGroup only for display purposes
		@ArgGroup(heading = "Codyze Options\n", exclusive = false)
		private CodyzeConfiguration codyzeConfig;

		// ArgGroup only for display purposes
		@ArgGroup(heading = "CPG Options\n", exclusive = false)
		private CpgConfiguration cpgConfig;
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
				CommandLine.Help h = new CommandLine.Help(c, help.colorScheme());
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
}
