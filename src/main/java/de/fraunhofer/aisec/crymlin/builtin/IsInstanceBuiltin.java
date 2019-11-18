
package de.fraunhofer.aisec.crymlin.builtin;

import de.fraunhofer.aisec.analysis.utils.Utils;
import de.fraunhofer.aisec.analysis.markevaluation.ExpressionEvaluator;
import de.fraunhofer.aisec.analysis.structures.ResultWithContext;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
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
	public ResultWithContext execute(ResultWithContext arguments, ExpressionEvaluator expressionEvaluator) {

		List argResultList = (List) (arguments.get());

		Object classnameArgument = argResultList.get(1);
		if (!(classnameArgument instanceof String)) {
			log.error("var of is_instance is empty");
			return null;
		}
		// unify type (Java/C/C++)
		String classname = Utils.unifyType((String) classnameArgument);
		Set<Vertex> v = arguments.getResponsibleVertices();

		if (v.size() != 1) {
			log.error("Cannot evaluate _is_instance with multiple vertices as input");
			return null;
		} else {
			String type = v.iterator().next().value("type");
			return ResultWithContext.fromExisting(type.equals(classname), arguments);
		}
	}
}
