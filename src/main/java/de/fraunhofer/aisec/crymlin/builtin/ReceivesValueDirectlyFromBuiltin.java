
package de.fraunhofer.aisec.crymlin.builtin;

import de.fraunhofer.aisec.analysis.markevaluation.ExpressionEvaluator;
import de.fraunhofer.aisec.analysis.structures.ConstantValue;
import de.fraunhofer.aisec.analysis.structures.ErrorValue;
import de.fraunhofer.aisec.analysis.structures.ListValue;
import de.fraunhofer.aisec.analysis.structures.MarkContextHolder;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReceivesValueDirectlyFromBuiltin implements Builtin {
	private static final Logger log = LoggerFactory.getLogger(ReceivesValueFromBuiltin.class);

	@Override
	public @NonNull String getName() {
		return "_receives_value_directly_from";
	}

	@Override
	public ConstantValue execute(
			ListValue argResultList,
			Integer contextID,
			MarkContextHolder markContextHolder,
			ExpressionEvaluator expressionEvaluator) {
		// TODO implement
		log.warn("the builtin _receives_value_directly_from is not implemented yet");

		return ErrorValue.newErrorValue("ReceivesValueDirectlyFromBuiltin not implemented yet");
	}
}
