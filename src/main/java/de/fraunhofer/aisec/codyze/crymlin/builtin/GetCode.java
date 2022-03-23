
package de.fraunhofer.aisec.codyze.crymlin.builtin;

import de.fraunhofer.aisec.codyze.analysis.*;
import de.fraunhofer.aisec.codyze.analysis.markevaluation.ExpressionEvaluator;
import de.fraunhofer.aisec.codyze.analysis.resolution.ConstantValue;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Method signature: _get_code(var instance)
 *
 * <p>
 * This Builtin returns the corresponding source code of a MARK variable.
 *
 * <p>
 * In case of an error or an empty result, this Builtin returns an Optional.empty.
 *
 */
public class GetCode implements Builtin {
	private static final Logger log = LoggerFactory.getLogger(IsInstance.class);

	@Override
	public @NonNull String getName() {
		return "_get_code";
	}

	public MarkIntermediateResult execute(
			@NonNull AnalysisContext ctx,
			@NonNull ListValue argResultList,
			@NonNull Integer contextID,
			@NonNull MarkContextHolder markContextHolder,
			ExpressionEvaluator expressionEvaluator) {

		try {
			BuiltinHelper.verifyArgumentTypesOrThrow(argResultList, ConstantValue.class);

			ConstantValue cv;
			var v = ((ConstantValue) argResultList.get(0)).getResponsibleNodes();

			if (v.size() != 1) {
				log.warn("Cannot evaluate _get_code with multiple vertices as input");
				cv = ErrorValue.newErrorValue("Cannot evaluate _get_code with multiple vertices as input", argResultList.getAll());
			} else {
				var next = v.iterator().next();
				if (next == null) {
					log.warn("Vertex is null, cannot apply _get_code");
					cv = ErrorValue.newErrorValue("Vertex is null, cannot apply _get_code", argResultList.getAll());
				} else {
					var srcCode = next.getCode();
					if (srcCode == null) {
						log.warn("Failed to get source code.");
						cv = ErrorValue.newErrorValue("Failed to get source code", argResultList.getAll());
					} else {
						log.debug("Parsed code as: {}", srcCode);
						cv = ConstantValue.of(srcCode);
					}
				}
			}

			cv.addResponsibleNodesFrom((ConstantValue) argResultList.get(0));
			return cv;
		}
		catch (InvalidArgumentException e) {
			log.warn(e.getMessage());
			return ErrorValue.newErrorValue(e.getMessage(), argResultList.getAll());
		}
	}
}