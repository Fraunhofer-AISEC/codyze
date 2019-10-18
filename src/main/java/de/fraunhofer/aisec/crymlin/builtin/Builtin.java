
package de.fraunhofer.aisec.crymlin.builtin;

import de.fraunhofer.aisec.mark.markDsl.Literal;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;
import java.util.Optional;

/**
 * All built-in functions must implement this interface.
 */
public interface Builtin {

	@NonNull
	public String getName();

	@NonNull
	public Optional<Literal> execute(List<Optional> arguments); // TODO argument list should not be <Optional>
}
