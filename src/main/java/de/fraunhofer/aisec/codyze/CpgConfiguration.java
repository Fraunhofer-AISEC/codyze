
package de.fraunhofer.aisec.codyze;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import picocli.CommandLine;
import picocli.CommandLine.Option;

import java.util.*;
import java.util.concurrent.Callable;

public class CpgConfiguration implements Callable<Integer> {

	@JsonDeserialize(using = LanguageDeseralizer.class)
	private EnumSet<Language> additionalLanguages = EnumSet.noneOf(Language.class);

	// TODO: maybe change to enum set instead of booleans for each language
	@JsonIgnore
	@Option(names = {
			"--enable-python-support" }, description = "Enables the experimental Python support. Additional files need to be placed in certain locations. Please follow the CPG README.")
	private boolean enablePython;

	@JsonIgnore
	@Option(names = {
			"--enable-go-support" }, description = "Enables the experimental Go support. Additional files need to be placed in certain locations. Please follow the CPG README.")
	private boolean enableGo;

	@CommandLine.ArgGroup(exclusive = false, heading = "Translation settings\n")
	private TranslationSettings translationSettings = new TranslationSettings();

	@Option(names = { "--unity" }, description = "Enables unity builds (C++ only) for files in the path")
	private boolean useUnityBuild = false;

	public TranslationSettings getTranslationSettings() {
		return translationSettings;
	}

	public void setTranslationSettings(TranslationSettings translationSettings) {
		this.translationSettings = translationSettings;
	}

	public EnumSet<Language> getAdditionalLanguages() {
		return additionalLanguages;
	}

	public void setAdditionalLanguages(EnumSet<Language> additionalLanguages) {
		this.additionalLanguages = additionalLanguages;
	}

	public boolean isEnablePython() {
		return enablePython;
	}

	public boolean isEnableGo() {
		return enableGo;
	}

	public boolean isUseUnityBuild() {
		return useUnityBuild;
	}

	@JsonProperty("unity")
	public void setUseUnityBuild(boolean useUnityBuild) {
		this.useUnityBuild = useUnityBuild;
	}

	@Override
	public Integer call() throws Exception {
		return 0;
	}
}