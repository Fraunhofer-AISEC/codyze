
package de.fraunhofer.aisec.codyze.legacy.analysis;

import de.fraunhofer.aisec.cpg.graph.Node;

import java.util.Set;

public abstract class MarkIntermediateResult {
	private final ResultType resultType;

	public enum ResultType {
		UNDEFINED, SINGLEVALUE, LIST
	}

	public MarkIntermediateResult(ResultType t) {
		this.resultType = t;
	}

	public boolean isSingleResult() {
		return resultType == ResultType.SINGLEVALUE;
	}

	public boolean isListResult() {
		return resultType == ResultType.LIST;
	}

	/**
	 * Returns a set of nodes that are responsible for the represented value.
	 *
	 * @return set of nodes
	 */
	public abstract Set<Node> getResponsibleNodes();
}
