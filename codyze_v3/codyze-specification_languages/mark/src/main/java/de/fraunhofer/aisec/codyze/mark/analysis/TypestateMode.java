
package de.fraunhofer.aisec.codyze.mark.analysis;

public enum TypestateMode {

	/**
	 * Non-deterministic finite automaton. Intraprocedural, not alias-aware.
	 */
	DFA,

	/**
	 * Weighted Pushdown System. Interprocedural, alias-aware, context-aware.
	 */
	WPDS

}
