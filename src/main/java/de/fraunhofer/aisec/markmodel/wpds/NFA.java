
package de.fraunhofer.aisec.markmodel.wpds;

import de.fraunhofer.aisec.mark.markDsl.Expression;
import de.fraunhofer.aisec.mark.markDsl.RepetitionExpression;
import de.fraunhofer.aisec.mark.markDsl.SequenceExpression;
import de.fraunhofer.aisec.mark.markDsl.Terminal;
import de.fraunhofer.aisec.markmodel.fsm.Node;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/** A non-deterministic finite automaton. Shameless plug from Dennis' FSM class. */
public class NFA {
	private static final Logger log = LoggerFactory.getLogger(NFA.class);
	private HashSet<Node> startNodes = null;

	final private Node START = new Node("START", "START");

	/* Set of transitions between states */
	private Set<NFATransition<Node>> transitions = new HashSet<>();

	/* The set of states with tokens. */
	private Set<Node> currentConfiguration = new HashSet<>();

	/**
	 * Factory method to create a new NFA from a Mark "order" expression (effectively a regular expression).
	 *
	 * @param seq
	 * @return
	 */
	public static NFA of(final Expression seq) {
		NFA nfa = new NFA();
		nfa.sequenceToFSM(seq);
		return nfa;
	}

	/**
	 * Returns a deep copy of the set of NFA transitions.
	 *
	 * @return
	 */
	public Set<NFATransition<Node>> getTransitions() {
		return Set.copyOf(this.transitions);
	}

	public boolean handleEvent(String event) {
		boolean didTransition = false;
		Iterator<Node> it = currentConfiguration.iterator();
		while (it.hasNext()) {
			Node currentConfig = it.next();
			List<Node> possibleTargets = this.transitions.stream()
					.filter(t -> t.getSource().equals(currentConfig) && t.getLabel().equals(event))
					.map(
						t -> t.getTarget())
					.collect(Collectors.toList());
			if (!possibleTargets.isEmpty()) {
				it.remove();
				currentConfiguration.addAll(possibleTargets);
				didTransition = true;
			}
		}

		return didTransition;
	}

	/**
	 * Order-Statement to FSM
	 *
	 * <p>
	 * Possible classes of the order construct: Terminal SequenceExpression RepetitionExpression (mit ?, *, +)
	 *
	 * <p>
	 * Start with a "empty" FSM with only StartNode and EndNode
	 *
	 * <p>
	 * prevPointer = [&StartNode]
	 *
	 * <p>
	 * For each Terminal Add node, connect each last node (= each Node in prevPointer) to the current node, return current node as only new prevPointer
	 *
	 * <p>
	 * For each Exp in SequenceExpression: call algo recursively, update (=overwrite) prevPointer-List after each algo-call
	 *
	 * <p>
	 * For RepetitionExpression For + algo(inner) use * - part below once For ? algo(inner) the resulting prevPointer-List needs to be added to the outer prevPointer List
	 * For * algo(inner), BUT: the last node of the inner construct needs to point to the first node of the inner construct the resulting prevPointer-List needs to be
	 * added to the outer prevPointer List
	 */
	private void sequenceToFSM(final Expression seq) {
		Node start = new Node(null, "BEGIN");
		start.setStart(true);

		HashSet<Node> currentNodes = new HashSet<>();
		currentNodes.add(start);
		addExpr(seq, currentNodes);

		// not strictly needed, we could simply set end=true for all the returned nodes
		Node end = new Node(null, "END");
		end.setEnd(true);
		end.setFake(true);
		currentNodes.forEach(x -> x.addSuccessor(end));

		// we could remove BEGIN here, and set begin=true for its successors
		for (Node n : start.getSuccessors()) {
			n.setStart(true);
		}
		startNodes = start.getSuccessors();

		// make transitions explicit
		populateTransitions();

		// Create transitions from artificial START state into start nodes
		for (Node startNode : startNodes) {
			NFATransition initialTransition = new NFATransition(START, startNode, startNode.getOp());
			this.transitions.add(initialTransition);
		}
		// Set NFA to START state
		this.currentConfiguration.add(START);
	}

	/**
	 * Initially, the NFA consists only of states (=Nodes), connected by "successor" edges. We make these transitions explicit in a "transitions" set.
	 */
	private void populateTransitions() {
		HashSet<Node> seen = new HashSet<>();
		ArrayList<Node> current = new ArrayList<>(startNodes);
		HashMap<Node, Integer> nodeToId = new HashMap<>();
		int node_counter = 0;
		while (!current.isEmpty()) {
			ArrayList<Node> newWork = new ArrayList<>();
			for (Node n : current) {
				if (!seen.contains(n)) {
					Integer id = nodeToId.get(n);
					if (id == null) {
						id = node_counter++;
						nodeToId.put(n, id);
					}
					TreeMap<String, Node> sorted = new TreeMap<>();
					for (Node s : n.getSuccessors()) {
						sorted.put(s.getName(), s);
					}
					for (Map.Entry<String, Node> entry : sorted.entrySet()) {
						Node s = entry.getValue();
						Integer id_succ = nodeToId.get(s);
						if (id_succ == null) {
							id_succ = node_counter++;
							nodeToId.put(s, id_succ);
						}
						if (!seen.contains(s)) {
							newWork.add(s);
						}
						transitions.add(new NFATransition(n, s, n.getOp()));
					}
					seen.add(n);
				}
			}
			current = newWork;
		}
	}

	@Nullable
	private HashSet<Node> addExpr(final Expression expr, final HashSet<Node> currentNodes) {
		if (expr instanceof Terminal) {
			Terminal inner = (Terminal) expr;
			Node n = new Node(inner.getEntity(), inner.getOp());
			currentNodes.forEach(x -> x.addSuccessor(n));
			currentNodes.clear();
			currentNodes.add(n);
			return currentNodes;
		} else if (expr instanceof SequenceExpression) {
			SequenceExpression inner = (SequenceExpression) expr;
			addExpr(inner.getLeft(), currentNodes);
			return addExpr(inner.getRight(), currentNodes);
		} else if (expr instanceof RepetitionExpression) {
			RepetitionExpression inner = (RepetitionExpression) expr;
			switch (inner.getOp()) {
				case "?": {
					HashSet<Node> remember = new HashSet<>(currentNodes);
					addExpr(inner.getExpr(), currentNodes);
					currentNodes.addAll(remember);
					return currentNodes;
				}
				case "+": {
					HashSet<Node> remember = new HashSet<>(currentNodes);
					addExpr(inner.getExpr(), currentNodes);
					for (Node n : remember) {
						currentNodes.forEach(x -> x.addSuccessor(n.getSuccessors()));
					}
					return currentNodes;
				}
				case "*": {
					HashSet<Node> remember = new HashSet<>(currentNodes);
					addExpr(inner.getExpr(), currentNodes);
					for (Node n : remember) {
						currentNodes.forEach(x -> x.addSuccessor(n.getSuccessors()));
					}
					currentNodes.addAll(remember);
					return currentNodes;
				}
				default:
					log.error("UNKNOWN OP: {}", inner.getOp());
					return addExpr(inner.getExpr(), currentNodes);
			}
		}

		log.error("ERROR, unknown Expression: {}", expr.getClass());
		return null;
	}

	/**
	 * Returns the (aritifial) START state.
	 *
	 * @return
	 */
	public Node getStart() {
		return this.START;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (NFATransition t : transitions) {
			sb.append("\t");
			sb.append(t.toString());
			sb.append("\n");
		}
		return sb.toString();
	}

	public Set<Node> getCurrentConfiguration() {
		return currentConfiguration;
	}

	/**
	 * Returns all transitions leading from the START state to the first <i>real/i> states.
	 *
	 * @return
	 */
	public Set<NFATransition> getInitialTransitions() {
		return getTransitions().stream().filter(tr -> tr.getSource().equals(START)).collect(Collectors.toSet());
	}

}
