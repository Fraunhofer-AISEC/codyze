
package de.fraunhofer.aisec.crymlin.server;

import de.fraunhofer.aisec.crymlin.structures.Finding;
import de.fraunhofer.aisec.crymlin.structures.Method;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AnalysisContext {

	/** List of violations of MARK rules. the region, etc. */
	@NonNull
	private final Set<Finding> findings = new HashSet<>();

	/** Map of method signatures to {@code Method}s. */
	public final Map<String, Method> methods = new HashMap<>();

	/**
	 * Returns a (possibly empty) list of findings, i.e. violations of MARK rules that were found during analysis. Make sure to call {@code analyze()} before as otherwise
	 * this method will return an empty list.
	 *
	 * @return
	 */
	public @NonNull Set<Finding> getFindings() {
		return this.findings;
	}
}
