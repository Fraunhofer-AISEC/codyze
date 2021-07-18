
package de.fraunhofer.aisec.codyze.markmodel.fsm;

import de.fraunhofer.aisec.mark.markDsl.AlternativeExpression;
import de.fraunhofer.aisec.mark.markDsl.Expression;
import de.fraunhofer.aisec.mark.markDsl.RepetitionExpression;
import de.fraunhofer.aisec.mark.markDsl.SequenceExpression;
import de.fraunhofer.aisec.mark.markDsl.Terminal;
import de.fraunhofer.aisec.mark.markDsl.impl.AlternativeExpressionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class FSM {

	private static final Logger log = LoggerFactory.getLogger(FSM.class);

	private Set<StateNode> startNodes = null;

	public String toString() {
		Set<StateNode> seen = new HashSet<>();
		ArrayList<StateNode> current = new ArrayList<>(startNodes);
		HashMap<StateNode, Integer> nodeToId = new HashMap<>();
		StringBuilder sb = new StringBuilder();
		int nodeCounter = 0;
		while (!current.isEmpty()) {
			ArrayList<StateNode> newWork = new ArrayList<>();
			for (StateNode n : current) {
				if (!seen.contains(n)) {
					Integer id = nodeToId.get(n);
					if (id == null) {
						id = nodeCounter++;
						nodeToId.put(n, id);
					}
					sb.append(n).append(" (").append(id).append(")\n");
					TreeMap<String, StateNode> sorted = new TreeMap<>();
					for (StateNode s : n.getSuccessors()) {
						sorted.put(s.getName(), s);
					}
					for (Map.Entry<String, StateNode> entry : sorted.entrySet()) {
						StateNode s = entry.getValue();
						Integer idSucc = nodeToId.get(s);
						if (idSucc == null) {
							idSucc = nodeCounter++;
							nodeToId.put(s, idSucc);
						}
						if (!seen.contains(s)) {
							newWork.add(s);
						}
						sb.append("\t-> ").append(s).append("(").append(idSucc).append(")").append("\n");
					}
					seen.add(n);
				}
			}
			current = newWork;
		}
		return sb.toString();
	}

	/**
	 * Order-Statement to FSM
	 *
	 * <p>
	 * Possible classes of the order construct: Terminal SequenceExpression RepetitionExpression (with ?, *, +)
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
	public void sequenceToFSM(final Expression seq) {
		StateNode start = new StateNode(null, "BEGIN");
		start.setStart(true);

		Set<StateNode> endNodes = new HashSet<>();
		endNodes.add(start);
		expressionToNodes(seq, endNodes, null);

		// not strictly needed, we could simply set end=true for all the returned nodes
		StateNode end = new StateNode(null, "END");
		end.setEnd(true);
		end.setFake(true);
		endNodes.forEach(x -> x.addSuccessor(end));

		// we could remove BEGIN here, and set begin=true for its successors
		for (StateNode n : start.getSuccessors()) {
			n.setStart(true);
		}
		startNodes = start.getSuccessors();
	}

	private void expressionToNodes(
			final Expression expr, final Set<StateNode> endNodes, final Head head) {
		if (expr instanceof Terminal) {
			Terminal inner = (Terminal) expr;
			StateNode n = new StateNode(inner.getEntity(), inner.getOp());
			endNodes.forEach(x -> x.addSuccessor(n));
			endNodes.clear();
			endNodes.add(n);
			if (head != null && head.addNextNode) {
				head.add(n);
				head.addNextNode = false;
			}
			return;
		} else if (expr instanceof SequenceExpression) {
			SequenceExpression inner = (SequenceExpression) expr;
			expressionToNodes(inner.getLeft(), endNodes, head);
			expressionToNodes(inner.getRight(), endNodes, head);
			return;
		} else if (expr instanceof RepetitionExpression) {
			RepetitionExpression inner = (RepetitionExpression) expr;
			String op = inner.getOp();
			if ("?".equals(op)) {
				Set<StateNode> remember = new HashSet<>(endNodes);
				expressionToNodes(inner.getExpr(), endNodes, head);
				endNodes.addAll(remember);
				return;
			} else if ("+".equals(op)) {
				Head innerHead = new Head();
				innerHead.addNextNode = true;
				expressionToNodes(inner.getExpr(), endNodes, innerHead);
				for (StateNode j : innerHead.get()) {
					// connect to innerHead
					endNodes.forEach(x -> x.addSuccessor(j));
					if (head != null && head.addNextNode) {
						head.add(j);
					}
				}
				return;
			} else if ("*".equals(op)) {
				Set<StateNode> remember = new HashSet<>(endNodes);
				Head innerHead = new Head();
				innerHead.addNextNode = true;
				expressionToNodes(inner.getExpr(), endNodes, innerHead);
				for (StateNode j : innerHead.get()) {
					// connect to innerHead
					endNodes.forEach(x -> x.addSuccessor(j));
					if (head != null && head.addNextNode) {
						head.add(j);
					}
				}

				endNodes.addAll(remember);
				return;
			}
			log.error("UNKNOWN OP: {}", inner.getOp());
			return;

		} else if (expr instanceof AlternativeExpressionImpl) {
			AlternativeExpression inner = (AlternativeExpression) expr;
			Set<StateNode> remember = new HashSet<>(endNodes);
			expressionToNodes(inner.getLeft(), endNodes, head);
			if (head != null) {
				head.addNextNode = true;
			}

			expressionToNodes(inner.getRight(), remember, head);
			endNodes.addAll(remember);
			return;
		}

		log.error("ERROR, unknown Expression: {}", expr.getClass());
	}

	public Set<StateNode> getStart() {
		return startNodes;
	}

	private static class Head {
		private final List<StateNode> nodes = new ArrayList<>();
		private Boolean addNextNode = null;

		void add(StateNode n) {
			nodes.add(n);
		}

		List<StateNode> get() {
			return nodes;
		}
	}
}
