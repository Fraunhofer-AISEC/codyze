
package de.fraunhofer.aisec.crymlin.server;

import de.breakpoint.pushdown.WPDS;
import de.fraunhofer.aisec.markmodel.wpds.NFA;

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
