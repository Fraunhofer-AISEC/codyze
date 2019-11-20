
package de.fraunhofer.aisec.analysis.structures;

import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Maps a Mark instances ("b") to (one of) the CPG vertices that defines it.
 *
 * key: Mark instance ("b")
 * value: the Vertex that usages of program variables corresponding to "b" REFERS_TO. Typically a VariableDeclaration node.
 *
 */
public class CPGInstanceContext {
	// e.g. for
	//    using Botan as b, Random as r
	// maps "b" to its vertex and "r" to its vertex
	private Map<String, Vertex> entityAssignment = new HashMap<>();

	/**
	 * Stores a Mark instance (e.g., "b") and the Vertex that defines it.
	 *
	 * @param s
	 * @param v
	 */
	public void putMarkInstance(String s, Vertex v) {
		entityAssignment.put(s, v);
	}

	public Vertex getVertex(String s) {
		return entityAssignment.get(s);
	}

	public Set<String> getMarkInstances() {
		return entityAssignment.keySet();
	}
}
