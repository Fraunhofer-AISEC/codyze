
package de.fraunhofer.aisec.codyze;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import picocli.CommandLine;
import picocli.CommandLine.Option;

import java.io.File;
import java.util.*;

public class CpgConfiguration {

	@CommandLine.ArgGroup(exclusive = false, heading = "Translation Options\n")
	private TranslationSettings translation = new TranslationSettings();

	@JsonDeserialize(using = LanguageDeseralizer.class)
	private Set<Language> additionalLanguages = EnumSet.noneOf(Language.class);

	// TODO: maybe change to enum set instead of booleans for each language
	@JsonIgnore
	@Option(names = {
			"--enable-python-support" }, description = "Enables the experimental Python support. Additional files need to be placed in certain locations. Please follow the CPG README.")
	private boolean enablePython;

	@JsonIgnore
	@Option(names = {
			"--enable-go-support" }, description = "Enables the experimental Go support. Additional files need to be placed in certain locations. Please follow the CPG README.")
	private boolean enableGo;

	@Option(names = { "--unity" }, description = "Enables unity builds (C++ only) for files in the path")
	private boolean useUnityBuild = false;

	public TranslationSettings getTranslation() {
		return translation;
	}

	public void setTranslation(boolean analyzeIncludes, File[] includesPath) {
		this.translation.analyzeIncludes = analyzeIncludes;
		this.translation.includes = includesPath;
	}

	public Set<Language> getAdditionalLanguages() {
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
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		CpgConfiguration that = (CpgConfiguration) o;
		return useUnityBuild == that.useUnityBuild && enablePython == that.enablePython && enableGo == that.enableGo && Objects.equals(translation, that.translation)
				&& Objects.equals(additionalLanguages, that.additionalLanguages);
	}
}

class TranslationSettings {
	@Option(names = {
			"--analyze-includes" }, description = "Enables parsing of include files. By default, if --includes are given, the parser will resolve symbols/templates from these include, but not load their parse tree. This will enforced to true, if unity builds are used.")
	protected boolean analyzeIncludes = false;

	@Option(names = { "--includes" }, description = "Path(s) containing include files. Path must be separated by : (Mac/Linux) or ; (Windows)", split = ":|;")
	protected File[] includes;

	public void setAnalyzeIncludes(boolean analyzeIncludes) {
		this.analyzeIncludes = analyzeIncludes;
	}

	public void setIncludes(File[] includes) {
		this.includes = includes;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		TranslationSettings that = (TranslationSettings) o;
		return analyzeIncludes == that.analyzeIncludes && Arrays.equals(includes, that.includes);
	}
}