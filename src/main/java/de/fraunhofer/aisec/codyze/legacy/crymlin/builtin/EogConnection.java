
package de.fraunhofer.aisec.codyze.legacy.crymlin.builtin;

import de.fraunhofer.aisec.codyze.legacy.analysis.*;
import de.fraunhofer.aisec.codyze.legacy.analysis.markevaluation.ExpressionEvaluator;
import de.fraunhofer.aisec.codyze.legacy.analysis.resolution.ConstantValue;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static de.fraunhofer.aisec.codyze.legacy.analysis.markevaluation.EvaluationHelperKt.hasEOGTo;

/**
 * this Builtin checks if there is an arbitrary EOG-connection between the two given vertices (responsible for the markvars given as parameters)
 */
public class EogConnection implements Builtin {
	private static final Logger log = LoggerFactory.getLogger(EogConnection.class);

	@Override
	public @NonNull String getName() {
		return "_eog_connection";
	}

	@Override
	public MarkIntermediateResult execute(
			@NonNull AnalysisContext ctx,
			@NonNull ListValue argResultList,
			@NonNull Integer contextID,
			@NonNull MarkContextHolder markContextHolder,
			ExpressionEvaluator expressionEvaluator) {
		try {
			var vertices = BuiltinHelper.extractResponsibleNodes(argResultList, 2);
			// now we have one vertex each for arg0 and arg1, both not null

			ConstantValue ret = ConstantValue.of(hasEOGTo(vertices.get(0), vertices.get(1), true));
			ret.addResponsibleNodes(vertices.get(0), vertices.get(1));
			return ret;

		}
		catch (InvalidArgumentException e) {
			log.warn(e.getMessage());
			return ErrorValue.newErrorValue(e.getMessage(), argResultList.getAll());
		}
	}
}
