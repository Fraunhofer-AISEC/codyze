
package de.fraunhofer.aisec.crymlin.builtin;

/**
 * Indicates that a MARK expression could not be evaluated.
 *
 * This exception is expected to the caught by the outermost "evaluation" method of a MARK expression.
 */
public class InvalidArgumentException extends RuntimeException {
	public InvalidArgumentException(String s) {
		super(s);
	}
}
