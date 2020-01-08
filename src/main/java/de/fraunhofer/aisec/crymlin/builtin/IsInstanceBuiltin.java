
package de.fraunhofer.aisec.crymlin.builtin;

import de.fraunhofer.aisec.analysis.structures.ConstantValue;
import de.fraunhofer.aisec.analysis.structures.ListValue;
import de.fraunhofer.aisec.analysis.structures.MarkContextHolder;
import de.fraunhofer.aisec.analysis.utils.Utils;
import de.fraunhofer.aisec.analysis.markevaluation.ExpressionEvaluator;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 */
public class IsInstanceBuiltin implements Builtin {
	private static final Logger log = LoggerFactory.getLogger(IsInstanceBuiltin.class);

	@Override
	public @NonNull String getName() {
		return "_is_instance";
	}

	public ConstantValue execute(
			ListValue argResultList,
			Integer contextID,
			MarkContextHolder markContextHolder,
			ExpressionEvaluator expressionEvaluator) {

		ConstantValue cv = null;
		Object classnameArgument = argResultList.get(1);
		if (!(classnameArgument instanceof ConstantValue)
				||
				!((ConstantValue) classnameArgument).isString()) {
			log.error("second parameter of _is_instance is not a String");
			cv = ConstantValue.newNull();
		}
		if (!(argResultList.get(0) instanceof ConstantValue)) {
			log.error("first parameter of _is_instance is not a ConstantValue containing a Vertex");
			cv = ConstantValue.newNull();
		}

		if (cv == null) {
			// unify type (Java/C/C++)
			String classname = Utils.unifyType((String) ((ConstantValue) classnameArgument).getValue());
			Set<Vertex> v = ((ConstantValue) argResultList.get(0)).getResponsibleVertices();

			if (v.size() != 1) {
				log.error("Cannot evaluate _is_instance with multiple vertices as input");
				cv = ConstantValue.newNull();
			} else {
				Vertex next = v.iterator().next();
				if (next == null) {
					log.error("Vertex is null, cannot check _is_instance");
					cv = ConstantValue.newNull();
				} else {
					String type = next.value("type");
					cv = ConstantValue.of(type.equals(classname));
				}
			}
			cv.addResponsibleVerticesFrom((ConstantValue) argResultList.get(0),
				(ConstantValue) argResultList.get(1));
		}
		return cv;
	}
}
