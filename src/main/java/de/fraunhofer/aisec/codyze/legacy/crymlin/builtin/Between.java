
package de.fraunhofer.aisec.codyze.legacy.crymlin.builtin;

import de.fraunhofer.aisec.codyze.legacy.analysis.*;
import de.fraunhofer.aisec.codyze.legacy.analysis.markevaluation.ExpressionEvaluator;
import de.fraunhofer.aisec.codyze.legacy.analysis.markevaluation.ExpressionHelper;
import de.fraunhofer.aisec.codyze.legacy.analysis.resolution.ConstantValue;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Method signature: _between(String str, String start, String end)
 *
 * returns the part of the string between start and end (both are part of the string)
 *
 * e.g. _between("(09)", "(", ")") returns "09"
 *
 * In case of an error, this Builtin returns an ErrorValue;
 */
public class Between implements Builtin {
	private static final Logger log = LoggerFactory.getLogger(Between.class);

	@NonNull
	@Override
	public String getName() {
		return "_between";
	}

	@Override
	public MarkIntermediateResult execute(
			@NonNull AnalysisContext ctx,
			@NonNull ListValue argResultList,
			@NonNull Integer contextID,
			@NonNull MarkContextHolder markContextHolder,
			ExpressionEvaluator expressionEvaluator) {

		try {
			BuiltinHelper.verifyArgumentTypesOrThrow(argResultList, ConstantValue.class, ConstantValue.class, ConstantValue.class);

			String s = ExpressionHelper.asString(argResultList.get(0));
			String start = ExpressionHelper.asString(argResultList.get(1));
			String end = ExpressionHelper.asString(argResultList.get(2));

			if (s == null || start == null || end == null) {
				log.warn("One of the arguments for _between was not the expected type, or not initialized/resolved");
				return ErrorValue.newErrorValue("One of the arguments for _between was not the expected type, or not initialized/resolved", argResultList.getAll());
			}

			log.debug("args are: {}; {}; {}", s, start, end);
			String ret = s.substring(s.indexOf(start) + 1);
			ret = ret.substring(0, ret.lastIndexOf(end));

			var cv = ConstantValue.of(ret);

			cv.addResponsibleNodesFrom((ConstantValue) argResultList.get(0),
				(ConstantValue) argResultList.get(1),
				(ConstantValue) argResultList.get(2));

			return cv;
		}
		catch (StringIndexOutOfBoundsException se) {
			log.warn("start or end not found in string");
			return ErrorValue.newErrorValue("start or end not found in string", argResultList.getAll());
		}
		catch (InvalidArgumentException e) {
			log.warn(e.getMessage());
			return ErrorValue.newErrorValue(e.getMessage(), argResultList.getAll());
		}
	}
}
