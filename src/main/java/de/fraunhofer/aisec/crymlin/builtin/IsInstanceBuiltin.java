
package de.fraunhofer.aisec.crymlin.builtin;

import de.fraunhofer.aisec.analysis.structures.ConstantValue;
import de.fraunhofer.aisec.analysis.structures.ListValue;
import de.fraunhofer.aisec.analysis.structures.MarkIntermediateResult;
import de.fraunhofer.aisec.analysis.utils.Utils;
import de.fraunhofer.aisec.analysis.markevaluation.ExpressionEvaluator;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Method signature: _is_instance(var instance, String classname)
 *
 * <p>
 * This Builtin behaves like "var instanceof classname".
 *
 * <p>
 * In case of an error or an empty result, this Builtin returns an Optional.empty.
 *
 *
 * FIxME does is_instance even make sense? we fill markvars by looking for methods with the exact signature as specified in the entity. we could check for that, but isnt
 * it by default true for all nodes we find?
 */
public class IsInstanceBuiltin implements Builtin {
	private static final Logger log = LoggerFactory.getLogger(IsInstanceBuiltin.class);

	@Override
	public @NonNull String getName() {
		return "_is_instance";
	}

	@Override
	public Map<Integer, MarkIntermediateResult> execute(
			Map<Integer, MarkIntermediateResult> arguments,
			ExpressionEvaluator expressionEvaluator) {

		Map<Integer, MarkIntermediateResult> result = new HashMap<>();

		for (Map.Entry<Integer, MarkIntermediateResult> entry : arguments.entrySet()) {

			if (!(entry.getValue() instanceof ListValue)) {
				log.error("Arguments must be a list");
				continue;
			}

			ListValue argResultList = (ListValue) (entry.getValue());

			ConstantValue cv = null;
			Object classnameArgument = argResultList.get(1);
			if (!(classnameArgument instanceof ConstantValue)
					||
					!((ConstantValue) classnameArgument).isString()) {
				log.error("second parameter of _is_instance is not a String");
				cv = ConstantValue.NULL;
			}
			if (!(argResultList.get(0) instanceof ConstantValue)) {
				log.error("first parameter of _is_instance is not a ConstantValue containing a Vertex");
				cv = ConstantValue.NULL;
			}

			if (cv == null) {
				// unify type (Java/C/C++)
				String classname = Utils.unifyType((String) ((ConstantValue) classnameArgument).getValue());
				Set<Vertex> v = ((ConstantValue) argResultList.get(0)).getResponsibleVertices();

				if (v.size() != 1) {
					log.error("Cannot evaluate _is_instance with multiple vertices as input");
					cv = ConstantValue.NULL;
				} else {
					Vertex next = v.iterator().next();
					if (next == null) {
						log.error("Vertex is null, cannot check _is_instance");
						cv = ConstantValue.NULL;
					} else {
						String type = next.value("type");
						cv = ConstantValue.of(type.equals(classname));
					}
				}
				cv.addResponsibleVerticesFrom((ConstantValue) argResultList.get(0),
					(ConstantValue) argResultList.get(1));
			}
			result.put(entry.getKey(), cv);
		}
		return result;
	}
}
