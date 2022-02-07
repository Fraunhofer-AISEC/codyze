
package de.fraunhofer.aisec.codyze.analysis;

import de.fraunhofer.aisec.cpg.frontends.LanguageFrontend;
import kotlin.Pair;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

	/**
	 * Passed down to {@link de.fraunhofer.aisec.cpg.TranslationConfiguration}. Whether to
	 * parse include files.
	 */
	public final boolean analyzeIncludes;

	/**
	 * Enables or disables unity builds (for C++ only).
	 */
	public final boolean useUnityBuild;

	/**
	 * Path(s) containing include files.
	 *
	 * <p>Paths must be separated by File.pathSeparator (: or ;).
	 */
	@NonNull
	public final File[] includePath;

	/** If true, no "positive" findings will be returned from the analysis. */
	public final boolean disableGoodFindings;

	/** Additional registered languages */
	public final List<Pair<Class<? extends LanguageFrontend>, List<String>>> additionalLanguages;

	/** Disabled Mark rules */
	public final Map<String, Pair<Boolean, Set<String>>> disabledMarkRules;

	private ServerConfiguration(
			boolean launchConsole,
			boolean launchLsp,
			@NonNull String[] markModelFiles,
			@NonNull TypestateMode typestateMode,
			boolean analyzeIncludes,
			boolean useUnityBuild,
			@NonNull File[] includePath,
			boolean disableGoodFindings,
			List<Pair<Class<? extends LanguageFrontend>, List<String>>> additionalLanguages,
			Map<String, Pair<Boolean, Set<String>>> disabledMarkRules) {
		this.launchConsole = launchConsole;
		this.launchLsp = launchLsp;
		this.markModelFiles = markModelFiles;
		this.typestateAnalysis = typestateMode;
		this.analyzeIncludes = analyzeIncludes;
		this.useUnityBuild = useUnityBuild;
		this.includePath = includePath;
		this.disableGoodFindings = disableGoodFindings;
		this.additionalLanguages = additionalLanguages;
		this.disabledMarkRules = disabledMarkRules;
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
		private boolean analyzeIncludes;
		private boolean useUnityBuild;
		private File[] includePath = new File[0];
		private boolean disableGoodFindings;
		public final List<Pair<Class<? extends LanguageFrontend>, List<String>>> additionalLanguages = new ArrayList<>();
		private Map<String, Pair<Boolean, Set<String>>> disabledMarkRules;

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

		public Builder analyzeIncludes(boolean analyzeIncludes) {
			this.analyzeIncludes = analyzeIncludes;
			return this;
		}

		public Builder useUnityBuild(boolean useUnityBuild) {
			this.useUnityBuild = useUnityBuild;
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

		public Builder registerLanguage(Class<? extends LanguageFrontend> clazz, List<String> fileExtensions) {
			this.additionalLanguages.add(new Pair<>(clazz, fileExtensions));
			return this;
		}

		public Builder disableGoodFindings(boolean disableGoodFindings) {
			this.disableGoodFindings = disableGoodFindings;
			return this;
		}

		public Builder useLegacyEvaluator() {
			return this;
		}

		public Builder disableMark(Map<String, Pair<Boolean, Set<String>>> disabledMarkRules) {
			this.disabledMarkRules = disabledMarkRules;
			return this;
		}

		public ServerConfiguration build() {
			return new ServerConfiguration(
				launchConsole,
				launchLsp,
				markModelFiles,
				typestateAnalysis,
				analyzeIncludes,
				useUnityBuild,
				includePath,
				disableGoodFindings,
				additionalLanguages,
				disabledMarkRules);
		}
	}
}
