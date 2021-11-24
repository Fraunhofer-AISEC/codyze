
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

	private Properties codyzeProp;
	private Properties cpgProp;
	private Properties prop;
	private CommandLine.PropertiesDefaultProvider pdp;

	public ConfigProvider(CodyzeConfigurationFile codyze, CpgConfigurationFile cpg) throws IOException {
		JavaPropsMapper propsMapper = new JavaPropsMapper();
		cpgProp = propsMapper.writeValueAsProperties(cpg);
		codyzeProp = propsMapper.writeValueAsProperties(codyze);
		prop = new Properties();
		prop.putAll(codyzeProp);
		prop.putAll(cpgProp);
		pdp = new CommandLine.PropertiesDefaultProvider(prop);
	}

	@Override
	public String defaultValue(CommandLine.Model.ArgSpec argSpec) throws Exception {

		return pdp.defaultValue(argSpec);
	}
}
