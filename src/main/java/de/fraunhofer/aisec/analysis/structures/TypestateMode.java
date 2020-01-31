
package de.fraunhofer.aisec.analysis.structures;

public enum TypestateMode {

	/**
	 * Non-deterministic finite automaton. Intraprocedural, not alias-aware.
	 */
	NFA,

	/**
	 * Weighted Pushdown System. Interprocedural, alias-aware, context-aware.
	 */
	WPDS

}
