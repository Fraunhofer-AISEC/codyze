
package de.fraunhofer.aisec.codyze.analysis.wpds;

import de.breakpointsec.pushdown.WPDS;
import de.breakpointsec.pushdown.fsm.Transition;
import de.breakpointsec.pushdown.fsm.WeightedAutomaton;
import de.breakpointsec.pushdown.rules.Rule;
import de.fraunhofer.aisec.cpg.graph.Node;
import de.fraunhofer.aisec.codyze.markmodel.fsm.StateNode;
import kotlin.Pair;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * Different strategies to create initial WPDS configurations.
 *
 * A WPDS configuration is a set of Stmt/Val pairs, i.e. code locations (stmt) and a variable (Val) that is on top of thw WPDS stack.
 *
 * An initial configuration is a configuration that indicates a state of the program from which the data flow analysis starts,
 * i.e. it seeds the P-automaton for creation of the saturated pre-* or post-* automatons.
 *
 * There are different strategies to create initial configurations, depending on type and precision of the analysis.
 *
 * For instance:
 *
 * - All entrypoints of the program.
 * This is a "precise" initial configuration that corresponds to the actual execution of the program. However, it requires a precise analysis of entrypoints and if the relevant data flow is far away from an actual program entrypoint, this strategy will result in too large and possibly incorrect automatons.
 *
 * - Function arguments and first line in a function
 * This initial configuration ignores all call-site contexts before the given function but is precise within that function (and all its callees).
 */
public class InitialConfiguration {
	private static final Logger log = LoggerFactory.getLogger(InitialConfiguration.class);
	private static final String ACCEPT = "ACCEPT";
	private static final String START = "START";

	private InitialConfiguration() {
		// Do not instantiate.
	}

	public static WeightedAutomaton<Node, Val, TypestateWeight> create(IInitialConfig creator, WPDS<Node, Val, TypestateWeight> wpds) {
		return creator.create(wpds);
	}

	/**
	 * Create initial WPDS configuration corresponding to the first statement that creates a typestate transition.
	 *
	 * @param wpds
	 * @return
	 */
	@java.lang.SuppressWarnings({ "squid:S100", "squid:S1905" })
	static WeightedAutomaton<Node, Val, TypestateWeight> FIRST_TYPESTATE_EVENT(@NonNull WPDS<Node, Val, TypestateWeight> wpds) {
		// Get all WPDS rules have a type state transition originating in a START state.
		var startRules = getTypestateStartRules(wpds);

		// Collect initial configurations from the start rules.
		/* TODO limit to the Var that is assigned the typestate.
			For non-OO languages the TS changes even if the operation does not affect the typestate object.
			For instance, the following rule could indicate a TS transition:
			<State: dec_length in (aes256_decrypt); Location: 70:5 decrypted_message = message_init(*encrypted_message -> length);> --> <State: dec_length in (aes256_decrypt); Location: 71:15 EVP_CIPHER_CTX_new()>(START.START -- [create] --> ctx.create)
		
			This is incorrect, as the Var in this rule is "dec_length" which is not the one that is assigned the typestate object. Correct would be "dec_ctx"
		 */
		var initialStates = new HashSet<Pair<Val, Node>>();
		for (var r : startRules) {
			Val initialState = r.getS1();
			var stmt = r.getL1();
			initialStates.add(new Pair<>(initialState, stmt));
		}

		if (initialStates.isEmpty()) {
			log.error("Did not find initial configuration for typestate analysis. Will fail soon.");
		}

		return createInitialWNFA(initialStates);
	}

	/**
	 * Create initial WPDS configuration corresponding to the statements declaring variables within a function.
	 *
	 * @param wpds
	 * @return
	 */
	@java.lang.SuppressWarnings({ "squid:S100", "squid:S1905" })
	static WeightedAutomaton<Node, Val, TypestateWeight> VAR_DECLARATIONS(@NonNull WPDS<Node, Val, TypestateWeight> wpds) {
		// Get START state from WPDS
		var initialStates = new HashSet<Pair<Val, Node>>();
		var normalRules = wpds.getNormalRules();
		for (var nr : normalRules) {
			if (nr.getS1()
					.getVariable()
					.equals(GraphWPDS.EPSILON_NAME)) {
				var initialState = nr.getS2();
				var stmt = nr.getL2();
				initialStates.add(new Pair<>(initialState, stmt));
			}
		}

		if (initialStates.isEmpty()) {
			log.error("Did not find initial configuration for typestate analysis. Will fail soon.");
		}

		return createInitialWNFA(initialStates);
	}

	private static WeightedAutomaton<Node, Val, TypestateWeight> createInitialWNFA(Set<Pair<Val, Node>> initialStates) {
		// Create statement for start configuration and create start CONFIG
		// TODO make initialState a set or remove completely
		int line = Integer.MAX_VALUE;
		Val initialState = null;
		Node stmt = null;
		for (var s : initialStates) {
			if (s.getSecond()
					.getLocation()
					.getRegion()
					.getStartLine() < line) {
				line = s.getSecond()
						.getLocation()
						.getRegion()
						.getStartLine();
				initialState = s.getFirst();
				stmt = s.getSecond();
			}
		}

		WeightedAutomaton<Node, Val, TypestateWeight> wnfa = new WeightedAutomaton<>(initialState) {
			@Override
			public Val createState(Val val, Node stmt) {
				return val;
			}

			@Override
			public boolean isGeneratedState(Val val) {
				return false;
			}

			@Override
			public Node epsilon() {
				return new GraphWPDS.EpsilonNode();
			}

			@Override
			public TypestateWeight getZero() {
				return TypestateWeight.zero();
			}

			@Override
			public TypestateWeight getOne() {
				return TypestateWeight.one();
			}
		};
		final Val accepting = new Val(ACCEPT, ACCEPT);
		// Create an automaton for the initial configuration from where post* will start.
		dumpInitialConfigurations(initialStates);

		if (initialState == null) {
			return wnfa;
		}
		wnfa.addTransition(new Transition<>(initialState, stmt, accepting),
			new TypestateWeight(Set.of(new NFATransition<StateNode>(new StateNode(START, START), new StateNode(START, START), "constructor"))));

		// Add final ("accepting") states to NFA.
		wnfa.addFinalState(accepting);

		return wnfa;
	}

	private static void dumpInitialConfigurations(Set<Pair<Val, Node>> initialStates) {
		log.debug("Initial configuration(s):");
		for (var initialConfig : initialStates) {
			log.debug("  {} in {} at {}", initialConfig.getFirst().getVariable(), initialConfig.getFirst().getCurrentScope(), initialConfig.getSecond());
		}
	}

	/**
	 * Returns all rules of the WPDS that have a type state transition starting in a START state.
	 *
	 * @param wpds
	 * @return
	 */
	private static Set<Rule<Node, Val, TypestateWeight>> getTypestateStartRules(WPDS<Node, Val, TypestateWeight> wpds) {
		var startRules = new HashSet<Rule<Node, Val, TypestateWeight>>();

		for (var rule : wpds.getAllRules()) {
			var tsWeight = rule.getWeight();

			if (tsWeight.value() instanceof Set) {
				Set<NFATransition<StateNode>> tsTransitions = (Set) tsWeight.value();
				for (NFATransition<StateNode> tsTransition : tsTransitions) {
					if (tsTransition.getSource()
							.toString()
							.equals("START.START")) {
						startRules.add(rule);
					}
				}
			}
		}

		return startRules;
	}

}
