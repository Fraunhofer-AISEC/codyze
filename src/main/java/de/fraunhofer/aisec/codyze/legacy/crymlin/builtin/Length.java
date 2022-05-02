
package de.fraunhofer.aisec.codyze.legacy.crymlin.builtin;

import de.fraunhofer.aisec.codyze.legacy.analysis.*;
import de.fraunhofer.aisec.codyze.legacy.analysis.markevaluation.ExpressionEvaluator;
import de.fraunhofer.aisec.codyze.legacy.analysis.resolution.ConstantValue;
import de.fraunhofer.aisec.cpg.graph.statements.expressions.ArrayCreationExpression;
import de.fraunhofer.aisec.cpg.graph.statements.expressions.Literal;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static de.fraunhofer.aisec.codyze.legacy.analysis.markevaluation.EvaluationHelperKt.getInitializerFor;

/**
 * This builtin gets the length for a variable.
 *
 * currently this can only return the dimension of an array, and only for Java
 */
public class Length implements Builtin {
	private static final Logger log = LoggerFactory.getLogger(Length.class);

	@Override
	public @NonNull String getName() {
		return "_length";
	}

	@Override
	public MarkIntermediateResult execute(
			@NonNull AnalysisContext ctx,
			@NonNull ListValue argResultList,
			@NonNull Integer contextID,
			@NonNull MarkContextHolder markContextHolder,
			ExpressionEvaluator expressionEvaluator) {
		try {
			var vertices = BuiltinHelper.extractResponsibleNodes(argResultList, 1);

			var ini = getInitializerFor(vertices.get(0));

			if (ini instanceof ArrayCreationExpression) {
				var dimensions = ((ArrayCreationExpression) ini).getDimensions();
				if (!dimensions.isEmpty()) {
					var first = dimensions.get(0);

					if (first instanceof Literal<?>) {
						var value = ((Literal<?>) first).getValue();
						ConstantValue ret = ConstantValue.of(value);
						ret.addResponsibleNodes(vertices);
						return ret;
					}
				}
			}

			log.warn("Could not determine length");
			return ErrorValue.newErrorValue("Could not determine length", argResultList.getAll());

		}
		catch (InvalidArgumentException e) {
			log.warn(e.getMessage());
			return ErrorValue.newErrorValue(e.getMessage(), argResultList.getAll());
		}

	}
}
