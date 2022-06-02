
package de.fraunhofer.aisec.codyze.legacy.crymlin.builtin;

import de.fraunhofer.aisec.codyze.legacy.analysis.*;
import de.fraunhofer.aisec.codyze.legacy.analysis.markevaluation.ExpressionEvaluator;
import de.fraunhofer.aisec.codyze.legacy.analysis.resolution.ConstantValue;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;

/**
 * Method signature: _subset(List subset, List superset)
 * <p>
 * This Builtin determines if the first list is a subset of the second list.
 * <p>
 * In case of an error, this Builtin returns an ErrorValue;
 */
public class Subset implements Builtin {

	private static final Logger log = LoggerFactory.getLogger(Subset.class);

	@NonNull
	@Override
	public String getName() {
		return "_subset";
	}

	/**
	 * @param ctx                 The analysis context
	 * @param argResultList       Resolved argumentsList for one context of the Builtin function call.
	 * @param contextID
	 * @param markContextHolder
	 * @param expressionEvaluator the expressionEvaluator, this builtin is called from
	 * @return
	 */
	@Override
	public MarkIntermediateResult execute(@NonNull AnalysisContext ctx, @NonNull ListValue argResultList, @NonNull Integer contextID,
			@NonNull MarkContextHolder markContextHolder, ExpressionEvaluator expressionEvaluator) {

		try {
			BuiltinHelper.verifyArgumentTypesOrThrow(argResultList, ListValue.class, ListValue.class);

			ListValue arg0 = (ListValue) argResultList.get(0);
			ListValue arg1 = (ListValue) argResultList.get(1);

			var superset = new HashSet<>(arg1.getAll());
			boolean isSubset = superset.containsAll(arg0.getAll());

			ConstantValue cv = ConstantValue.of(isSubset);

			cv.addResponsibleNodes(arg0.getResponsibleNodes());
			cv.addResponsibleNodes(arg1.getResponsibleNodes());

			return cv;
		}
		catch (InvalidArgumentException e) {
			log.error("arguments must be lists");
			return ErrorValue.newErrorValue("arguments must be lists", argResultList.getAll());
		}
	}

}
