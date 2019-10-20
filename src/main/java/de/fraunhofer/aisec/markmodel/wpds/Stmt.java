
package de.fraunhofer.aisec.markmodel.wpds;

import org.checkerframework.checker.nullness.qual.NonNull;

/** Wrapper for a statement in a CPG to be used as a WPDS control location. */
public class Stmt {
	@NonNull
	private final String stmt;

	public Stmt(@NonNull String n) {
		this.stmt = n;
	}

	@Override
	public String toString() {
		return stmt;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + stmt.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Stmt))
			return false;
		Stmt other = (Stmt) obj;
		if (!stmt.equals(other.stmt))
			return false;
		return true;
	}
}
