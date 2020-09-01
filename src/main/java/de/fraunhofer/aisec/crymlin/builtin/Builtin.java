
package de.fraunhofer.aisec.crymlin.builtin;

import de.fraunhofer.aisec.analysis.markevaluation.ExpressionEvaluator;
import de.fraunhofer.aisec.analysis.structures.AnalysisContext;
import de.fraunhofer.aisec.analysis.structures.ConstantValue;
import de.fraunhofer.aisec.analysis.structures.ListValue;
import de.fraunhofer.aisec.analysis.structures.MarkContextHolder;
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
	ConstantValue execute(
			@NonNull AnalysisContext ctx,
			@NonNull ListValue argResultList,
			@NonNull Integer contextID,
			@NonNull MarkContextHolder markContextHolder,
			@NonNull ExpressionEvaluator expressionEvaluator);
}
