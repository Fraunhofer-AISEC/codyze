
package de.fraunhofer.aisec.analysis.markevaluation;

public class ExpressionEvaluationException extends RuntimeException {
	public ExpressionEvaluationException(String s) {
		super(s);
	}

	public ExpressionEvaluationException(String s, Throwable t) {
		super(s, t);
	}
}
