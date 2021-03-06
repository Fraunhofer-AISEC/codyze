
package de.fraunhofer.aisec.codyze.markmodel;

import org.checkerframework.checker.nullness.qual.NonNull;

public class MOpCallStmt {
	private String name;
	private String condition;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setCondition(@NonNull String condition) {
		this.condition = condition;
	}

	public String getCondition() {
		return condition;
	}
}
