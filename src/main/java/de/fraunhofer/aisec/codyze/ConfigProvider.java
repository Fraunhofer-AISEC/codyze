
package de.fraunhofer.aisec.codyze;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.dataformat.toml.TomlMapper;
import picocli.CommandLine;
import picocli.CommandLine.IDefaultValueProvider;
import picocli.CommandLine.Spec;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Model.OptionSpec;

import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper;

public class ConfigProvider implements IDefaultValueProvider {

	private CodyzeConfigurationFile codyze;
	private CpgConfigurationFile cpg;
	private Properties prop;
	private CommandLine.PropertiesDefaultProvider defaultProvider;

	public ConfigProvider(CodyzeConfigurationFile codyze, CpgConfigurationFile cpg) throws IOException {
		this.codyze = codyze;
		this.cpg = cpg;
		JavaPropsMapper propsMapper = new JavaPropsMapper();
		prop = new Properties();
		prop.putAll(propsMapper.writeValueAsProperties(cpg));
		prop.putAll(propsMapper.writeValueAsProperties(codyze));
		defaultProvider = new CommandLine.PropertiesDefaultProvider(prop);
	}

	@Override
	public String defaultValue(CommandLine.Model.ArgSpec argSpec) throws Exception {

		if (argSpec.isOption()) {
			OptionSpec o = (OptionSpec) argSpec;
			switch (o.shortestName()) {
				case "-m":
					if (codyze != null) {
						StringBuilder sb = new StringBuilder();
						File[] markPaths = codyze.getMark();
						for (File f : markPaths) {
							//							sb.append("\"");
							sb.append(f.toString());
							sb.append(",");
						}
						return sb.toString().trim();
					} else
						return "./";
				case "--enable-python-support":
					if (cpg != null && cpg.getAdditionalLanguages() != null && cpg.getAdditionalLanguages().contains(Language.PYTHON))
						return "true";
					else
						return "false";
				case "--enable-go-support":
					if (cpg != null && cpg.getAdditionalLanguages() != null && cpg.getAdditionalLanguages().contains(Language.GO))
						return "true";
					else
						return "false";
			}
		}
		return defaultProvider.defaultValue(argSpec);
	}
}
