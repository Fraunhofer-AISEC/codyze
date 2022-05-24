
package specification_languages.mark.analysis.markevaluation;

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
			if (value.equals(t)) {
				return 0;
			}
			try {
				BigDecimal other = new BigDecimal((String) t);
				BigDecimal myself = new BigDecimal((String) value);
				return myself.compareTo(other);
			}
			catch (NumberFormatException e) {
				return ((String) value).compareTo((String) t);
			}
		}
		log.error("Cannot compare {} to {}", value, t);
		return -1;
	}
}
