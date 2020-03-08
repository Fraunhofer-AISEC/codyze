
package de.fraunhofer.aisec.analysis.wpds;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.*;

public class PredecessorMap<V, S> {
	private Map<V, Set<S>> predecessors = new HashMap<>();

	public void put(V v, S stmt) {
		if (predecessors.get(v) == null) {
			predecessors.put(v, new HashSet<>());
		}
		predecessors.get(v).add(stmt);
	}

	@NonNull
	public Set<S> getAll(V v) {
		Set<S> result = predecessors.get(v);
		if (result == null) {
			return Set.of();
		}
		return result;
	}

	@Nullable
	public S pop(V v) {
		Set<S> set = predecessors.get(v);
		if (set != null && !set.isEmpty()) {
			Iterator<S> it = set.iterator();
			S stmt = it.next();
			it.remove();
			return stmt;
		}
		return null;
	}

	public boolean hasMoreElements(V v) {
		return predecessors.get(v) != null && !predecessors.get(v).isEmpty();
	}

	public void dump() {
		System.out.println("--- Predecessors ---");
		for (V v : predecessors.keySet()) {
			if (v instanceof Vertex) {
				String fullcode = ((String) ((Vertex) v).property("code")
						.value());
				System.out.println(fullcode.substring(0, Math.min(fullcode.length(), 20)));
			}
			for (S s : predecessors.get(v)) {
				System.out.println("    =>   " + s);
			}
			System.out.println("-------------------");
		}
		System.out.println("-------------------");
	}
}
