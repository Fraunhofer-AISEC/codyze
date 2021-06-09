
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
 * Method signature: _split_match(String str, String splitter, List set)
 * <p>
 * This Builtin splits the str and checks, if all the resulting set of elements matches the provided set.
 * <p>
 * In case of an error, this Builtin returns an ErrorValue;
 */
public class SplitMatch implements Builtin {
	private static final Logger log = LoggerFactory.getLogger(SplitMatch.class);

	@NonNull
	@Override
	public String getName() {
		return "_split_match";
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
		// _split_match("ASD/EFG/JKL", "/", ["ASD", "EFG", "JKL"]) returns true
		String regex = "";

		try {
			BuiltinHelper.verifyArgumentTypesOrThrow(argResultList, ConstantValue.class, ConstantValue.class, ListValue.class);

			String s = ExpressionHelper.asString(argResultList.get(0));
			regex = ExpressionHelper.asString(argResultList.get(1));
			var listSet = new HashSet<>(((ListValue) argResultList.get(2)).getAll());

			var whitelist = listSet.stream().map(mir -> ((ConstantValue) mir).getValue()).collect(Collectors.toSet());

			if (s == null || regex == null || whitelist == null) {
				log.warn("One of the arguments for _split_match was not the expected type, or not initialized/resolved");
				return ErrorValue.newErrorValue("One of the arguments for _split_match was not the expected type, or not initialized/resolved", argResultList.getAll());
			}

			log.info("args are: {}; {}; {}", s, regex, whitelist);

			String[] splitted = s.split(regex);
			var values = Set.of(splitted);
			boolean isMatch = whitelist.containsAll(values) && values.containsAll(whitelist);

			ConstantValue cv = ConstantValue.of(isMatch);

			cv.addResponsibleVerticesFrom((ConstantValue) argResultList.get(0),
				(ConstantValue) argResultList.get(1));

			return cv;
		}
		catch (PatternSyntaxException e) {
			log.warn("Pattern for _split_match wrong: '{}': {}", regex, e.getMessage());
			return ErrorValue.newErrorValue(String.format("Pattern for _split_match wrong: '%s': %s", regex, e.getMessage()), argResultList.getAll());
		}
		catch (InvalidArgumentException e) {
			log.warn(e.getMessage());
			return ErrorValue.newErrorValue(e.getMessage(), argResultList.getAll());
		}
	}
}
