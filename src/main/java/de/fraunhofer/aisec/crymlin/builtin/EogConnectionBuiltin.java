
package de.fraunhofer.aisec.crymlin.builtin;

import de.fraunhofer.aisec.analysis.markevaluation.ExpressionEvaluator;
import de.fraunhofer.aisec.analysis.structures.ConstantValue;
import de.fraunhofer.aisec.analysis.structures.ErrorValue;
import de.fraunhofer.aisec.analysis.structures.ListValue;
import de.fraunhofer.aisec.analysis.structures.MarkContextHolder;
import de.fraunhofer.aisec.crymlin.CrymlinQueryWrapper;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class EogConnectionBuiltin implements Builtin {
	private static final Logger log = LoggerFactory.getLogger(EogConnectionBuiltin.class);

	@Override
	public @NonNull String getName() {
		return "_eog_connection";
	}

	@Override
	public ConstantValue execute(
			ListValue argResultList,
			Integer contextID,
			MarkContextHolder markContextHolder,
			ExpressionEvaluator expressionEvaluator) {

		if (argResultList.size() != 2) {
			log.error("Invalid number of arguments: {}", argResultList.size());
			return ErrorValue.newErrorValue("Invalid number of arguments: {}", argResultList.size());
		}

		if (!(argResultList.get(0) instanceof ConstantValue) || !(argResultList.get(1) instanceof ConstantValue)) {
			log.error("Argument is not a ConstantValue");
			return ErrorValue.newErrorValue("Argument is not a ConstantValue");
		}

		Set<Vertex> responsibleVerticesArg0 = ((ConstantValue) argResultList.get(0)).getResponsibleVertices();
		Set<Vertex> responsibleVerticesArg1 = ((ConstantValue) argResultList.get(1)).getResponsibleVertices();

		if (responsibleVerticesArg0.size() != 1 || responsibleVerticesArg1.size() != 1) {
			log.error("Vertices for arguments not available or invalid");
			return ErrorValue.newErrorValue("Vertices for arguments not available or invalid");
		}

		Vertex arg0 = responsibleVerticesArg0.iterator().next();
		Vertex arg1 = responsibleVerticesArg1.iterator().next();

		if (arg0 == null || arg1 == null) {
			log.error("Vertices for arguments are invalid");
			return ErrorValue.newErrorValue("Vertices for arguments are invalid");
		}

		// now we have one vertex each for arg0 and arg1, both not null

		ConstantValue ret = ConstantValue.of(CrymlinQueryWrapper.eogConnection(arg0, arg1, true));
		ret.addResponsibleVertices(arg0, arg1);
		return ret;

		// TODO FW: needs to be discussed, I am not clear what this should achieve
		// the example is:
		/*
		 * rule UseRandomIV { using Botan::Cipher_Mode as cm, Botan::AutoSeededRNG as rng when _split(cm.algorithm, "/", 1) == "CBC" && cm.direction ==
		 * Botan::Cipher_Dir::ENCRYPTION ensure _receives_value_from(cm.iv, rng.myValue) onfail NoRandomIV }
		 */
	}
}
