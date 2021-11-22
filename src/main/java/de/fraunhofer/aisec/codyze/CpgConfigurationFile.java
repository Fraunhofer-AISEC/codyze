
package de.fraunhofer.aisec.codyze;

import java.io.File;

public class CpgConfigurationFile {

	private boolean analyzeIncludes;
	private File[] includePaths;

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
}
