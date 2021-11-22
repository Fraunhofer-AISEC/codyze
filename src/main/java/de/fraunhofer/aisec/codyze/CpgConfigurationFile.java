
package de.fraunhofer.aisec.codyze;

import de.fraunhofer.aisec.cpg.passes.Pass;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CpgConfigurationFile {

	private boolean debugParser;
	private boolean analyzeIncludes;
	private File[] includePaths;
	private List<String> includeWhitelist;
	private List<String> includeBlacklist;
	private boolean defaultPasses;
	private List<Pass> passes;
	private String[] additionalLanguages;
	private Map<String, String> symbols;

	public void standardizeLanguages() {
		if (additionalLanguages != null)
			for (int i = 0; i < additionalLanguages.length; i++) {
				additionalLanguages[i] = additionalLanguages[i].toLowerCase();
			}
	}

	public boolean isAnalyzeIncludes() {
		return analyzeIncludes;
	}

	public void setAnalyzeIncludes(boolean analyzeIncludes) {
		this.analyzeIncludes = analyzeIncludes;
	}

	public File[] getIncludePaths() {
		return includePaths;
	}

	public void setIncludePaths(File[] includePaths) {
		this.includePaths = includePaths;
	}

	public String[] getAdditionalLanguages() {
		return additionalLanguages;
	}

	public void setAdditionalLanguages(String[] additionalLanguages) {
		this.additionalLanguages = additionalLanguages;
	}

	public boolean isDebugParser() {
		return debugParser;
	}

	public void setDebugParser(boolean debugParser) {
		this.debugParser = debugParser;
	}

	public List<String> getIncludeWhitelist() {
		return includeWhitelist;
	}

	public void setIncludeWhitelist(List<String> includeWhitelist) {
		this.includeWhitelist = includeWhitelist;
	}

	public List<String> getIncludeBlacklist() {
		return includeBlacklist;
	}

	public void setIncludeBlacklist(List<String> includeBlacklist) {
		this.includeBlacklist = includeBlacklist;
	}

	public boolean isDefaultPasses() {
		return defaultPasses;
	}

	public void setDefaultPasses(boolean defaultPasses) {
		this.defaultPasses = defaultPasses;
	}

	public List<Pass> getPasses() {
		return passes;
	}

	public void setPasses(List<Pass> passes) {
		this.passes = passes;
	}

	public Map<String, String> getSymbols() {
		return symbols;
	}

	public void setSymbols(Map<String, String> symbols) {
		this.symbols = symbols;
	}
}
