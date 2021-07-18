
package de.fraunhofer.aisec.codyze.crymlin.builtin;

import de.fraunhofer.aisec.codyze.analysis.*;
import de.fraunhofer.aisec.codyze.analysis.markevaluation.ExpressionEvaluator;
import de.fraunhofer.aisec.codyze.analysis.markevaluation.ExpressionHelper;
import de.fraunhofer.aisec.codyze.analysis.resolution.ConstantValue;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static de.fraunhofer.aisec.codyze.analysis.markevaluation.EvaluationHelperKt.getField;
import static de.fraunhofer.aisec.codyze.analysis.markevaluation.EvaluationHelperKt.getInitializerValue;

/**
 *
 */
public class ConstValue implements Builtin {
	private static final Logger log = LoggerFactory.getLogger(ConstValue.class);

	@Override
	public @NonNull String getName() {
		return "_const_value";
	}

	@Override
	public ConstantValue execute(
			@NonNull AnalysisContext ctx,
			@NonNull ListValue argResultList,
			@NonNull Integer contextID,
			@NonNull MarkContextHolder markContextHolder,
			ExpressionEvaluator expressionEvaluator) {

		try {
			BuiltinHelper.verifyArgumentTypesOrThrow(argResultList, ConstantValue.class);

			String s = ExpressionHelper.asString(argResultList.get(0));

			if (s == null) {
				log.warn("Argument of _const_value is not a string.");
				return ErrorValue.newErrorValue("Argument of _const_value is not a string.", argResultList.getAll());
			}

			log.info("args are: {}", s);
			if (!s.contains(".")) {
				log.warn("Argument is not fully qualified");
				return ErrorValue.newErrorValue("Argument is not fully qualified", argResultList.getAll());
			}

			int i = s.lastIndexOf('.');
			String fqnClassName = s.substring(0, i);
			String fieldName = s.substring(i + 1);

			// check if this is a field and we have a value from the field
			var field = getField(expressionEvaluator.getGraph(), fqnClassName, fieldName);

			if (field == null) {
				log.warn("Unknown field value {}", s);
				return ErrorValue.newErrorValue("Unknown field value " + s, argResultList.getAll());
			}

			Object initializerValue = getInitializerValue(field);
			if (initializerValue == null) {
				log.warn("Unknown cannot determine value of {}", s);
				return ErrorValue.newErrorValue("Unknown cannot determine value of " + s, argResultList.getAll());
			}

			ConstantValue of = ConstantValue.of(initializerValue);
			of.addResponsibleNode(field);
			return of;
		}
		catch (InvalidArgumentException e) {
			log.warn(e.getMessage());
			return ErrorValue.newErrorValue(e.getMessage(), argResultList.getAll());
		}
	}
}
