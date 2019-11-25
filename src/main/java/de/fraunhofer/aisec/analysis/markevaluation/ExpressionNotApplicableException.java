
package de.fraunhofer.aisec.analysis.markevaluation;

/**
 * Indicates that a MARK expression could not be evaluated.
 *
 * This exception is expected to the caught by the outermost "evaluation" method of a MARK expression.
 */
public class ExpressionNotApplicableException extends RuntimeException {
	public ExpressionNotApplicableException(String s) {
		super(s);
	}

	public ExpressionNotApplicableException(String s, Throwable t) {
		super(s, t);
	}
}
