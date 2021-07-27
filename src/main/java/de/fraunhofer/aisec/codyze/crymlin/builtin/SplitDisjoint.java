
package de.fraunhofer.aisec.codyze.crymlin.builtin;

import de.fraunhofer.aisec.codyze.analysis.*;
import de.fraunhofer.aisec.codyze.analysis.markevaluation.ExpressionEvaluator;
import de.fraunhofer.aisec.codyze.analysis.markevaluation.ExpressionHelper;
import de.fraunhofer.aisec.codyze.analysis.resolution.ConstantValue;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

/**
 * Method signature: _split_disjoint(String str, String splitter, List set)
 * <p>
 * This Builtin splits the str and checks, if any of the resulting elements is part of the provided set.
 * <p>
 * In case of an error, this Builtin returns an ErrorValue;
 */
public class SplitDisjoint implements Builtin {
	private static final Logger log = LoggerFactory.getLogger(SplitDisjoint.class);

	@NonNull
	@Override
	public String getName() {
		return "_split_disjoint";
	}

	@Override
	public MarkIntermediateResult execute(
			@NonNull AnalysisContext ctx,
			@NonNull ListValue argResultList,
			@NonNull Integer contextID,
			@NonNull MarkContextHolder markContextHolder,
			ExpressionEvaluator expressionEvaluator) {

		// arguments: String, String, List
		// example:
		// _split_disjoint("ASD/EFG/JKL", "/", ["ABC", "EFG", "JKL"]) returns true
		String regex = "";

		try {
			BuiltinHelper.verifyArgumentTypesOrThrow(argResultList, ConstantValue.class, ConstantValue.class, ListValue.class);

			String s = ExpressionHelper.asString(argResultList.get(0));
			regex = ExpressionHelper.asString(argResultList.get(1));
			var listSet = new HashSet<>(((ListValue) argResultList.get(2)).getAll());

			var providedSet = listSet.stream().map(mir -> ((ConstantValue) mir).getValue()).collect(Collectors.toSet());

			if (s == null || regex == null) {
				log.warn("One of the arguments for _split_disjoint was not the expected type, or not initialized/resolved");
				return ErrorValue.newErrorValue("One of the arguments for _split_disjoint was not the expected type, or not initialized/resolved",
					argResultList.getAll());
			}

			log.info("args are: {}; {}; {}", s, regex, providedSet);

			String[] splitted = s.split(regex);
			var values = Arrays.stream(splitted).map(String::strip).collect(Collectors.toSet());
			boolean isDisjoint = Collections.disjoint(values, providedSet);

			ConstantValue cv = ConstantValue.of(isDisjoint);

			cv.addResponsibleNodesFrom((ConstantValue) argResultList.get(0),
				(ConstantValue) argResultList.get(1));

			return cv;
		}
		catch (PatternSyntaxException e) {
			log.warn("Pattern for _split_disjoint wrong: '{}': {}", regex, e.getMessage());
			return ErrorValue.newErrorValue(String.format("Pattern for _split_disjoint wrong: '%s': %s", regex, e.getMessage()), argResultList.getAll());
		}
		catch (InvalidArgumentException e) {
			log.warn(e.getMessage());
			return ErrorValue.newErrorValue(e.getMessage(), argResultList.getAll());
		}
	}
}
