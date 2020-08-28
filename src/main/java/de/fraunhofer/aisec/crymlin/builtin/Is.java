
package de.fraunhofer.aisec.crymlin.builtin;

import de.fraunhofer.aisec.analysis.markevaluation.ExpressionEvaluator;
import de.fraunhofer.aisec.analysis.structures.AnalysisContext;
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
			@NonNull AnalysisContext ctx,
			@NonNull ListValue argResultList,
			@NonNull Integer contextID,
			@NonNull MarkContextHolder markContextHolder,
			@NonNull ExpressionEvaluator expressionEvaluator) {

		try {
			List<Vertex> vertices = BuiltinHelper.extractResponsibleVertices(argResultList, 2);

			Vertex v1 = CrymlinQueryWrapper.refersTo(vertices.get(0)).orElse(vertices.get(0));
			Vertex v2 = CrymlinQueryWrapper.refersTo(vertices.get(1)).orElse(vertices.get(1));

			ConstantValue ret = ConstantValue.of(v1.id().equals(v2.id()));
			ret.addResponsibleVertices(vertices.get(0), vertices.get(1));
			return ret;
		}
		catch (InvalidArgumentException e) {
			// Expected: Did not find a matching vertex v1 or v2. Return false
			log.info("_is({}, {}) returns false", argResultList.get(0), argResultList.get(1), e.getMessage());
			ConstantValue retFalse = ConstantValue.of(false);
			return retFalse;
		}

	}
}
