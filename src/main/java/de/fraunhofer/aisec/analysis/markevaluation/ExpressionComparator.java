
package de.fraunhofer.aisec.analysis.markevaluation;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Comparator;

/**
 * Compares duck typed values in expressions.
 *
 * Strings and numeric values can be compared, where reasonable.
 *
 * For instance, the following comparisons are valid: "1" < 2.0 1 < "2.1" "-1" < "0"
 *
 * @param <T>
 */
public class ExpressionComparator<T> implements Comparator<T> {
	private static final Logger log = LoggerFactory.getLogger(ExpressionComparator.class);

	@Override
	public int compare(T value, T t) {
		if (t instanceof String && value instanceof String) {
			try {
				BigDecimal other = new BigDecimal((String) t);
				BigDecimal myself = new BigDecimal((String) value);
				return myself.compareTo(other);
			}
			catch (NumberFormatException e) {
				log.debug("Cannot compare " + value + " to " + t);
				throw new ExpressionEvaluationException("Cannot compare " + value + " to " + t, e);
			}
		}
		log.error("Cannot compare " + value + " to " + t);
		return -1;
	}
}
