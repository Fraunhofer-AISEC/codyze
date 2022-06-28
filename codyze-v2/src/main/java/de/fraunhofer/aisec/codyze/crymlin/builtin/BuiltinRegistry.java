
package de.fraunhofer.aisec.codyze.crymlin.builtin;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.HashSet;
import java.util.Set;

/**
 * All MARK built-in functions that shall be supported by the evaluation must be registered here.
 */
public class BuiltinRegistry {

	private static BuiltinRegistry instance = null;
	private final Set<Builtin> builtins = new HashSet<>();

	private BuiltinRegistry() {
		/* do not instantiate */ }

	@NonNull
	public static BuiltinRegistry getInstance() {
		if (instance == null) {
			instance = new BuiltinRegistry();
		}
		return instance;
	}

	public void register(Builtin builtin) {
		this.builtins.add(builtin);
	}

	public void unregister(Builtin builtin) {
		this.builtins.remove(builtin);
	}

	public Set<Builtin> getRegisteredBuiltins() {
		return Set.copyOf(this.builtins);
	}
}
