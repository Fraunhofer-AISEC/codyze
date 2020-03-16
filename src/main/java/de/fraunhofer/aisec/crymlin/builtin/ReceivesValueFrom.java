
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

import java.util.*;

/**
 * Method signature: _receives_value_from(var target, var source).
 *
 * Returns true, if there is a data flow from source to target.
 *
 * Data flows are only traced intraprocedurally and this builtin will return true if <i>any</i> flow exists.
 */
public class ReceivesValueFrom implements Builtin {
	private static final Logger log = LoggerFactory.getLogger(ReceivesValueFrom.class);

	@Override
	public @NonNull String getName() {
		return "_receives_value_from";
	}

	@Override
	public ConstantValue execute(
			ListValue argResultList,
			Integer contextID,
			MarkContextHolder markContextHolder,
			ExpressionEvaluator expressionEvaluator) {

		try {
			// We expect a source and a target vertex
			List<Vertex> vertices = BuiltinHelper.extractResponsibleVertices(argResultList, 2);
			if (vertices.size() != 2) {
				return ErrorValue.newErrorValue("Could not resolve arguments of _receives_value_from");
			}
			Vertex targetV = vertices.get(0);
			Vertex sourceV = vertices.get(1);

			// Follow DFG edges backwards, avoiding loops
			Set<Vertex> seen = new HashSet<>();
			Deque<Vertex> worklist = new ArrayDeque<>();
			worklist.addAll(CrymlinQueryWrapper.getDFGSources(targetV));
			while (!worklist.isEmpty()) {
				Vertex currentV = worklist.pop();
				if (seen.contains(currentV)) {
					continue;
				}
				seen.add(currentV);
				if (currentV.id().equals(sourceV.id())) {
					return ConstantValue.of(true);
				}
				worklist.addAll(CrymlinQueryWrapper.getDFGSources(targetV));
			}
		}
		catch (InvalidArgumentException e) {
			log.warn(e.getMessage());
			return ErrorValue.newErrorValue(e.getMessage() + " in _receives_value_from");
		}
		return ErrorValue.newErrorValue("Error in _receives_value_from");
	}
}
