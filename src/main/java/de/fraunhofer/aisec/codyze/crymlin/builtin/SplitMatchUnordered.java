
package de.fraunhofer.aisec.codyze.crymlin.builtin;

import de.fraunhofer.aisec.codyze.analysis.*;
import de.fraunhofer.aisec.codyze.analysis.markevaluation.ExpressionEvaluator;
import de.fraunhofer.aisec.codyze.analysis.markevaluation.ExpressionHelper;
import de.fraunhofer.aisec.codyze.analysis.resolution.ConstantValue;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

/**
 * Method signature: _split_match_unordered(String str, String splitter, List set, Boolean strict)
 * <p>
 * This Builtin splits the str and checks, if resulting elements are part of the provided set.
 * The optional parameter <code>strict</code> controls if the set resulting from splitting is identical
 * to the provided set.
 * <p>
 * In case of an error, this Builtin returns an ErrorValue;
 */
public class SplitMatchUnordered implements Builtin {
	private static final Logger log = LoggerFactory.getLogger(SplitMatchUnordered.class);

	@NonNull
	@Override
	public String getName() {
		return "_split_match_unordered";
	}

	@Override
	public MarkIntermediateResult execute(
			@NonNull AnalysisContext ctx,
			@NonNull ListValue argResultList,
			@NonNull Integer contextID,
			@NonNull MarkContextHolder markContextHolder,
			@NonNull ExpressionEvaluator expressionEvaluator) {

		// arguments: String, String, List
		// example:
		// _split_match_unordered("ASD/EFG/JKL", "/", ["ASD", "EFG", "JKL"], true) returns true
		String regex = "";

		try {
			if (argResultList.size() == 4) {
				BuiltinHelper.verifyArgumentTypesOrThrow(argResultList, ConstantValue.class, ConstantValue.class,
					ListValue.class, ConstantValue.class);
			} else {
				BuiltinHelper.verifyArgumentTypesOrThrow(argResultList, ConstantValue.class, ConstantValue.class, ListValue.class);
			}

			String s = ExpressionHelper.asString(argResultList.get(0));
			regex = ExpressionHelper.asString(argResultList.get(1));
			var listSet = new HashSet<>(((ListValue) argResultList.get(2)).getAll());

			var strict = argResultList.size() == 4 ? ExpressionHelper.asBoolean(argResultList.get(3)) : false;

			var providedSet = listSet.stream().map(mir -> ((ConstantValue) mir).getValue()).collect(Collectors.toSet());

			if (s == null || regex == null) {
				log.warn("One of the arguments for _split_match_unordered was not the expected type, or not initialized/resolved");
				return ErrorValue.newErrorValue("One of the arguments for _split_match_unordered was not the expected type, or not initialized/resolved",
					argResultList.getAll());
			}

			log.info("args are: {}; {}; {}; {}", s, regex, providedSet, strict);

			String[] splitted = s.split(regex);
			var values = Arrays.stream(splitted).map(String::strip).collect(Collectors.toSet());
			boolean isMatch = strict ? Objects.equals(values, providedSet) : providedSet.containsAll(values);

			ConstantValue cv = ConstantValue.of(isMatch);

			cv.addResponsibleNodesFrom((ConstantValue) argResultList.get(0),
				(ConstantValue) argResultList.get(1));

			return cv;
		}
		catch (PatternSyntaxException e) {
			log.warn("Pattern for _split_match_unordered wrong: '{}': {}", regex, e.getMessage());
			return ErrorValue.newErrorValue(String.format("Pattern for _split_match_unordered wrong: '%s': %s", regex, e.getMessage()), argResultList.getAll());
		}
		catch (InvalidArgumentException e) {
			log.warn(e.getMessage());
			return ErrorValue.newErrorValue(e.getMessage(), argResultList.getAll());
		}
	}
}
