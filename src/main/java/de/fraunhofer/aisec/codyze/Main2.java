
package de.fraunhofer.aisec.codyze;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.dataformat.toml.TomlMapper;
import de.fraunhofer.aisec.codyze.analysis.*;
import de.fraunhofer.aisec.cpg.frontends.golang.GoLanguageFrontend;
import de.fraunhofer.aisec.cpg.frontends.python.PythonLanguageFrontend;
import org.python.antlr.ast.Str;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Model.ArgGroupSpec;
import picocli.CommandLine.Help.IOptionRenderer;
import picocli.CommandLine.Help.IParamLabelRenderer;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Help;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Ansi.Text;
import picocli.CommandLine.IHelpSectionRenderer;
import picocli.CommandLine.Model.ArgGroupSpec;
import picocli.CommandLine.Model.ArgSpec;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.Option;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import picocli.CommandLine.Help.TextTable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help;
import picocli.CommandLine.Help.Column;
import picocli.CommandLine.Help.Column.Overflow;
import picocli.CommandLine.Help.TextTable;
import picocli.CommandLine.IHelpSectionRenderer;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.UsageMessageSpec;
import picocli.CommandLine.Unmatched;

import static picocli.CommandLine.Model.UsageMessageSpec.SECTION_KEY_OPTION_LIST;

/**
 * Start point of the standalone analysis server.
 */
@SuppressWarnings("java:S106")
public class Main2 {

	// TODO: Idea -> Configuration classes populated by config -> then populated by picocli (with options)

	public static void main(String... args) throws Exception {
		FirstPass firstPass = new FirstPass();
		CommandLine cmd = new CommandLine(firstPass);
		cmd.parseArgs(args); // first pass
		if (cmd.isUsageHelpRequested()) {
			CommandLine a = new CommandLine(new FinalPass(null, null));
			a.getHelpSectionMap().put(SECTION_KEY_OPTION_LIST, new HelpRenderer());
			a.usage(System.out);
		} else if (cmd.isVersionHelpRequested()) {
			new CommandLine(new ConfigTest()).printVersionHelp(System.out);
		} else {
			// final pass
			ConfigurationFile2 config;
			if (firstPass.configFile != null && firstPass.configFile.isFile()) {
				try {
					config = parseFile(firstPass.configFile);
				}
				catch (UnrecognizedPropertyException e) {
					printErrorMessage(e);
					System.out.println("Continue without configurations from configuration file.");
					config = new ConfigurationFile2();
					config.setCodyze(new CodyzeConfigurationFile2());
					config.setCpg(new CpgConfigurationFile2());
				}
			} else {
				config = new ConfigurationFile2();
				config.setCodyze(new CodyzeConfigurationFile2());
				config.setCpg(new CpgConfigurationFile2());
			}

			int a = new CommandLine(config.getCodyzeConfig()).setDefaultValueProvider(new ConfigProvider(config.getCodyzeConfig(), config.getCpgConfig()))
					.execute(args);
			a = new CommandLine(config.getCpgConfig()).setDefaultValueProvider(new ConfigProvider(config.getCodyzeConfig(), config.getCpgConfig()))
					.execute(args);
			int exitCode = new CommandLine(new FinalPass(config.getCodyzeConfig(), config.getCpgConfig()))
					.execute(args);
			System.exit(exitCode);
		}
	}

	private static ConfigurationFile2 parseFile(File configFile) throws IOException {
		// parse toml configuration file with jackson
		TomlMapper mapper = new TomlMapper();
		mapper.setPropertyNamingStrategy(new PropertyNamingStrategies.KebabCaseStrategy());
		var configuration = mapper.readValue(configFile, ConfigurationFile2.class);
		return configuration;
	}

	private static void printErrorMessage(UnrecognizedPropertyException e) {
		System.out.printf("Could not parse configuration file correctly " +
				"because '%s' is not a valid argument name for %s configurations.%n" +
				"Valid argument names are%n%s",
			e.getPropertyName(), e.getPath().get(0).getFieldName(), e.getKnownPropertyIds());
	}

	@Command(name = "codyze", version = "1.5.0", description = "Codyze finds security flaws in source code", sortOptions = false, usageHelpWidth = 100)
	static class FinalPass implements Callable<Integer> {
		private static final Logger log = LoggerFactory.getLogger(Main2.class);

		private ConfigTest c;

		@CommandLine.Mixin
		private FirstPass fp;

		@ArgGroup(validate = false, heading = "Codyze Options\n")
		private CodyzeConfigurationFile2 codyzeConfig;

		@ArgGroup(validate = false, heading = "CPG Options\n")
		private CpgConfigurationFile2 cpgConfig;

		public FinalPass(CodyzeConfigurationFile2 codyzeConfig, CpgConfigurationFile2 cpgConfig) {
			this.codyzeConfig = codyzeConfig;
			this.cpgConfig = cpgConfig;
		}

		public FinalPass(CodyzeConfigurationFile2 codyzeConfig, CpgConfigurationFile2 cpgConfig, ConfigTest c) {
			this.codyzeConfig = codyzeConfig;
			this.cpgConfig = cpgConfig;
			this.c = c;
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
			log.info("Analysis server started in {} in ms.", Duration.between(start, Instant.now()).toMillis());

			if (!c.executionMode.lsp && c.analysisInput != null) {
				log.info("Analyzing {}", c.analysisInput);
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
				log.error("Could not serialize findings: {}", e.getMessage());
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

@Command(mixinStandardHelpOptions = true)
class FirstPass {
	@Option(names = {
			"--config" }, paramLabel = "<path>", description = "Parse configuration settings from file")
	File configFile;

	@Unmatched
	List<String> remainder;
}

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
