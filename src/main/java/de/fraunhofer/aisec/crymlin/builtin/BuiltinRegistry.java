
package de.fraunhofer.aisec.crymlin.builtin;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.HashSet;
import java.util.Set;

public class BuiltinRegistry {

	private static BuiltinRegistry INSTANCE = null;
	private final Set<Builtin> builtins = new HashSet<>();

	private BuiltinRegistry() {
		/* do not instantiate */ }

	@NonNull
	public static BuiltinRegistry getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new BuiltinRegistry();
		}
		return INSTANCE;
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
