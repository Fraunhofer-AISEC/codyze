
package de.fraunhofer.aisec.crymlin.builtin;

import de.fraunhofer.aisec.analysis.markevaluation.ExpressionEvaluator;
import de.fraunhofer.aisec.analysis.markevaluation.ExpressionHelper;
import de.fraunhofer.aisec.analysis.structures.ResultWithContext;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Method signature: _split(String str, String splitter, int position)
 *
 * This Builtin behaves like str.split(splitter)[position].
 *
 * In case of an error, this Builtin returns null;
 */
public class SplitBuiltin implements Builtin {
	private static final Logger log = LoggerFactory.getLogger(SplitBuiltin.class);

	@NonNull
	@Override
	public String getName() {
		return "_split";
	}

	@Override
	public ResultWithContext execute(ResultWithContext arguments, ExpressionEvaluator expressionEvaluator) {

		// arguments: String, String, int
		// example:
		// _split("ASD/EFG/JKL", "/", 1) returns "EFG"

		List argResultList = (List) (arguments.get());

		String s = ExpressionHelper.asString(argResultList.get(0));
		String regex = ExpressionHelper.asString(argResultList.get(1));
		Number index = ExpressionHelper.asNumber(argResultList.get(2));

		if (s == null || regex == null || index == null) {
			return null;
		}

		log.debug("args are: " + s + "; " + regex + "; " + index);
		String ret = null;
		String[] splitted = s.split(regex);
		if (index.intValue() < splitted.length) {
			ret = splitted[index.intValue()];
		}

		if (ret != null) {
			// StringLiteral stringResult = new MarkDslFactoryImpl().createStringLiteral();
			// stringResult.setValue(ret);
			return ResultWithContext.fromExisting(ret, arguments);
		} else {
			return null;
		}
	}
}
