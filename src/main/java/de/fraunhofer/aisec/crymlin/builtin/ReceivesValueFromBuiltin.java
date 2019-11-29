
package de.fraunhofer.aisec.crymlin.builtin;

import de.fraunhofer.aisec.analysis.markevaluation.ExpressionEvaluator;
import de.fraunhofer.aisec.analysis.scp.ConstantValue;
import de.fraunhofer.aisec.analysis.structures.ResultWithContext;
import de.fraunhofer.aisec.analysis.utils.Utils;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ReceivesValueFromBuiltin implements Builtin {
	@Override
	public @NonNull String getName() {
		return "_receives_value_from";
	}

	@Override
	public Map<Integer, Object> execute(
			Map<Integer, Object> arguments,
			ExpressionEvaluator expressionEvaluator) {
		// TODO implement

		// TODO FW: needs to be discussed, I am not clear what this should achieve
		// the example is:
		/*
		 * rule UseRandomIV { using Botan::Cipher_Mode as cm, Botan::AutoSeededRNG as rng when _split(cm.algorithm, "/", 1) == "CBC" && cm.direction ==
		 * Botan::Cipher_Dir::ENCRYPTION ensure _receives_value_from(cm.iv, rng.myValue) onfail NoRandomIV }
		 */

		for (Map.Entry<Integer, Object> entry : arguments.entrySet()) {
			arguments.put(entry.getKey(), ConstantValue.NULL);
		}
		return arguments;
	}
}
