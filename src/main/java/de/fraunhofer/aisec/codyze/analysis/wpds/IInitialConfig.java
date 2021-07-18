
package de.fraunhofer.aisec.codyze.analysis.wpds;

import de.breakpointsec.pushdown.WPDS;
import de.breakpointsec.pushdown.fsm.WeightedAutomaton;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Functional interface for creation of initial WPDS configurations.
 */
public interface IInitialConfig {
	WeightedAutomaton<Stmt, Val, TypestateWeight> create(@NonNull WPDS<Stmt, Val, @NonNull TypestateWeight> wpds);
}
