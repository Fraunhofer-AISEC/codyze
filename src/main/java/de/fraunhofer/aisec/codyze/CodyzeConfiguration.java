
package de.fraunhofer.aisec.codyze;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.fraunhofer.aisec.codyze.analysis.TypestateMode;
import org.checkerframework.checker.nullness.qual.NonNull;
import picocli.CommandLine;
import picocli.CommandLine.Option;

import java.io.File;
import java.util.Objects;

public class CodyzeConfiguration {
	// TODO: names

	@JsonIgnore
	@CommandLine.ArgGroup(exclusive = true, multiplicity = "1", heading = "Execution Mode\n")
	private ExecutionMode executionMode;

	@CommandLine.ArgGroup(exclusive = false, heading = "Analysis Options\n")
	private AnalysisMode analysis = new AnalysisMode();

	@Option(names = { "-s", "--source" }, paramLabel = "<path>", description = "Source file or folder to analyze.")
	private File source;

	@Option(names = { "-m",
			"--mark" }, paramLabel = "<path>", description = "Loads MARK policy files\n\t(Default: ${DEFAULT-VALUE})", split = ",")
	@NonNull
	private static File[] mark = { new File("./") };

	// TODO output standard stdout?
	@Option(names = { "-o",
			"--output" }, paramLabel = "<file>", description = "Write results to file. Use - for stdout.\n\t(Default: ${DEFAULT-VALUE})")
	private static String output = "findings.sarif";

	@Option(names = {
			"--timeout" }, paramLabel = "<minutes>", description = "Terminate analysis after timeout\n\t(Default: ${DEFAULT-VALUE})")
	private static long timeout = 120L;

	@Option(names = {
			"--no-good-findings" }, description = "Disable output of \"positive\" findings which indicate correct implementations\n\t(Default: ${DEFAULT-VALUE})")
	private static boolean noGoodFindings;

	@Option(names = { "--sarif" }, description = "Enables the SARIF output.")
	private boolean sarifOutput;

	public ExecutionMode getExecutionMode() {
		return executionMode;
	}

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
		CodyzeConfiguration.output = output;
	}

	public File[] getMark() {
		return mark;
	}

	public void setMark(File[] markModelFiles) {
		CodyzeConfiguration.mark = markModelFiles;
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		CodyzeConfiguration.timeout = timeout;
	}

	public boolean isNoGoodFindings() {
		return noGoodFindings;
	}

	public void setNoGoodFindings(boolean noGoodFindings) {
		CodyzeConfiguration.noGoodFindings = noGoodFindings;
	}

	public AnalysisMode getAnalysis() {
		return analysis;
	}

	public void setAnalysis(TypestateMode tsMode) {
		this.analysis.setTsMode(tsMode);
	}

	public void setExecutionMode(boolean cli, boolean lsp, boolean tui) {
		if (executionMode == null)
			executionMode = new ExecutionMode();
		if ((cli ^ lsp ^ tui) && !(cli && lsp && tui)) {
			executionMode.cli = cli;
			executionMode.lsp = lsp;
			executionMode.tui = tui;
		}
	}

	public boolean isSarifOutput() {
		return sarifOutput;
	}

	public void setSarifOutput(boolean sarifOutput) {
		this.sarifOutput = sarifOutput;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		CodyzeConfiguration that = (CodyzeConfiguration) o;
		return sarifOutput == that.sarifOutput && Objects.equals(executionMode, that.executionMode) && Objects.equals(analysis, that.analysis)
				&& Objects.equals(source, that.source);
	}
}

/**
 * Codyze runs in any of three modes:
 * <p>
 * CLI: Non-interactive command line client. Accepts arguments from command line and runs analysis.
 * <p>
 * LSP: Bind to stdout as a server for Language Server Protocol (LSP). This mode is for IDE support.
 * <p>
 * TUI: The text based user interface (TUI) is an interactive console that allows exploring the analyzed source code by manual queries.
 */
class ExecutionMode {
	@Option(names = "-c", required = true, description = "Start in command line mode.")
	boolean cli;
	@Option(names = "-l", required = true, description = "Start in language server protocol (LSP) mode.")
	boolean lsp;
	@Option(names = "-t", required = true, description = "Start interactive console (Text-based User Interface).")
	boolean tui;

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		ExecutionMode that = (ExecutionMode) o;
		return cli == that.cli && lsp == that.lsp && tui == that.tui;
	}
}

class AnalysisMode {

	@Option(names = "--typestate", paramLabel = "<NFA|WPDS>", type = TypestateMode.class, description = "Typestate analysis mode\nNFA:  Non-deterministic finite automaton (faster, intraprocedural)\nWPDS: Weighted pushdown system (slower, interprocedural)\n\t(Default: ${DEFAULT-VALUE})")
	protected static TypestateMode tsMode = TypestateMode.NFA;

	@JsonProperty("typestate")
	public void setTsMode(TypestateMode tsMode) {
		AnalysisMode.tsMode = tsMode;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass())
			return false;
		return true;
	}
}
