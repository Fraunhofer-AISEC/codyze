
package de.fraunhofer.aisec.crymlin.builtin;

import de.fraunhofer.aisec.analysis.markevaluation.ExpressionEvaluator;
import de.fraunhofer.aisec.analysis.structures.ResultWithContext;
import org.checkerframework.checker.nullness.qual.NonNull;

public class ReceivesValueFromBuiltin implements Builtin {
	@Override
	public @NonNull String getName() {
		return "_receives_value_from";
	}

	@Override
	public ResultWithContext execute(ResultWithContext arguments, ExpressionEvaluator expressionEvaluator) {
		// TODO implement

		// TODO FW: needs to be discussed, I am not clear what this should achieve
		// the example is:
		/*
		 * rule UseRandomIV { using Botan::Cipher_Mode as cm, Botan::AutoSeededRNG as rng when _split(cm.algorithm, "/", 1) == "CBC" && cm.direction ==
		 * Botan::Cipher_Dir::ENCRYPTION ensure _receives_value_from(cm.iv, rng.myValue) onfail NoRandomIV }
		 */

		return null;
	}
}
