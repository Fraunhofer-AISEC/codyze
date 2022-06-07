
package de.fraunhofer.aisec.codyze.mark.builtin;

/**
 * Indicates that a MARK expression could not be evaluated.
 *
 * This exception is expected to the caught by the outermost "evaluation" method of a MARK expression.
 */
public class InvalidArgumentException extends Exception {
	public InvalidArgumentException(String s) {
		super(s);
	}
}
