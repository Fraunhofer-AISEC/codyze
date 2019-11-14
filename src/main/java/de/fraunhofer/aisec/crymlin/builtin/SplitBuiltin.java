
package de.fraunhofer.aisec.crymlin.builtin;

import de.fraunhofer.aisec.mark.markDsl.Argument;
import de.fraunhofer.aisec.markmodel.MarkContext;
import de.fraunhofer.aisec.markmodel.ExpressionEvaluator;
import de.fraunhofer.aisec.markmodel.ExpressionHelper;
import de.fraunhofer.aisec.markmodel.ResultWithContext;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Method signature: _split(String str, String splitter, int position)
 *
 * <p>
 * This Builtin behaves like str.split(splitter)[position].
 *
 * <p>
 * In case of an error or an empty result, this Builtin returns an Optional.empty.
 */
public class SplitBuiltin implements Builtin {
	private static final Logger log = LoggerFactory.getLogger(ExpressionEvaluator.class);

	@NonNull
	@Override
	public String getName() {
		return "_split";
	}

	@Override
	public ResultWithContext execute(List<Argument> arguments, ExpressionEvaluator expressionEvaluator) {

		ResultWithContext argResult = expressionEvaluator.evaluateArgs(arguments);

		// arguments: String, String, int
		// example:
		// _split("ASD/EFG/JKL", "/", 1) returns "EFG"

		if (argResult == null || !(argResult.get() instanceof List)) {
			return null;
		}
		List argResultList = (List) (argResult.get());

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
			return ResultWithContext.fromExisting(ret, argResult);
		} else {
			return null;
		}
	}
}
