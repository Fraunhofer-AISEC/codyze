
package de.fraunhofer.aisec.crymlin.builtin;

import de.fraunhofer.aisec.analysis.markevaluation.ExpressionEvaluator;
import de.fraunhofer.aisec.analysis.markevaluation.ExpressionHelper;
import de.fraunhofer.aisec.analysis.structures.ConstantValue;
import de.fraunhofer.aisec.analysis.structures.ErrorValue;
import de.fraunhofer.aisec.analysis.structures.ListValue;
import de.fraunhofer.aisec.analysis.structures.MarkContextHolder;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	public ConstantValue execute(
			ListValue argResultList,
			Integer contextID,
			MarkContextHolder markContextHolder,
			ExpressionEvaluator expressionEvaluator) {

		// arguments: String, String, int
		// example:
		// _split("ASD/EFG/JKL", "/", 1) returns "EFG"

		String s = ExpressionHelper.asString(argResultList.get(0));
		String regex = ExpressionHelper.asString(argResultList.get(1));
		Number index = ExpressionHelper.asNumber(argResultList.get(2));

		if (s == null || regex == null || index == null) {
			log.error("One of the arguments was not the expected type");
			return ErrorValue.newErrorValue("One of the arguments was not the expected type");
		}

		log.debug("args are: {}; {}; {}", s, regex, index);
		String ret = null;
		String[] splitted = s.split(regex);
		if (index.intValue() < splitted.length) {
			ret = splitted[index.intValue()];
		} else {
			log.error("{} did not have an {}-th element when split by '{}'", s, index, regex);
			return ErrorValue.newErrorValue("{} did not have an {}-th element when split by '{}'", s, index, regex);
		}

		ConstantValue cv = ConstantValue.of(ret);
		// StringLiteral stringResult = new MarkDslFactoryImpl().createStringLiteral();
		// stringResult.setValue(ret);

		cv.addResponsibleVerticesFrom((ConstantValue) argResultList.get(0),
			(ConstantValue) argResultList.get(1),
			(ConstantValue) argResultList.get(2));

		return cv;
	}
}
