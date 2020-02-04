
package de.fraunhofer.aisec.crymlin.builtin;

import de.fraunhofer.aisec.analysis.structures.ConstantValue;
import de.fraunhofer.aisec.analysis.structures.ErrorValue;
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

		try {
			BuiltinHelper.verifyArgumentTypesOrThrow(argResultList, ConstantValue.class, ConstantValue.class);

			ConstantValue cv;
			ConstantValue classnameArgument = (ConstantValue) argResultList.get(1);
			if (!classnameArgument.isString()) {
				log.error("second parameter of _is_instance is not a String");
				return ErrorValue.newErrorValue("second parameter of _is_instance is not a String", argResultList.getAll());
			}

			// unify type (Java/C/C++)
			String classname = Utils.unifyType((String) classnameArgument.getValue());
			Set<Vertex> v = ((ConstantValue) argResultList.get(0)).getResponsibleVertices();

			if (v.size() != 1) {
				log.error("Cannot evaluate _is_instance with multiple vertices as input");
				cv = ErrorValue.newErrorValue("Cannot evaluate _is_instance with multiple vertices as input", argResultList.getAll());
			} else {
				Vertex next = v.iterator().next();
				if (next == null) {
					log.error("Vertex is null, cannot check _is_instance");
					cv = ErrorValue.newErrorValue("Vertex is null, cannot check _is_instance", argResultList.getAll());
				} else {
					String type = next.value("type");
					cv = ConstantValue.of(type.equals(classname));
					// todo we could also check `mostPreciseType` once available
				}
			}
			cv.addResponsibleVerticesFrom((ConstantValue) argResultList.get(0),
				(ConstantValue) argResultList.get(1));
			return cv;
		}
		catch (InvalidArgumentException e) {
			log.warn(e.getMessage());
			return ErrorValue.newErrorValue(e.getMessage(), argResultList.getAll());
		}
	}
}
