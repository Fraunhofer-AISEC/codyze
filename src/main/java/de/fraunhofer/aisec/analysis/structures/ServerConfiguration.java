
package de.fraunhofer.aisec.analysis.structures;

import de.fraunhofer.aisec.analysis.server.AnalysisServer;

/** The configuration for the {@link AnalysisServer} holds all values used by the server. */
public class ServerConfiguration {

	/** Should the server launch an interactive CLI console? */
	public final boolean launchConsole;

	/** Should the server launch an LSP server? */
	public final boolean launchLsp;

	/** Directory or file with MARK entities/rules. */
	public final String markModelFiles;

	/** Which type of typestate analysis do we want? */
	public final TYPESTATE_ANALYSIS typestateAnalysis;

	// should we export the data to neo4j
	public static final boolean EXPORT_GRAPHML_AND_IMPORT_TO_NEO4J = false;

	private ServerConfiguration(boolean launchConsole, boolean launchLsp, String markModelFiles, TYPESTATE_ANALYSIS typestateAnalysis) {
		this.launchConsole = launchConsole;
		this.launchLsp = launchLsp;
		this.markModelFiles = markModelFiles;
		this.typestateAnalysis = typestateAnalysis;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private boolean launchConsole = true;
		private boolean launchLsp = true;
		private String markModelFiles = ""; // Path of a file or directory
		private TYPESTATE_ANALYSIS typestateAnalysis = TYPESTATE_ANALYSIS.NFA;

		public Builder launchConsole(boolean launchConsole) {
			this.launchConsole = launchConsole;
			return this;
		}

		public Builder launchLsp(boolean launchLsp) {
			this.launchLsp = launchLsp;
			return this;
		}

		public Builder markFiles(String markModelFiles) {
			this.markModelFiles = markModelFiles;
			return this;
		}

		public Builder typestateAnalysis(TYPESTATE_ANALYSIS tsAnalysis) {
			this.typestateAnalysis = tsAnalysis;
			return this;
		}

		public ServerConfiguration build() {
			return new ServerConfiguration(launchConsole, launchLsp, markModelFiles, typestateAnalysis);
		}
	}
}
