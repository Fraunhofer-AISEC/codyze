
package de.fraunhofer.aisec.analysis.structures;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Representation of the concrete value that would be assigned to a Mark parameter within a specific evaluation context.
 *
 * For instance:
 *
 * key: "b.alg" value: (V, "AES/ECB")
 *
 * where V is the vertex representing the assignment of the value as specified by the Mark op.
 */
public class CPGVariableContext {
	// stores e.g. for b.alg which argument-vertex sets alg, and the value of alg
	// Todo maybe move from strings as keys to sth more usable
	private Map<String, CPGVertexWithValue> variableAssignments = new HashMap<>();

	public void put(String key, CPGVertexWithValue mva) {
		variableAssignments.put(key, mva);
	}

	public Set<String> keySet() {
		return variableAssignments.keySet();
	}

	public void fillFrom(CPGVariableContext varContext) {
		for (Map.Entry<String, CPGVertexWithValue> inner : varContext.variableAssignments.entrySet()) {
			this.variableAssignments.put(inner.getKey(), inner.getValue());
		}

	}

	public CPGVertexWithValue get(String key) {
		return variableAssignments.get(key);
	}

}
