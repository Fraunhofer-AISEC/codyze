
package de.fraunhofer.aisec.analysis.structures;

import de.fraunhofer.aisec.cpg.graph.Node;
import de.fraunhofer.aisec.crymlin.connectors.db.Database;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AnalysisContext {

	/** List of violations of MARK rules. the region, etc. */
	@NonNull
	private final Set<Finding> findings = new HashSet<>();

	/** Map of method signatures to {@code Method}s. */
	public final Map<String, Method> methods = new HashMap<>();

	private final List<File> sourceLocations;

	/** The database used for this analysis. */
	@NonNull
	private Database<Node> db;

	public AnalysisContext(List<File> sourceLocations, @NonNull Database<Node> db) {
		this.sourceLocations = sourceLocations;
		this.db = db;
	}

	public AnalysisContext(File f, @NonNull Database<Node> db) {
		this(List.of(f), db);
	}

	/**
	 * Returns a (possibly empty) mutable list of findings, i.e. violations of MARK rules that were
	 * found during analysis. Make sure to call {@code analyze()} before as otherwise this method will
	 * return an empty list.
	 *
	 * @return Set of all findings
	 */
	public @NonNull Set<Finding> getFindings() {
		return this.findings;
	}

	public List<File> getSourceLocations() {
		return sourceLocations;
	}

	@NonNull
	public Database<Node> getDatabase() {
		return this.db;
	}
}
