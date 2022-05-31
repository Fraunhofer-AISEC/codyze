
package de.fraunhofer.aisec.codyze.analysis;

public class MarkIntermediateResult {
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

}
