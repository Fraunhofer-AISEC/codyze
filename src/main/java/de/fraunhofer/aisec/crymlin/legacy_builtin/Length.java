
package de.fraunhofer.aisec.crymlin.legacy_builtin;

import de.fraunhofer.aisec.analysis.markevaluation.LegacyExpressionEvaluator;
import de.fraunhofer.aisec.analysis.structures.AnalysisContext;
import de.fraunhofer.aisec.analysis.structures.ConstantValue;
import de.fraunhofer.aisec.analysis.structures.ErrorValue;
import de.fraunhofer.aisec.analysis.structures.ListValue;
import de.fraunhofer.aisec.analysis.structures.MarkContextHolder;
import de.fraunhofer.aisec.crymlin.CrymlinQueryWrapper;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * This builtin gets the length for a variable.
 *
 * currently this can only return the dimension of an array, and only for Java
 */
public class Length implements Builtin {
	private static final Logger log = LoggerFactory.getLogger(Length.class);

	@Override
	public @NonNull String getName() {
		return "_length";
	}

	@Override
	public ConstantValue execute(
			@NonNull AnalysisContext ctx,
			@NonNull ListValue argResultList,
			@NonNull Integer contextID,
			@NonNull MarkContextHolder markContextHolder,
			@NonNull LegacyExpressionEvaluator expressionEvaluator) {

		try {
			List<Vertex> vertices = BuiltinHelper.extractResponsibleVertices(argResultList, 1);

			Optional<Vertex> ini = CrymlinQueryWrapper.getInitializerFor(vertices.get(0));

			if (ini.isPresent()) {
				Iterator<Edge> dimensions = ini.get().edges(Direction.OUT, "DIMENSIONS");
				if (dimensions.hasNext()) {
					Long value = dimensions.next().inVertex().value("value");
					ConstantValue ret = ConstantValue.of(value);
					ret.addResponsibleVertices(vertices);
					return ret;
				}
			}

			log.warn("Could not determine length");
			return ErrorValue.newErrorValue("Could not determine length", argResultList.getAll());

		}
		catch (InvalidArgumentException e) {
			log.warn(e.getMessage());
			return ErrorValue.newErrorValue(e.getMessage(), argResultList.getAll());
		}

	}
}
