
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

import java.util.List;

/**
 * This builtin checks if two mark vars are equal
 */
public class Is implements Builtin {
	private static final Logger log = LoggerFactory.getLogger(Is.class);

	@Override
	public @NonNull String getName() {
		return "_is";
	}

	@Override
	public ConstantValue execute(
			ListValue argResultList,
			Integer contextID,
			MarkContextHolder markContextHolder,
			ExpressionEvaluator expressionEvaluator) {

		try {
			List<Vertex> vertices = BuiltinHelper.extractResponsibleVertices(argResultList, 2);

			Vertex v1 = CrymlinQueryWrapper.refersTo(vertices.get(0)).orElse(vertices.get(0));
			Vertex v2 = CrymlinQueryWrapper.refersTo(vertices.get(1)).orElse(vertices.get(1));

			ConstantValue ret = ConstantValue.of(v1.id().equals(v2.id()));
			ret.addResponsibleVertices(vertices.get(0), vertices.get(1));
			return ret;
		}
		catch (InvalidArgumentException e) {
			log.warn(e.getMessage());
			return ErrorValue.newErrorValue(e.getMessage(), argResultList.getAll());
		}

	}
}
