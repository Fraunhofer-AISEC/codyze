
package de.fraunhofer.aisec.crymlin.builtin;

import de.fraunhofer.aisec.analysis.markevaluation.ExpressionEvaluator;
import de.fraunhofer.aisec.analysis.markevaluation.ExpressionHelper;
import de.fraunhofer.aisec.analysis.structures.*;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.regex.PatternSyntaxException;

/**
 * Method signature: _now()
 *
 * <p>
 * Relies on Java's {@link Instant#now()}.
 * </p>
 *
 * Note:
 * Time is represented as numeric value. The value represents the number of seconds
 * since epoch (1970-01-01T00:00:00Z).
 */
public class Now implements Builtin {
	private static final Logger log = LoggerFactory.getLogger(Now.class);

	@NonNull
	@Override
	public String getName() {
		return "_now";
	}

	@NonNull
	@Override
	public boolean hasParameters() {
		return false;
	}

	@NonNull
	@Override
	public ConstantValue execute(
			@NonNull AnalysisContext ctx,
			@NonNull ListValue argResultList,
			@NonNull Integer contextID,
			@NonNull MarkContextHolder markContextHolder,
			@NonNull ExpressionEvaluator expressionEvaluator) {

		log.debug("Executing builtin: {}", this.getName());

		var instant = Instant.now();
		var epochSeconds = instant.getEpochSecond();

		log.debug("_now() -> {}", epochSeconds);

		return ConstantValue.of(epochSeconds);
	}
}
