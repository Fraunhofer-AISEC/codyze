
package de.fraunhofer.aisec.crymlin.server;

public enum TYPESTATE_ANALYSIS {

	/**
	 * Non-deterministic finite automaton. Intraprocedural, not alias-aware.
	 */
	NFA,

	/**
	 * Weighted Pushdown System. Interprocedural, alias-aware, context-aware.
	 */
	WPDS

}