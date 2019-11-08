
package de.fraunhofer.aisec.crymlin.builtin;

import de.fraunhofer.aisec.analysis.scp.ConstantValue;
import de.fraunhofer.aisec.mark.markDsl.Argument;
import de.fraunhofer.aisec.markmodel.EvaluationContext;
import de.fraunhofer.aisec.markmodel.ExpressionEvaluator;
import de.fraunhofer.aisec.markmodel.ExpressionHelper;
import jnr.constants.Constant;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.emf.common.util.EList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.text.html.Option;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Method signature: _split(String str, String splitter, int position)
 *
 * <p>
 * This Builtin behaves like str.split(splitter)[position].
 *
 * <p>
 * In case of an error or an empty result, this Builtin returns an Optional.empty.
 */
public class SplitBuiltin implements Builtin {
	private static final Logger log = LoggerFactory.getLogger(ExpressionEvaluator.class);

	@NonNull
	@Override
	public String getName() {
		return "_split";
	}

	@Override
	@NonNull // TODO Should return Optional<Literal>. This must be changed consistently across all evaluation functions, however.
	public Optional execute(List<Argument> arguments, @Nullable EvaluationContext evalCtx) {
		if (evalCtx == null) {
			return Optional.empty();
		}

		List<Optional> argOptionals = new ExpressionEvaluator(evalCtx).evaluateArgs(arguments);

		// arguments: String, String, int
		// example:
		// _split("ASD/EFG/JKL", "/", 1) returns "EFG"

		String s = ExpressionHelper.asString(argOptionals.get(0));
		String regex = ExpressionHelper.asString(argOptionals.get(1));
		Number index = ExpressionHelper.asNumber(argOptionals.get(2));

		if (s == null || regex == null || index == null) {
			return Optional.empty();
		}

		log.debug("args are: " + s + "; " + regex + "; " + index);
		// TODO #8
		String ret = null;
		String[] splitted = s.split(regex);
		if (index.intValue() < splitted.length) {
			ret = splitted[index.intValue()];
		}

		if (ret != null) {
			// StringLiteral stringResult = new MarkDslFactoryImpl().createStringLiteral();
			// stringResult.setValue(ret);
			return Optional.of(ret);
		} else {
			return Optional.empty();
		}
	}
}
