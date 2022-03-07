
package de.fraunhofer.aisec.codyze.analysis;

import org.checkerframework.checker.nullness.qual.NonNull;
import de.fraunhofer.aisec.codyze.config.DisabledMarkRulesValue;
import de.fraunhofer.aisec.cpg.frontends.LanguageFrontend;
import kotlin.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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

	/**
	 * Enables pedantic mode analyzing all MARK rules and reporting all findings regardless of other configuration options.
	 */
	public final boolean pedantic;

	/** Disabled Mark rules. key=package, value=(disableEntirePackage, markRulesToDisable) */
	public final Map<String, DisabledMarkRulesValue> packageToDisabledMarkRules;

	private ServerConfiguration(
			boolean launchConsole,
			boolean launchLsp,
			@NonNull String[] markModelFiles,
			@NonNull TypestateMode typestateMode,
			boolean disableGoodFindings,
			Map<String, DisabledMarkRulesValue> packageToDisabledMarkRules,
			boolean pedantic) {
		this.launchConsole = launchConsole;
		this.launchLsp = launchLsp;
		this.markModelFiles = markModelFiles;
		this.typestateAnalysis = typestateMode;
		this.disableGoodFindings = disableGoodFindings;
		this.pedantic = pedantic;
		this.packageToDisabledMarkRules = packageToDisabledMarkRules;
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
		private boolean pedantic;
		private Map<String, DisabledMarkRulesValue> packageToDisabledMarkRules = Collections.emptyMap();

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

		public Builder pedantic(boolean pedantic) {
			this.pedantic = pedantic;
			return this;
		}

		public Builder useLegacyEvaluator() {
			return this;
		}

		public Builder disableMark(Map<String, DisabledMarkRulesValue> disabledMarkRules) {
			this.packageToDisabledMarkRules = disabledMarkRules;
			return this;
		}

		public ServerConfiguration build() {
			return new ServerConfiguration(
				launchConsole,
				launchLsp,
				markModelFiles,
				typestateAnalysis,
				disableGoodFindings,
				packageToDisabledMarkRules,
				pedantic);
		}
	}
}
