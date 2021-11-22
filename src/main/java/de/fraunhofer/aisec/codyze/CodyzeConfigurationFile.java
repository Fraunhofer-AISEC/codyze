
package de.fraunhofer.aisec.codyze;

import de.fraunhofer.aisec.codyze.analysis.TypestateMode;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;

public class CodyzeConfigurationFile {

	// TODO: names
	private File source;
	private String output;
	@NonNull
	private File[] mark;
	private Long timeout;
	private boolean noGoodFindings;
	@NonNull
	private TypestateMode typestateAnalysis;
	private String[] additionalLanguages;

	public File getSource() {
		return source;
	}

	public void setSource(File source) {
		this.source = source;
	}

	public String getOutput() {
		return output;
	}

	public void setOutput(String output) {
		this.output = output;
	}

	public File[] getMark() {
		return mark;
	}

	public void setMark(File[] markModelFiles) {
		this.mark = markModelFiles;
	}

	public Long getTimeout() {
		return timeout;
	}

	public void setTimeout(Long timeout) {
		this.timeout = timeout;
	}

	public boolean isNoGoodFindings() {
		return noGoodFindings;
	}

	public void setNoGoodFindings(boolean noGoodFindings) {
		this.noGoodFindings = noGoodFindings;
	}

	public TypestateMode getTypestateAnalysis() {
		return typestateAnalysis;
	}

	public void setTypestateAnalysis(TypestateMode typestateAnalysis) {
		this.typestateAnalysis = typestateAnalysis;
	}

	public String[] getAdditionalLanguages() {
		return additionalLanguages;
	}

	public void setAdditionalLanguages(String[] additionalLanguages) {
		this.additionalLanguages = additionalLanguages;
	}
}
