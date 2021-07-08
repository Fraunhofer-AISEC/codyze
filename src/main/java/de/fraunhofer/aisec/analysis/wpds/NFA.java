
package de.fraunhofer.aisec.analysis.wpds;

import de.fraunhofer.aisec.mark.markDsl.AlternativeExpression;
import de.fraunhofer.aisec.mark.markDsl.Expression;
import de.fraunhofer.aisec.mark.markDsl.RepetitionExpression;
import de.fraunhofer.aisec.mark.markDsl.SequenceExpression;
import de.fraunhofer.aisec.mark.markDsl.Terminal;
import de.fraunhofer.aisec.markmodel.fsm.Node;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** A non-deterministic finite automaton. Shameless plug from Dennis' FSM class. */
public class NFA {
	private static final Logger log = LoggerFactory.getLogger(NFA.class);
	private Set<Node> startNodes = null;

	private final Node START = new Node("START", "START");
	public static final Node ERROR;
	static {
		ERROR = new Node("ERROR", "ERROR");
		ERROR.setError(true);
	}

	/* Set of transitions between states */
	private final Set<NFATransition<Node>> transitions = new HashSet<>();

	/* The set of states with tokens. */
	private final Set<Node> currentConfiguration = new HashSet<>();

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

	public void clear() {
		this.transitions.clear();
	}

	public void addTransition(NFATransition<Node> t) {
		this.transitions.add(t);
	}

	public boolean handleEvent(NFATransition<Node> event) {
		boolean didTransition = false;
		Iterator<Node> it = currentConfiguration.iterator();
		while (it.hasNext()) {
			Node currentConfig = it.next();
			List<Node> possibleTargets = this.transitions.stream()
					.filter(t -> t.getSource().equals(currentConfig) && t.getSource().equals(event.getSource()) && t.getLabel().equals(event.getLabel()))
					.map(
						NFATransition::getTarget)
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
	 * Possible classes of the order construct: Terminal SequenceExpression RepetitionExpression (with ?, *, +)
	 *
	 * <p>
	 * Start with an "empty" FSM with only StartNode and EndNode
	 *
	 * <p>
	 * prevPointer = [&StartNode]
	 *
	 * <p>
	 * For each Terminal add node, connect each last node (= each Node in prevPointer) to the current node, return current node as only new prevPointer
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
		Node start = new Node("START", "START");
		start.setStart(true);

		Set<Node> currentNodes = new HashSet<>();
		currentNodes.add(start);
		addExpr(seq, currentNodes, null);

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

		// Mark END state as "isEnd()"
		transitions.stream().filter(t -> t.getLabel() != null && t.getLabel().equals("END") && t.getSource() != null).forEach(t -> t.getSource().setEnd(true));

		// Create transitions from artificial START state into start nodes
		for (Node startNode : startNodes) {
			NFATransition<Node> initialTransition = new NFATransition<>(START, startNode, startNode.getOp());
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
		int nodeCounter = 0;
		while (!current.isEmpty()) {
			ArrayList<Node> newWork = new ArrayList<>();
			for (Node n : current) {
				if (!seen.contains(n)) {
					Integer id = nodeToId.get(n);
					if (id == null) {
						id = nodeCounter++;
						nodeToId.put(n, id);
					}

					// Get all successor nodes of n and sort them
					List<Node> sortedSuccessors = n.getSuccessors()
							.stream()
							.sorted(Comparator.comparing(Node::getName))
							.collect(Collectors.toList());

					for (Node s : sortedSuccessors) {
						Integer idSucc = nodeToId.get(s);
						if (idSucc == null) {
							idSucc = nodeCounter++;
							nodeToId.put(s, idSucc);
						}

						// Any successor which has not be handled before gets added to work list.
						if (!seen.contains(s)) {
							newWork.add(s);
						}
						transitions.add(new NFATransition<>(n, s, s.getOp()));
					}
					seen.add(n);
				}
			}
			current = newWork;
		}
	}

	@Nullable
	private Set<Node> addExpr(final Expression expr, final Set<Node> currentNodes, Head head) {
		if (expr instanceof Terminal) {
			Terminal inner = (Terminal) expr;
			Node n = new Node(inner.getEntity(), inner.getOp());
			currentNodes.forEach(x -> x.addSuccessor(n));
			currentNodes.clear();
			currentNodes.add(n);
			if (head != null && head.addNextNode) {
				head.add(n);
				head.addNextNode = false;
			}
			return currentNodes;
		} else if (expr instanceof SequenceExpression) {
			SequenceExpression inner = (SequenceExpression) expr;
			addExpr(inner.getLeft(), currentNodes, head);
			return addExpr(inner.getRight(), currentNodes, head);
		} else if (expr instanceof RepetitionExpression) {
			RepetitionExpression inner = (RepetitionExpression) expr;
			String op = inner.getOp();
			if ("?".equals(op)) {
				HashSet<Node> remember = new HashSet<>(currentNodes);
				addExpr(inner.getExpr(), currentNodes, head);
				currentNodes.addAll(remember);
				return currentNodes;
			} else if ("+".equals(op)) {
				Head innerHead = new Head();
				innerHead.addNextNode = true;
				addExpr(inner.getExpr(), currentNodes, innerHead);
				for (Node j : innerHead.get()) {
					currentNodes.forEach(x -> x.addSuccessor(j));
					if (head != null && head.addNextNode) {
						head.add(j);
					}
				}
				return currentNodes;
			} else if ("*".equals(op)) {
				HashSet<Node> remember = new HashSet<>(currentNodes);
				Head innerHead = new Head();
				innerHead.addNextNode = true;
				addExpr(inner.getExpr(), currentNodes, innerHead);
				for (Node j : innerHead.get()) {
					currentNodes.forEach(x -> x.addSuccessor(j));
					if (head != null && head.addNextNode) {
						head.add(j);
					}
				}
				currentNodes.addAll(remember);
				return currentNodes;
			}
			log.error("UNKNOWN OP: {}", inner.getOp());
			return addExpr(inner.getExpr(), currentNodes, head);

		} else if (expr instanceof AlternativeExpression) {
			AlternativeExpression inner = (AlternativeExpression) expr;
			Set<Node> remember = new HashSet<>(currentNodes);
			addExpr(inner.getLeft(), currentNodes, head);
			if (head != null) {
				head.addNextNode = true;
			}
			addExpr(inner.getRight(), remember, head);
			currentNodes.addAll(remember);
			return currentNodes;
		}

		log.error("ERROR, unknown Expression: {}", expr.getClass());
		return new HashSet<>();
	}

	/**
	 * Returns the (artificial) START state.
	 *
	 * @return
	 */
	public Node getStart() {
		return START;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (NFATransition<Node> t : transitions) {
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
	public Set<NFATransition<Node>> getInitialTransitions() {
		return getTransitions().stream().filter(tr -> tr.getSource().equals(START)).collect(Collectors.toSet());
	}

	private static class Head {
		private final ArrayList<Node> nodes = new ArrayList<>();
		private Boolean addNextNode = null;

		void add(Node n) {
			nodes.add(n);
		}

		ArrayList<Node> get() {
			return nodes;
		}
	}
}
