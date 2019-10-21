
package de.fraunhofer.aisec.crymlin.builtin;

import de.fraunhofer.aisec.mark.markDsl.Argument;
import de.fraunhofer.aisec.markmodel.EvaluationContext;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.Optional;

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
	public String getName();

	/**
	 * Runs this Builtin.
	 *
	 * @param arguments Resolved arguments of the Builtin function call.
	 * @param evalCtx An (optional) EvaluationContext. Some Builtin function may need access to the current EvaluationContext and will produce an Optional.empty, if not
	 *        provided with a context.
	 * @return
	 */
	@NonNull
	public Optional execute(
			List<Argument> arguments,
			@Nullable EvaluationContext evalCtx); // TODO argument list should not be <Optional>
}
