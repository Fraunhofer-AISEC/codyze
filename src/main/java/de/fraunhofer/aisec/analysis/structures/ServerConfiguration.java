
package de.fraunhofer.aisec.analysis.structures;

import de.fraunhofer.aisec.analysis.server.AnalysisServer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;

/** The configuration for the {@link AnalysisServer} holds all values used by the server. */
public class ServerConfiguration {

	/** Should the server launch an interactive CLI console? */
	public final boolean launchConsole;

	/** Should the server launch an LSP server? */
	public final boolean launchLsp;

	/** Directory or file with MARK entities/rules. */
	@Nullable
	public final String markModelFiles;

	/** Which type of typestate analysis do we want? */
	@NonNull
	public final TypestateMode typestateAnalysis;

	/**
	 * Passed down to {@link de.fraunhofer.aisec.cpg.TranslationConfiguration}. Whether or not to
	 * parse include files.
	 */
	public final boolean analyzeIncludes;

	/**
	 * Path(s) containing include files.
	 *
	 * <p>Paths must be separated by File.pathSeparator (: or ;).
	 */
	@NonNull
	public final File[] includePath;

	/** If true, no "positive" findings will be returned from the analysis. */
	public final boolean disableGoodFindings;

	/**
	 * If true, the overflow of OverflowDB will be disabled. Might be faster for projects who fit
	 * completely in memory.
	 */
	public final boolean disableOverflow;

	/**
	 * Use the OverflowDB-based legacy evaluator.
	 */
	@Deprecated
	public boolean legacyEvaluator;

	private ServerConfiguration(
			boolean launchConsole,
			boolean launchLsp,
			@Nullable String markModelFiles,
			@NonNull TypestateMode typestateMode,
			boolean analyzeIncludes,
			@NonNull File[] includePath,
			boolean disableGoodFindings,
			boolean disableOverflow,
			boolean legacyEvaluator) {
		this.launchConsole = launchConsole;
		this.launchLsp = launchLsp;
		this.markModelFiles = markModelFiles;
		this.typestateAnalysis = typestateMode;
		this.analyzeIncludes = analyzeIncludes;
		this.includePath = includePath;
		this.disableGoodFindings = disableGoodFindings;
		this.disableOverflow = disableOverflow;
		this.legacyEvaluator = legacyEvaluator;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private boolean launchConsole = true;
		private boolean launchLsp = true;
		@Nullable
		private String markModelFiles = ""; // Path of a file or directory
		@NonNull
		private TypestateMode typestateAnalysis = TypestateMode.NFA;
		private boolean analyzeIncludes;
		private File[] includePath = new File[0];
		private boolean disableGoodFindings;
		private boolean disableOverflow;
		private boolean legacyEvaluator;

		public Builder launchConsole(boolean launchConsole) {
			this.launchConsole = launchConsole;
			return this;
		}

		public Builder launchLsp(boolean launchLsp) {
			this.launchLsp = launchLsp;
			return this;
		}

		public Builder markFiles(@Nullable String markModelFiles) {
			this.markModelFiles = markModelFiles;
			return this;
		}

		public Builder typestateAnalysis(@NonNull TypestateMode tsAnalysis) {
			this.typestateAnalysis = tsAnalysis;
			return this;
		}

		public Builder analyzeIncludes(boolean analyzeIncludes) {
			this.analyzeIncludes = analyzeIncludes;
			return this;
		}

		public Builder includePath(File[] includePath) {
			if (includePath == null) {
				this.includePath = new File[0];
			} else {
				this.includePath = includePath;
			}
			return this;
		}

		public Builder disableGoodFindings(boolean disableGoodFindings) {
			this.disableGoodFindings = disableGoodFindings;
			return this;
		}

		public Builder disableOverflow(boolean disableOverflow) {
			this.disableOverflow = disableOverflow;
			return this;
		}

		public Builder useLegacyEvaluator(boolean legacyEvaluator) {
			this.legacyEvaluator = legacyEvaluator;
			return this;
		}

		public ServerConfiguration build() {
			return new ServerConfiguration(
				launchConsole,
				launchLsp,
				markModelFiles,
				typestateAnalysis,
				analyzeIncludes,
				includePath,
				disableGoodFindings,
				disableOverflow,
				legacyEvaluator);
		}
	}
}
