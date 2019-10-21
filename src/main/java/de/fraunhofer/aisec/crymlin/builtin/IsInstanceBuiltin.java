
package de.fraunhofer.aisec.crymlin.builtin;

import de.fraunhofer.aisec.crymlin.utils.Utils;
import de.fraunhofer.aisec.mark.markDsl.Argument;
import de.fraunhofer.aisec.mark.markDsl.Expression;
import de.fraunhofer.aisec.mark.markDsl.Operand;
import de.fraunhofer.aisec.markmodel.EvaluationContext;
import de.fraunhofer.aisec.markmodel.ExpressionEvaluator;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.emf.common.util.EList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Method signature: _is_instance(var instance, String classname)
 *
 * <p>
 * This Builtin behaves like "var instanceof classname".
 *
 * <p>
 * In case of an error or an empty result, this Builtin returns an Optional.empty.
 */
public class IsInstanceBuiltin implements Builtin {
	private static final Logger log = LoggerFactory.getLogger(IsInstanceBuiltin.class);

	@Override
	public @NonNull String getName() {
		return "_is_instance";
	}

	@Override
	public @NonNull Optional execute(List<Argument> args, @Nullable EvaluationContext evalCtx) {
		if (evalCtx == null) {
			return Optional.empty();
		}

		// we need to get the node(s) corresponding to the 1st argument, and the string for the
		// second argument

		Optional classnameArgument = new ExpressionEvaluator(evalCtx).evaluateExpression((Expression) (args.get(1)));
		if (classnameArgument.isEmpty() || !(classnameArgument.get() instanceof String)) {
			log.error("var of is_instance is empty");
			return Optional.empty();
		}
		// unify type (Java/C/C++)
		String classname = Utils.unifyType((String) classnameArgument.get());

		// For operands, we try to find corresponding vertices in the graph and check their type
		List<Vertex> verticesForOperand = ExpressionEvaluator.getMatchingVertices((Operand) args.get(0), evalCtx);
		for (Vertex v : verticesForOperand) {
			String type = v.value("type");
			if (!type.equals(classname)) {
				log.info("type of cpp ({}) and mark ({}) do not match", type, classname);
				return Optional.of(false);
			} else {
				log.info("type of cpp ({}) and mark ({}) match", type, classname);
			}
		}

		return Optional.of(true);
	}
}
