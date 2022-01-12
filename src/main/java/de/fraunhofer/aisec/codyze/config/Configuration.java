
package de.fraunhofer.aisec.codyze.config;

import de.fraunhofer.aisec.codyze.analysis.ServerConfiguration;
import de.fraunhofer.aisec.codyze.analysis.passes.EdgeCachePass;
import de.fraunhofer.aisec.codyze.analysis.passes.IdentifierPass;
import de.fraunhofer.aisec.cpg.TranslationConfiguration;
import de.fraunhofer.aisec.cpg.frontends.golang.GoLanguageFrontend;
import de.fraunhofer.aisec.cpg.frontends.python.PythonLanguageFrontend;

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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Configuration {
	private static final Logger log = LoggerFactory.getLogger(Configuration.class);

	private CodyzeConfiguration codyze = new CodyzeConfiguration();
	private CpgConfiguration cpg = new CpgConfiguration();

	public static Configuration initConfig(File configFile, String... args) {
		Configuration config;

		if (configFile != null)
			config = parseFile(configFile);
		else
			config = new Configuration();

		config.parseCLI(args);

		return config;
	}

	// parse yaml configuration file with jackson
	private static Configuration parseFile(File configFile) {
		YAMLMapper mapper = YAMLMapper.builder().enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS).build();
		mapper.enable(JsonParser.Feature.IGNORE_UNDEFINED);
		mapper.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL);
		mapper.setPropertyNamingStrategy(new PropertyNamingStrategies.KebabCaseStrategy());

		Configuration config = null;
		try {
			config = mapper.readValue(configFile, Configuration.class);
		}
		catch (UnrecognizedPropertyException e) {
			printErrorMessage(
				String.format(
					"Could not parse configuration file correctly because %s is not a valid argument name for %s configurations.",
					e.getPropertyName(),
					e.getPath().get(0).getFieldName()));
		}
		catch (FileNotFoundException e) {
			printErrorMessage(String.format("File at %s not found.", configFile.getAbsolutePath()));
		}
		catch (IOException e) {
			printErrorMessage(e.getMessage());
		}

		return config != null ? config : new Configuration();
	}

	private static void printErrorMessage(String msg) {
		log.warn("{} Continue without configurations from file.", msg);
	}

	// Parse arguments to correct config class
	private void parseCLI(String... args) {
		// setUnmatchedArgumentsAllowed is true because both classes don't have the complete set of options and would cause exceptions
		// side effect is that all unknown options are ignored
		new CommandLine(codyze).setUnmatchedArgumentsAllowed(true).parseArgs(args);
		new CommandLine(cpg).setUnmatchedArgumentsAllowed(true).parseArgs(args);
	}

	public ServerConfiguration buildServerConfiguration() {
		var config = ServerConfiguration.builder()
				.launchLsp(codyze.getExecutionMode().lsp)
				.launchConsole(codyze.getExecutionMode().tui)
				.typestateAnalysis(AnalysisMode.tsMode)
				.disableGoodFindings(codyze.isNoGoodFindings())
				.markFiles(Arrays.stream(codyze.getMark()).map(File::getAbsolutePath).toArray(String[]::new));

		config.analyzeIncludes(cpg.getTranslation().analyzeIncludes)
				.includePath(cpg.getTranslation().includes)
				.useUnityBuild(cpg.isUseUnityBuild());

		if (cpg.getAdditionalLanguages().contains(Language.PYTHON) || cpg.isEnablePython()) {
			config.registerLanguage(PythonLanguageFrontend.class, PythonLanguageFrontend.PY_EXTENSIONS);
		}

		if (cpg.getAdditionalLanguages().contains(Language.GO) || cpg.isEnableGo()) {
			config.registerLanguage(GoLanguageFrontend.class, GoLanguageFrontend.GOLANG_EXTENSIONS);
		}

		return config.build();
	}

	public TranslationConfiguration buildTranslationConfiguration() {
		List<File> files = new ArrayList<>();
		files.add(new File(codyze.getSource().getAbsolutePath()));

		var translationConfig = TranslationConfiguration.builder()
				.debugParser(true)
				.failOnError(false)
				.codeInNodes(true)
				.loadIncludes(cpg.getTranslation().analyzeIncludes)
				.useUnityBuild(cpg.isUseUnityBuild())
				.defaultPasses()
				.defaultLanguages()
				.registerPass(new IdentifierPass())
				.registerPass(new EdgeCachePass())
				.sourceLocations(files.toArray(new File[0]));

		if (cpg.getAdditionalLanguages().contains(Language.PYTHON) || cpg.isEnablePython()) {
			translationConfig.registerLanguage(PythonLanguageFrontend.class, PythonLanguageFrontend.PY_EXTENSIONS);
		}

		if (cpg.getAdditionalLanguages().contains(Language.GO) || cpg.isEnableGo()) {
			translationConfig.registerLanguage(GoLanguageFrontend.class, GoLanguageFrontend.GOLANG_EXTENSIONS);
		}

		for (File file : cpg.getTranslation().includes)
			translationConfig.includePath(file.getAbsolutePath());

		return translationConfig.build();

	}

	public CodyzeConfiguration getCodyze() {
		return codyze;
	}

	public void setCodyze(CodyzeConfiguration codyze) {
		this.codyze = codyze;
	}

	public CpgConfiguration getCpg() {
		return cpg;
	}

	public void setCpg(CpgConfiguration cpg) {
		this.cpg = cpg;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Configuration that = (Configuration) o;
		return Objects.equals(codyze, that.codyze) && Objects.equals(cpg, that.cpg);
	}
}