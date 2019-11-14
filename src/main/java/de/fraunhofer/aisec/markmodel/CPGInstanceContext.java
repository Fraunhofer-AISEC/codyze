
package de.fraunhofer.aisec.markmodel;

import de.fraunhofer.aisec.crymlin.utils.Pair;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CPGInstanceContext {
	// e.g. for
	//    using Botan as b, Random as r
	// maps "b" to its vertex and "r" to its vertex
	private Map<String, Vertex> entityAssignemnt = new HashMap<>();

	public void entityPut(String s, Vertex v) {
		entityAssignemnt.put(s, v);
	}

	public Vertex entityGet(String s) {
		return entityAssignemnt.get(s);
	}

	public Set<String> entityKeySet() {
		return entityAssignemnt.keySet();
	}
}
