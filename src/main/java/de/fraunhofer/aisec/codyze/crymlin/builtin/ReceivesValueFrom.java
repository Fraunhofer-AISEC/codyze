
package de.fraunhofer.aisec.codyze.crymlin.builtin;

import de.fraunhofer.aisec.codyze.analysis.markevaluation.ExpressionEvaluator;
import de.fraunhofer.aisec.codyze.analysis.structures.*;
import de.fraunhofer.aisec.cpg.graph.Node;
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
			@NonNull AnalysisContext ctx,
			@NonNull ListValue argResultList,
			@NonNull Integer contextID,
			@NonNull MarkContextHolder markContextHolder,
			ExpressionEvaluator expressionEvaluator) {
		try {
			// We expect a source and a target vertex
			var vertices = BuiltinHelper.extractResponsibleNodes(argResultList, 2);

			if (vertices.size() != 2) {
				return ErrorValue.newErrorValue("Could not resolve arguments of _receives_value_from");
			}

			var targetV = vertices.get(0);
			var sourceV = vertices.get(1);

			// Follow DFG edges backwards, avoiding loops
			Set<Node> seen = new HashSet<>();
			Deque<Node> worklist = new ArrayDeque<>(targetV.getPrevDFG());

			while (!worklist.isEmpty()) {
				var currentV = worklist.pop();
				if (seen.contains(currentV)) {
					continue;
				}

				seen.add(currentV);
				if (currentV == sourceV) {
					return ConstantValue.of(true);
				}

				worklist.addAll(targetV.getPrevDFG());
			}
		}
		catch (InvalidArgumentException e) {
			log.warn(e.getMessage());
			return ErrorValue.newErrorValue(e.getMessage() + " in _receives_value_from");
		}

		return ErrorValue.newErrorValue("Error in _receives_value_from");
	}
}
