
package specification_languages.mark.analysis.wpds;

import de.breakpointsec.pushdown.WPDS;
import de.breakpointsec.pushdown.fsm.WeightedAutomaton;
import de.fraunhofer.aisec.cpg.graph.Node;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Functional interface for creation of initial WPDS configurations.
 */
public interface IInitialConfig {
	WeightedAutomaton<Node, Val, TypestateWeight> create(@NonNull WPDS<Node, Val, @NonNull TypestateWeight> wpds);
}
