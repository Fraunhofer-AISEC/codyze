
package de.fraunhofer.aisec.codyze.mark.analysis.markevaluation;

/**
 * Indicates that a MARK expression could not be evaluated.
 *
 * This exception is expected to the caught by the outermost "evaluation" method of a MARK expression.
 */
public class ExpressionEvaluationException extends RuntimeException {
	public ExpressionEvaluationException(String s) {
		super(s);
	}

	public ExpressionEvaluationException(String s, Throwable t) {
		super(s, t);
	}
}
