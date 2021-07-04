
package de.fraunhofer.aisec.crymlin.builtin;

import de.fraunhofer.aisec.analysis.markevaluation.ExpressionEvaluator;
import de.fraunhofer.aisec.analysis.structures.*;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This builtin checks if the markvar has a cpg-node. The markvar does not nessesarily have a value associated (i.e., the Constant Resolving could have failed). In this case, this builtin will still return true.
 */
public class HasValue implements Builtin {
	private static final Logger log = LoggerFactory.getLogger(HasValue.class);

	@Override
	public @NonNull String getName() {
		return "_has_value";
	}

	@Override
	public ConstantValue execute(
			@NonNull AnalysisContext ctx,
			@NonNull ListValue argResultList,
			@NonNull Integer contextID,
			@NonNull MarkContextHolder markContextHolder,
			ExpressionEvaluator expressionEvaluator) {

		if (argResultList.size() != 1) {
			log.warn("Invalid number of arguments: {}", argResultList.size());
			return ErrorValue.newErrorValue("Invalid number of arguments: {}" + argResultList.size(), argResultList.getAll());
		}

		if (!(argResultList.get(0) instanceof ConstantValue)) {
			log.warn("Argument %s is not a ConstantValue");
			return ErrorValue.newErrorValue("Argument %s is not a ConstantValue", argResultList.getAll());
		}

		var collect = ((ConstantValue) argResultList.get(0)).getResponsibleNodes().stream().filter(Objects::nonNull).collect(Collectors.toSet());

		ConstantValue ret = ConstantValue.of(!collect.isEmpty());
		ret.addResponsibleNodes(collect);
		return ret;

	}
}
