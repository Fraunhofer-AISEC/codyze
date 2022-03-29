
package de.fraunhofer.aisec.codyze.legacy.crymlin.builtin;

import de.fraunhofer.aisec.codyze.legacy.analysis.*;
import de.fraunhofer.aisec.codyze.legacy.analysis.markevaluation.ExpressionEvaluator;
import de.fraunhofer.aisec.codyze.legacy.analysis.resolution.ConstantValue;
import de.fraunhofer.aisec.codyze.legacy.analysis.utils.Utils;
import de.fraunhofer.aisec.cpg.graph.HasType;
import de.fraunhofer.aisec.cpg.graph.statements.expressions.CallExpression;
import de.fraunhofer.aisec.cpg.graph.types.Type;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;

/**
 * Method signature: _is_instance(var instance, String classname)
 *
 * <p>
 * This Builtin behaves like "var instanceof classname".
 *
 * <p>
 * In case of an error or an empty result, this Builtin returns an Optional.empty.
 *
 */
public class IsInstance implements Builtin {
	private static final Logger log = LoggerFactory.getLogger(IsInstance.class);

	@Override
	public @NonNull String getName() {
		return "_is_instance";
	}

	public MarkIntermediateResult execute(
			@NonNull AnalysisContext ctx,
			@NonNull ListValue argResultList,
			@NonNull Integer contextID,
			@NonNull MarkContextHolder markContextHolder,
			ExpressionEvaluator expressionEvaluator) {

		try {
			BuiltinHelper.verifyArgumentTypesOrThrow(argResultList, ConstantValue.class, ConstantValue.class);

			ConstantValue cv;
			ConstantValue classnameArgument = (ConstantValue) argResultList.get(1);
			if (!classnameArgument.isString()) {
				log.warn("second parameter of _is_instance is not a String");
				return ErrorValue.newErrorValue("second parameter of _is_instance is not a String", argResultList.getAll());
			}

			// unify type (Java/C/C++)
			var classname = Utils.unifyType((String) classnameArgument.getValue());
			var v = ((ConstantValue) argResultList.get(0)).getResponsibleNodes();

			if (v.size() != 1) {
				log.warn("Cannot evaluate _is_instance with multiple vertices as input");
				cv = ErrorValue.newErrorValue("Cannot evaluate _is_instance with multiple vertices as input", argResultList.getAll());
			} else {
				var next = v.iterator().next();
				if (!(next instanceof HasType)) {
					log.warn("Node is null or does not implement HasType, cannot check _is_instance");
					cv = ErrorValue.newErrorValue("Node is null or does not implement HasType, cannot check _is_instance", argResultList.getAll());
				} else if (next instanceof CallExpression) {
					// CallExpressions do not have a "type" property. We rather get their type from the called object.
					String type = ((CallExpression) next).getFqn();
					cv = ConstantValue.of(type.equals(classname));
				} else {
					var types = new HashSet<>(((HasType) next).getPossibleSubTypes());
					types.add(((HasType) next).getType());

					// Get list of possible types, including the most specific type.
					boolean match = types
							.stream()
							.map(Type::getTypeName)
							.anyMatch(classname::equals);
					cv = ConstantValue.of(match);
				}
			}
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
