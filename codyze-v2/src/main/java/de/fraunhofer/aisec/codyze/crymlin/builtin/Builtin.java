
package de.fraunhofer.aisec.codyze.crymlin.builtin;

import de.fraunhofer.aisec.codyze.analysis.MarkIntermediateResult;
import de.fraunhofer.aisec.codyze.analysis.markevaluation.ExpressionEvaluator;
import de.fraunhofer.aisec.codyze.analysis.AnalysisContext;
import de.fraunhofer.aisec.codyze.analysis.ListValue;
import de.fraunhofer.aisec.codyze.analysis.MarkContextHolder;
import org.checkerframework.checker.nullness.qual.NonNull;

/** All built-in functions must implement this interface. */
public interface Builtin {

	/**
	 * Unique name of this Builtin.
	 *
	 * <p>
	 * By convention, Builtin names should start with underscore (_) to distinguish them from native MARK grammer. This is however not enforced. Developers of new
	 * Builtins are responsible for making sure the name is unique.
	 *
	 * @return
	 */
	@NonNull
	String getName();

	default boolean hasParameters() {
		return true;
	}

	/**
	 * Runs this Builtin.
	 *
	 * Builtin needs to respect
	 *
	 * @param ctx The analysis context
	 * @param argResultList Resolved argumentsList for one context of the Builtin function call.
	 * @param contextID
	 * @param markContextHolder
	 * @param expressionEvaluator the expressionEvaluator, this builtin is called from
	 * @return
	 */
	MarkIntermediateResult execute(
			@NonNull AnalysisContext ctx,
			@NonNull ListValue argResultList,
			@NonNull Integer contextID,
			@NonNull MarkContextHolder markContextHolder,
			ExpressionEvaluator expressionEvaluator);
}
