
package de.fraunhofer.aisec.markmodel;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CPGVariableContext {
	// stores e.g. for b.alg which argument-vertex sets alg, and the value of alg
	// Todo maybe move from strings as keys to sth more usable
	private Map<String, MarkVariableAssignment> variableAssignments = new HashMap<>();

	public void put(String key, MarkVariableAssignment mva) {
		variableAssignments.put(key, mva);
	}

	public Set<String> keySet() {
		return variableAssignments.keySet();
	}

	public void fillFrom(CPGVariableContext varContext) {
		for (Map.Entry<String, MarkVariableAssignment> inner : varContext.variableAssignments.entrySet()) {
			this.variableAssignments.put(inner.getKey(), inner.getValue());
		}

	}

	public MarkVariableAssignment get(String key) {
		return variableAssignments.get(key);
	}

}
