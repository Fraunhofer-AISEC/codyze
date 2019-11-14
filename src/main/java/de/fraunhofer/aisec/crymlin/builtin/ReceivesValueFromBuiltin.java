
package de.fraunhofer.aisec.crymlin.builtin;

import de.fraunhofer.aisec.mark.markDsl.Argument;
import de.fraunhofer.aisec.markmodel.ExpressionEvaluator;
import de.fraunhofer.aisec.markmodel.MarkContext;
import de.fraunhofer.aisec.markmodel.ResultWithContext;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.Optional;

public class ReceivesValueFromBuiltin implements Builtin {
	@Override
	public @NonNull String getName() {
		return "_receives_value_from";
	}

	@Override
	public @NonNull ResultWithContext execute(List<Argument> arguments, ExpressionEvaluator expressionEvaluator) {
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
