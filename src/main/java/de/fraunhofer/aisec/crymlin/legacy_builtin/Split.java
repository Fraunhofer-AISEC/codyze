
package de.fraunhofer.aisec.crymlin.legacy_builtin;

import de.fraunhofer.aisec.analysis.markevaluation.LegacyExpressionEvaluator;
import de.fraunhofer.aisec.analysis.markevaluation.ExpressionHelper;
import de.fraunhofer.aisec.analysis.structures.AnalysisContext;
import de.fraunhofer.aisec.analysis.structures.ConstantValue;
import de.fraunhofer.aisec.analysis.structures.ErrorValue;
import de.fraunhofer.aisec.analysis.structures.ListValue;
import de.fraunhofer.aisec.analysis.structures.MarkContextHolder;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.PatternSyntaxException;

/**
 * Method signature: _split(String str, String splitter, int position)
 *
 * This Builtin behaves like str.split(splitter)[position].
 *
 * In case of an error, this Builtin returns an ErrorValue;
 */
public class Split implements Builtin {
	private static final Logger log = LoggerFactory.getLogger(Split.class);

	@NonNull
	@Override
	public String getName() {
		return "_split";
	}

	@Override
	public ConstantValue execute(
			@NonNull AnalysisContext ctx,
			@NonNull ListValue argResultList,
			@NonNull Integer contextID,
			@NonNull MarkContextHolder markContextHolder,
			@NonNull LegacyExpressionEvaluator expressionEvaluator) {

		// arguments: String, String, int
		// example:
		// _split("ASD/EFG/JKL", "/", 1) returns "EFG"
		String regex = "";

		try {
			BuiltinHelper.verifyArgumentTypesOrThrow(argResultList, ConstantValue.class, ConstantValue.class, ConstantValue.class);

			String s = ExpressionHelper.asString(argResultList.get(0));
			regex = ExpressionHelper.asString(argResultList.get(1));
			Number index = ExpressionHelper.asNumber(argResultList.get(2));

			if (s == null || regex == null || index == null) {
				log.warn("One of the arguments for _split was not the expected type, or not initialized/resolved");
				return ErrorValue.newErrorValue("One of the arguments for _split was not the expected type, or not initialized/resolved", argResultList.getAll());
			}

			log.info("args are: {}; {}; {}", s, regex, index);

			String ret;
			String[] splitted = s.split(regex);
			if (index.intValue() < splitted.length) {
				ret = splitted[index.intValue()];
			} else {
				log.warn("{} did not have an {}-th element when split by '{}'", s, index, regex);
				return ErrorValue.newErrorValue(String.format("%s did not have an %s-th element when split by '%s'", s, index.toString(), regex),
					argResultList.getAll());
			}

			ConstantValue cv = ConstantValue.of(ret);

			cv.addResponsibleVerticesFrom((ConstantValue) argResultList.get(0),
				(ConstantValue) argResultList.get(1),
				(ConstantValue) argResultList.get(2));

			return cv;
		}
		catch (PatternSyntaxException e) {
			log.warn("Pattern for _split wrong: '{}': {}", regex, e.getMessage());
			return ErrorValue.newErrorValue(String.format("Pattern for _split wrong: '%s': %s", regex, e.getMessage()), argResultList.getAll());
		}
		catch (InvalidArgumentException e) {
			log.warn(e.getMessage());
			return ErrorValue.newErrorValue(e.getMessage(), argResultList.getAll());
		}
	}
}
