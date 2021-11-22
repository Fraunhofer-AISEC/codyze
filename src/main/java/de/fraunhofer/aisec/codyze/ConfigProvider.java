
package de.fraunhofer.aisec.codyze;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.dataformat.toml.TomlMapper;
import picocli.CommandLine;
import picocli.CommandLine.IDefaultValueProvider;
import picocli.CommandLine.Spec;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Model.OptionSpec;

public class ConfigProvider implements IDefaultValueProvider {

	private File configFile;
	private CodyzeConfigurationFile codyze;
	private CpgConfigurationFile cpg;

	public ConfigProvider(File configFile) throws IOException {
		this.configFile = configFile;
		if (configFile != null && configFile.isFile()) {
			try {
				parseFile();
			}
			catch (UnrecognizedPropertyException e) {
				printErrorMessage(e);
				System.out.println("Continue without configurations from configuration file.");
			}
		}
	}

	@Override
	public String defaultValue(CommandLine.Model.ArgSpec argSpec) throws Exception {
		if (argSpec.isOption()) {
			OptionSpec o = (OptionSpec) argSpec;

			switch (o.shortestName()) {
				case ("-c"):
				case ("-l"):
				case ("-t"):
					return "false";
				case ("-s"):
					if (codyze != null && codyze.getSource() != null)
						return codyze.getSource().toString();
					break;
				case ("-m"):
					if (codyze != null && codyze.getMark() != null)
						return codyze.getMark().toString();
					return "./";
				case ("-o"):
					if (codyze != null && codyze.getOutput() != null)
						return codyze.getOutput();
					break;
				case ("--timeout"):
					if (codyze != null && codyze.getTimeout() != null)
						return codyze.getTimeout().toString();
					return "120";
				case ("--no-good-findings"):
					if (codyze != null)
						return String.valueOf(codyze.isNoGoodFindings());
					return "false";
				case ("--enable-python-support"):
					if (cpg != null && cpg.getAdditionalLanguages() != null && Arrays.stream(cpg.getAdditionalLanguages()).anyMatch(x -> x.equals("python")))
						return "true";
					return "false";
				case ("--enable-go-support"):
					if (cpg != null && cpg.getAdditionalLanguages() != null && Arrays.stream(cpg.getAdditionalLanguages()).anyMatch(x -> x.equals("go")))
						return "true";
					return "false";
				case ("--typestate"):
					if (codyze != null && codyze.getTypestateAnalysis() != null)
						return String.valueOf(codyze.getTypestateAnalysis());
					return "NFA";
				case ("--analyze-includes"):
					if (cpg != null)
						return String.valueOf(cpg.isAnalyzeIncludes());
					return "false";
				case ("--includes"):
					if (cpg != null && cpg.getIncludePaths() != null)
						return cpg.getIncludePaths().toString();
					break;
				default:
			}
		}

		return null;
	}

	private void parseFile() throws IOException {
		// parse toml configuration file with jackson
		TomlMapper mapper = new TomlMapper();
		var configuration = mapper.readValue(configFile, ConfigurationFile.class);
		this.codyze = configuration.getCodyzeConfig();
		this.cpg = configuration.getCpgConfig();
		cpg.standardizeLanguages();
	}

	private void printErrorMessage(UnrecognizedPropertyException e) {
		System.out.printf("Could not parse configuration file correctly " +
				"because '%s' is not a valid argument name for %s configurations.%n" +
				"Valid argument names are%n%s",
			e.getPropertyName(), e.getPath().get(0).getFieldName(), e.getKnownPropertyIds());
	}
}
