
package de.fraunhofer.aisec.codyze;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
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
import java.io.PrintWriter;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import picocli.CommandLine.Unmatched;

import static picocli.CommandLine.Model.UsageMessageSpec.SECTION_KEY_OPTION_LIST;

/**
 * Start point of the standalone analysis server.
 */
@SuppressWarnings("java:S106")
public class Main {

	private static final Logger log = LoggerFactory.getLogger(Main.class);

	/*	Order of main:
	 * 	1. Parse config file option with FirstPass
	 * 	2a. If help is requested, print help
	 * 	2b. If version is requested, print version
	 * 	2c. If config file is available, parse it into ConfigurationFile
	 * 	3. Parse cli options into CodyzeConfiguration and CpgConfiguration
	 * 	4. Call run() to start analysis setup
	*/
	public static void main(String... args) throws Exception {
		FirstPass firstPass = new FirstPass();
		CommandLine cmd = new CommandLine(firstPass);
		cmd.parseArgs(args); // first pass to get potential config file path
		if (cmd.isUsageHelpRequested()) {
			// print help message
			CommandLine c = new CommandLine(new FinalPass());
			c.getHelpSectionMap().put(SECTION_KEY_OPTION_LIST, new HelpRenderer());
			c.usage(System.out);
			System.exit(c.getCommandSpec().exitCodeOnUsageHelp());
		} else if (cmd.isVersionHelpRequested()) {
			// print version message
			CommandLine c = new CommandLine(new FinalPass());
			c.printVersionHelp(System.out);
			System.exit(c.getCommandSpec().exitCodeOnVersionHelp());
		} else {
			Configuration config = Configuration.initConfig(firstPass.configFile, args);

			// Start analysis setup
			int exitCode = new FinalPass(config.getCodyzeConfig(), config.getCpgConfig()).call();
			System.exit(exitCode);
		}
	}

	@Command(mixinStandardHelpOptions = true)
	static class FirstPass {
		@Option(names = {
				"--config" }, paramLabel = "<path>", description = "Parse configuration settings from file")
		File configFile;

		@Unmatched
		List<String> remainder;
	}

	// Three places with cli options:
	// 1. FirstPass
	// 2. CodyzeConfiguration
	// 3. CpgConfiguration
	@Command(name = "codyze", version = "2.0.0-beta1", description = "Codyze finds security flaws in source code", sortOptions = false, usageHelpWidth = 100)
	static class FinalPass {

		@CommandLine.Mixin
		private FirstPass fp;

		// ArgGroup only for display purposes
		@ArgGroup(heading = "Codyze Options\n", exclusive = false)
		private CodyzeConfiguration codyzeConfig;

		// ArgGroup only for display purposes
		@ArgGroup(heading = "CPG Options\n", exclusive = false)
		private CpgConfiguration cpgConfig;

		public FinalPass(CodyzeConfiguration codyzeConfig, CpgConfiguration cpgConfig) {
			this.codyzeConfig = codyzeConfig;
			this.cpgConfig = cpgConfig;
		}

		// Use this constructor only for printing help
		public FinalPass() {}

		// Setup and start of actual analysis
		public Integer call() throws Exception {
			Instant start = Instant.now();

			// we need to force load includes for unity builds, otherwise nothing will be parsed
			if (cpgConfig.isUseUnityBuild()) {
				cpgConfig.getTranslationSettings().analyzeIncludes = true;
			}

			var config = ServerConfiguration.builder()
					.launchLsp(codyzeConfig.getExecutionMode().lsp)
					.launchConsole(codyzeConfig.getExecutionMode().tui)
					.typestateAnalysis(AnalysisMode.tsMode)
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

	// Custom renderer to add nesting optically with indents in help message
	static class HelpRenderer implements CommandLine.IHelpSectionRenderer {

		private Help help;

		@Override
		public String render(CommandLine.Help help) {

			CommandSpec spec = help.commandSpec();
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
}




