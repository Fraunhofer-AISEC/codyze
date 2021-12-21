
package de.fraunhofer.aisec.codyze;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
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
		Configuration config = parseFile(configFile);
		config.parseCLI(args);
		return config;
	}

	private static Configuration parseFile(File configFile) throws IOException {
		// parse yaml configuration file with jackson
		YAMLMapper mapper = YAMLMapper.builder().enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS).build();
		mapper.enable(JsonParser.Feature.IGNORE_UNDEFINED);
		mapper.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL);
		mapper.setPropertyNamingStrategy(new PropertyNamingStrategies.KebabCaseStrategy());

		Configuration config = null;
		try {
			config = mapper.readValue(configFile, Configuration.class);
		} catch (UnrecognizedPropertyException e) {
			printErrorMessage(
					String.format(
							"Could not parse configuration file correctly because %s is not a valid argument name for %s configurations.",
							e.getPropertyName(),
							e.getPath().get(0).getFieldName()));
		} catch (StreamReadException e) {
			printErrorMessage(e.getMessage());
		} catch (DatabindException e) {
			printErrorMessage(e.getMessage());
		} catch (IOException e) {
			printErrorMessage(e.getMessage());
		}

		return config != null ? config : new Configuration();
	}

	private static void printErrorMessage(String msg) {
		log.warn(msg + " Continue without configurations from file.");
	}

	// Parse arguments to correct config class
	private void parseCLI(String... args) {
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