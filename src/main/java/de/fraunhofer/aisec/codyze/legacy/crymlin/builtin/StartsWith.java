
package de.fraunhofer.aisec.codyze.legacy.crymlin.builtin;

import de.fraunhofer.aisec.codyze.legacy.analysis.*;
import de.fraunhofer.aisec.codyze.legacy.analysis.markevaluation.ExpressionEvaluator;
import de.fraunhofer.aisec.codyze.legacy.analysis.markevaluation.ExpressionHelper;
import de.fraunhofer.aisec.codyze.legacy.analysis.resolution.ConstantValue;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Method signature: _starts_with(String str, String start)
 *
 * returns true if str starts with start
 *
 * In case of an error, this Builtin returns an ErrorValue;
 */
public class StartsWith implements Builtin {
	private static final Logger log = LoggerFactory.getLogger(StartsWith.class);

	@NonNull
	@Override
	public String getName() {
		return "_starts_with";
	}

	@Override
	public MarkIntermediateResult execute(
			@NonNull AnalysisContext ctx,
			@NonNull ListValue argResultList,
			@NonNull Integer contextID,
			@NonNull MarkContextHolder markContextHolder,
			ExpressionEvaluator expressionEvaluator) {

		try {
			BuiltinHelper.verifyArgumentTypesOrThrow(argResultList, ConstantValue.class, ConstantValue.class);

			String s = ExpressionHelper.asString(argResultList.get(0));
			String start = ExpressionHelper.asString(argResultList.get(1));

			if (s == null || start == null) {
				log.warn("One of the arguments for _split was not the expected type, or not initialized/resolved");
				return ErrorValue.newErrorValue("One of the arguments for _split was not the expected type, or not initialized/resolved", argResultList.getAll());
			}

			log.debug("args are: {}; {}", s, start);
			boolean ret = s.startsWith(start);

			ConstantValue cv = ConstantValue.of(ret);

			cv.addResponsibleNodesFrom((ConstantValue) argResultList.get(0),
				(ConstantValue) argResultList.get(1));

			return cv;
		}
		catch (InvalidArgumentException e) {
			log.warn(e.getMessage());
			return ErrorValue.newErrorValue(e.getMessage(), argResultList.getAll());
		}
	}
}
