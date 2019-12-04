
package de.fraunhofer.aisec.crymlin.builtin;

import de.fraunhofer.aisec.analysis.markevaluation.ExpressionEvaluator;
import de.fraunhofer.aisec.analysis.markevaluation.ExpressionHelper;
import de.fraunhofer.aisec.analysis.structures.ConstantValue;
import de.fraunhofer.aisec.analysis.structures.ListValue;
import de.fraunhofer.aisec.analysis.structures.MarkIntermediateResult;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	public Map<Integer, MarkIntermediateResult> execute(Map<Integer, MarkIntermediateResult> arguments, ExpressionEvaluator expressionEvaluator) {

		// arguments: String, String, int
		// example:
		// _split("ASD/EFG/JKL", "/", 1) returns "EFG"

		Map<Integer, MarkIntermediateResult> result = new HashMap<>();

		for (Map.Entry<Integer, MarkIntermediateResult> entry : arguments.entrySet()) {

			if (!(entry.getValue() instanceof ListValue)) {
				log.error("Arguments must be a list");
				continue;
			}

			ListValue argResultList = (ListValue) (entry.getValue());

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

			ConstantValue cv;
			if (ret != null) {
				// StringLiteral stringResult = new MarkDslFactoryImpl().createStringLiteral();
				// stringResult.setValue(ret);
				cv = ConstantValue.of(ret);
			} else {
				cv = ConstantValue.NULL;
			}
			cv.addResponsibleVerticesFrom((ConstantValue) argResultList.get(0),
				(ConstantValue) argResultList.get(1),
				(ConstantValue) argResultList.get(2));
			result.put(entry.getKey(), cv);
		}

		return result;
	}
}
