
package de.fraunhofer.aisec.codyze.crymlin.builtin;

import de.fraunhofer.aisec.codyze.analysis.markevaluation.ExpressionEvaluator;
import de.fraunhofer.aisec.codyze.analysis.structures.*;
import de.fraunhofer.aisec.cpg.graph.declarations.FunctionDeclaration;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static de.fraunhofer.aisec.codyze.analysis.markevaluation.EvaluationHelperKt.getContainingFunction;

/**
 * This builtin checks if 2 vertices (given via the two markvar parameters) are contained in the same function/method
 */
public class InsideSameFunction implements Builtin {
	private static final Logger log = LoggerFactory.getLogger(InsideSameFunction.class);

	@Override
	public @NonNull String getName() {
		return "_inside_same_function";
	}

	@Override
	public ConstantValue execute(
			@NonNull AnalysisContext ctx,
			@NonNull ListValue argResultList,
			@NonNull Integer contextID,
			@NonNull MarkContextHolder markContextHolder,
			ExpressionEvaluator expressionEvaluator) {

		try {
			// could be extended to a variable number of arguments quite easile
			var vertices = BuiltinHelper.extractResponsibleNodes(argResultList, 2);
			// now we have one vertex each for arg0 and arg1, both not null

			FunctionDeclaration lastIndex = null;

			boolean allInSame = true;

			for (var v : vertices) {
				var containingFunction = getContainingFunction(v);
				if (containingFunction == null) {
					log.warn("Instance vertex {} is not contained in a method/function, cannot evaluate {}", v.getCode(), getName());
					return ErrorValue
							.newErrorValue(String.format("Instance vertex %s is not contained in a method/function, cannot evaluate %s", v.getCode(), getName()));
				}
				if (lastIndex == null || lastIndex == containingFunction) {
					lastIndex = containingFunction;
				} else {
					allInSame = false;
					break;
				}
			}
			ConstantValue ret = ConstantValue.of(allInSame);
			ret.addResponsibleNodes(vertices);
			return ret;

		}
		catch (InvalidArgumentException e) {
			log.warn(e.getMessage());
			return ErrorValue.newErrorValue(e.getMessage(), argResultList.getAll());
		}

	}
}
