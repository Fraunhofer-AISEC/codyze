
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
import java.util.Optional;

/**
 * This builtin checks if 2 vertices (given via the two markvar parameters) are contained in the same function/method
 */
public class InsideSameFunction implements Builtin {
	private static final Logger log = LoggerFactory.getLogger(InsideSameFunction.class);

	@Override
	public @NonNull String getName() {
		return "_inside_same_function";
	}

	@Override
	public ConstantValue execute(
			ListValue argResultList,
			Integer contextID,
			MarkContextHolder markContextHolder,
			ExpressionEvaluator expressionEvaluator) {

		try {
			// could be extended to a variable number of arguments quite easile
			List<Vertex> vertices = BuiltinHelper.extractResponsibleVertices(argResultList, 2);
			// now we have one vertex each for arg0 and arg1, both not null

			Long lastIndex = -1L;

			boolean allInSame = true;

			for (Vertex v : vertices) {
				Optional<Vertex> containingFunction = CrymlinQueryWrapper.getContainingFunction(v, expressionEvaluator.getCrymlinTraversal());
				if (containingFunction.isEmpty()) {
					log.warn("Instance vertex {} is not contained in a method/function, cannot evaluate {}", v.property("code"), getName());
					return ErrorValue
							.newErrorValue(String.format("Instance vertex %s is not contained in a method/function, cannot evaluate %s", v.property("code"), getName()));
				}
				if (lastIndex == -1L || lastIndex.equals(containingFunction.get().id())) {
					lastIndex = (Long) containingFunction.get().id();
				} else {
					allInSame = false;
					break;
				}
			}
			ConstantValue ret = ConstantValue.of(allInSame);
			ret.addResponsibleVertices(vertices);
			return ret;

		}
		catch (InvalidArgumentException e) {
			log.warn(e.getMessage());
			return ErrorValue.newErrorValue(e.getMessage(), argResultList.getAll());
		}

	}
}
