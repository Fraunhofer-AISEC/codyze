
package de.fraunhofer.aisec.codyze.analysis;

import de.fraunhofer.aisec.cpg.frontends.LanguageFrontend;
import kotlin.Pair;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** The configuration for the {@link AnalysisServer} holds all values used by the server. */
public class ServerConfiguration {

	/** Should the server launch an interactive CLI console? */
	public final boolean launchConsole;

	/** Should the server launch an LSP server? */
	public final boolean launchLsp;

	/** Directories or files with MARK entities/rules. */
	@NonNull
	public final String[] markModelFiles;

	/** Which type of typestate analysis do we want? */
	@NonNull
	public final TypestateMode typestateAnalysis;

	/** If true, no "positive" findings will be returned from the analysis. */
	public final boolean disableGoodFindings;

	private ServerConfiguration(
			boolean launchConsole,
			boolean launchLsp,
			@NonNull String[] markModelFiles,
			@NonNull TypestateMode typestateMode,
			boolean disableGoodFindings) {
		this.launchConsole = launchConsole;
		this.launchLsp = launchLsp;
		this.markModelFiles = markModelFiles;
		this.typestateAnalysis = typestateMode;
		this.disableGoodFindings = disableGoodFindings;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private boolean launchConsole = true;
		private boolean launchLsp = true;
		@NonNull
		private String[] markModelFiles = new String[0]; // Path of a file or directory
		@NonNull
		private TypestateMode typestateAnalysis = TypestateMode.DFA;
		private boolean disableGoodFindings;

		public Builder launchConsole(boolean launchConsole) {
			this.launchConsole = launchConsole;
			return this;
		}

		public Builder launchLsp(boolean launchLsp) {
			this.launchLsp = launchLsp;
			return this;
		}

		public Builder markFiles(String... markModelFiles) {
			this.markModelFiles = markModelFiles;
			return this;
		}

		public Builder typestateAnalysis(@NonNull TypestateMode tsAnalysis) {
			this.typestateAnalysis = tsAnalysis;
			return this;
		}

		public Builder disableGoodFindings(boolean disableGoodFindings) {
			this.disableGoodFindings = disableGoodFindings;
			return this;
		}

		public Builder useLegacyEvaluator() {
			return this;
		}

		public ServerConfiguration build() {
			return new ServerConfiguration(
				launchConsole,
				launchLsp,
				markModelFiles,
				typestateAnalysis,
				disableGoodFindings);
		}
	}
}
