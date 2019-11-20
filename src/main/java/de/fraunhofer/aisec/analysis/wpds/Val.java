
package de.fraunhofer.aisec.analysis.wpds;

import org.checkerframework.checker.nullness.qual.NonNull;

public class Val {

	private final String variable;
	private final String currentScope;

	public Val(@NonNull String variable, @NonNull String currentScope) {
		this.variable = variable;
		this.currentScope = currentScope;
	}

	@Override
	public String toString() {
		return variable + " in (" + currentScope + ")";
	}

	public String getVariable() {
		return variable;
	}

	public String getCurrentScope() {
		return currentScope;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + variable.hashCode();
		result = prime * result + currentScope.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Val)) {
			return false;
		}
		Val other = (Val) obj;
		if (!variable.equals(other.variable)) {
			return false;
		}
		if (!currentScope.equals(other.currentScope)) {
			return false;
		}
		return true;
	}
}
