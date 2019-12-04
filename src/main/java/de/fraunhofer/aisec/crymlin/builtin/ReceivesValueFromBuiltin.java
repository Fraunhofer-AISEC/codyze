
package de.fraunhofer.aisec.crymlin.builtin;

import de.fraunhofer.aisec.analysis.markevaluation.ExpressionEvaluator;
import de.fraunhofer.aisec.analysis.structures.ConstantValue;
import de.fraunhofer.aisec.analysis.structures.ListValue;
import de.fraunhofer.aisec.analysis.structures.MarkIntermediateResult;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class ReceivesValueFromBuiltin implements Builtin {
	private static final Logger log = LoggerFactory.getLogger(ReceivesValueFromBuiltin.class);

	@Override
	public @NonNull String getName() {
		return "_receives_value_from";
	}

	@Override
	public Map<Integer, MarkIntermediateResult> execute(
			Map<Integer, MarkIntermediateResult> arguments,
			ExpressionEvaluator expressionEvaluator) {
		// TODO implement

		// TODO FW: needs to be discussed, I am not clear what this should achieve
		// the example is:
		/*
		 * rule UseRandomIV { using Botan::Cipher_Mode as cm, Botan::AutoSeededRNG as rng when _split(cm.algorithm, "/", 1) == "CBC" && cm.direction ==
		 * Botan::Cipher_Dir::ENCRYPTION ensure _receives_value_from(cm.iv, rng.myValue) onfail NoRandomIV }
		 */

		for (Map.Entry<Integer, MarkIntermediateResult> entry : arguments.entrySet()) {
			if (!(entry.getValue() instanceof ListValue)) {
				log.error("Arguments must be a list");
				continue;
			}
			arguments.put(entry.getKey(), ConstantValue.NULL);
		}
		return arguments;
	}
}
