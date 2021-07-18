
package de.fraunhofer.aisec.codyze.crymlin.builtin;

import de.fraunhofer.aisec.codyze.analysis.*;
import de.fraunhofer.aisec.codyze.analysis.markevaluation.ExpressionEvaluator;
import de.fraunhofer.aisec.codyze.analysis.markevaluation.ExpressionHelper;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Method signature: _year()
 *
 * Note:
 * Time is represented as numeric value. The value represents the number of seconds
 * since epoch (1970-01-01T00:00:00Z).
 */
public class Year implements Builtin {
	private static final Logger log = LoggerFactory.getLogger(Year.class);

	@NonNull
	@Override
	public String getName() {
		return "_year";
	}

	@NonNull
	@Override
	public ConstantValue execute(
			@NonNull AnalysisContext ctx,
			@NonNull ListValue argResultList,
			@NonNull Integer contextID,
			@NonNull MarkContextHolder markContextHolder,
			@NonNull ExpressionEvaluator expressionEvaluator) {

		// arguments: int
		// example:
		// _year(_now()) returns 2021

		try {
			BuiltinHelper.verifyArgumentTypesOrThrow(argResultList, ConstantValue.class);

			var epochSeconds = ExpressionHelper.asNumber(argResultList.get(0));

			if (epochSeconds == null) {
				log.warn("The argument for _year was not the expected type, or not initialized/resolved");
				return ErrorValue.newErrorValue("The argument for _year was not the expected type, or not initialized/resolved", argResultList.getAll());
			}

			log.info("arg: {}", epochSeconds);

			var instant = Instant.ofEpochSecond((Long) epochSeconds);
			var ldt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
			var year = ldt.getYear();

			log.debug("_year() -> {}", year);

			ConstantValue cv = ConstantValue.of(year);

			cv.addResponsibleNodesFrom((ConstantValue) argResultList.get(0));

			return cv;
		}
		catch (InvalidArgumentException e) {
			log.warn(e.getMessage());
			return ErrorValue.newErrorValue(e.getMessage(), argResultList.getAll());
		}
	}
}
