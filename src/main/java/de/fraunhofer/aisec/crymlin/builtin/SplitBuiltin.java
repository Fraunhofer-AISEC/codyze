
package de.fraunhofer.aisec.crymlin.builtin;

import de.fraunhofer.aisec.markmodel.ExpressionEvaluator;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Method signature: _split(String str, String splitter, int position)
 *
 * This Builtin behaves like str.split(splitter)[position].
 *
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
	public Optional execute(List<Optional> argOptionals) {
		// arguments: String, String, int
		// example:
		// _split("ASD/EFG/JKL", "/", 1) returns "EFG"
		final List<Class> paramTypes = Arrays.asList(String.class, String.class, Integer.class);

		if (paramTypes.size() != argOptionals.size()) {
			return Optional.empty();
		}

		for (int i = 0; i < paramTypes.size(); i++) {
			Optional arg = argOptionals.get(i);

			if (arg.isEmpty()) {
				return Optional.empty();
			}
			if (!arg.get().getClass().equals(paramTypes.get(i))) {
				return Optional.empty();
			}
		}

		String s = (String) argOptionals.get(0).get();
		String regex = (String) argOptionals.get(1).get();
		int index = (Integer) argOptionals.get(2).get();
		log.debug("args are: " + s + "; " + regex + "; " + index);
		// TODO #8
		String ret = null;
		String[] splitted = s.split(regex);
		if (index < splitted.length) {
			ret = splitted[index];
		}

		if (ret != null) {
			//StringLiteral stringResult = new MarkDslFactoryImpl().createStringLiteral();
			//stringResult.setValue(ret);
			return Optional.of(ret);
		} else {
			return Optional.empty();
		}

	}
}
