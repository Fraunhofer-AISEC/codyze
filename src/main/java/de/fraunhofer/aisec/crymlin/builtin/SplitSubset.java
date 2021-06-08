
package de.fraunhofer.aisec.crymlin.builtin;

import de.fraunhofer.aisec.analysis.markevaluation.ExpressionEvaluator;
import de.fraunhofer.aisec.analysis.markevaluation.ExpressionHelper;
import de.fraunhofer.aisec.analysis.structures.*;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

/**
 * Method signature: _split_subset(String str, String splitter, List superset)
 * <p>
 * This Builtin behaves like superset.containsAll(str.split(splitter)).
 * <p>
 * In case of an error, this Builtin returns an ErrorValue;
 */
public class SplitSubset implements Builtin {
	private static final Logger log = LoggerFactory.getLogger(SplitSubset.class);

	@NonNull
	@Override
	public String getName() {
		return "_split_subset";
	}

	@Override
	public ConstantValue execute(
			@NonNull AnalysisContext ctx,
			@NonNull ListValue argResultList,
			@NonNull Integer contextID,
			@NonNull MarkContextHolder markContextHolder,
			@NonNull ExpressionEvaluator expressionEvaluator) {

		// arguments: String, String, List
		// example:
		// _split_subset("ASD/EFG/JKL", "/", ["ASD", "EFG", "JKL", "MNO"]) returns true
		String regex = "";

		try {
			BuiltinHelper.verifyArgumentTypesOrThrow(argResultList, ConstantValue.class, ConstantValue.class, ListValue.class);

			String s = ExpressionHelper.asString(argResultList.get(0));
			regex = ExpressionHelper.asString(argResultList.get(1));
			var listSet = new HashSet<>(((ListValue) argResultList.get(2)).getAll());

			var whitelist = listSet.stream().map(mir -> ((ConstantValue) mir).getValue()).collect(Collectors.toSet());

			if (s == null || regex == null || whitelist == null) {
				log.warn("One of the arguments for _split_subset was not the expected type, or not initialized/resolved");
				return ErrorValue.newErrorValue("One of the arguments for _split_subset was not the expected type, or not initialized/resolved", argResultList.getAll());
			}

			log.info("args are: {}; {}; {}", s, regex, whitelist);

			String[] splitted = s.split(regex);
			var values = Set.of(splitted);
			boolean isWhitelisted = whitelist.containsAll(values);

			ConstantValue cv = ConstantValue.of(isWhitelisted);

			cv.addResponsibleVerticesFrom((ConstantValue) argResultList.get(0),
				(ConstantValue) argResultList.get(1));

			return cv;
		}
		catch (PatternSyntaxException e) {
			log.warn("Pattern for _split_subset wrong: '{}': {}", regex, e.getMessage());
			return ErrorValue.newErrorValue(String.format("Pattern for _split_subset wrong: '%s': %s", regex, e.getMessage()), argResultList.getAll());
		}
		catch (InvalidArgumentException e) {
			log.warn(e.getMessage());
			return ErrorValue.newErrorValue(e.getMessage(), argResultList.getAll());
		}
	}
}
