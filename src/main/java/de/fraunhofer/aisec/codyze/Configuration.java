
package de.fraunhofer.aisec.codyze;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;

public class Configuration {
	private static final Logger log = LoggerFactory.getLogger(Configuration.class);

	private CodyzeConfiguration codyze = new CodyzeConfiguration();
	private CpgConfiguration cpg = new CpgConfiguration();

	public static Configuration initConfig(File configFile, String... args) throws IOException {
		Configuration config;
		if (configFile != null && configFile.isFile()) {
			// parse config file
			try {
				config = Configuration.parseFile(configFile);
			}
			catch (UnrecognizedPropertyException e) {
				printErrorMessage(e);
				config = new Configuration();
			}
		} else {
			config = new Configuration();
		}

		config.parseCLI(args);

		return config;
	}

	private static Configuration parseFile(File configFile) throws IOException {
		// parse yaml configuration file with jackson
		YAMLMapper mapper = YAMLMapper.builder().enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS).build();
		mapper.enable(JsonParser.Feature.IGNORE_UNDEFINED);
		mapper.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL);
		mapper.setPropertyNamingStrategy(new PropertyNamingStrategies.KebabCaseStrategy());
		return mapper.readValue(configFile, Configuration.class);
	}

	private static void printErrorMessage(UnrecognizedPropertyException e) {
		log.warn("Could not parse configuration file correctly " +
				"because {} is not a valid argument name for {} configurations.\n" +
				"Valid argument names are\n{}",
			e.getPropertyName(), e.getPath().get(0).getFieldName(), e.getKnownPropertyIds());
		log.warn("Continue without configurations from configuration file.\n");
	}

	private void parseCLI(String... args) {
		// Parse arguments to correct config class by calling execute
		// setUnmatchedArgumentsAllowed is true because both classes don't have the complete set of options and would cause exceptions
		// side effect is that all unknown options are ignored
		new CommandLine(codyze).setUnmatchedArgumentsAllowed(true).parseArgs(args);
		new CommandLine(cpg).setUnmatchedArgumentsAllowed(true).parseArgs(args);
	}

	public CodyzeConfiguration getCodyzeConfig() {
		return codyze;
	}

	public void setCodyze(CodyzeConfiguration codyze) {
		this.codyze = codyze;
	}

	public CpgConfiguration getCpgConfig() {
		return cpg;
	}

	public void setCpg(CpgConfiguration cpg) {
		this.cpg = cpg;
	}
}