
package de.fraunhofer.aisec.markmodel.fsm;

import de.fraunhofer.aisec.mark.markDsl.*;
import de.fraunhofer.aisec.mark.markDsl.impl.AlternativeExpressionImpl;
import org.python.antlr.base.expr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class FSM {

	private static final Logger log = LoggerFactory.getLogger(FSM.class);

	private Set<Node> startNodes = null;

	public String toString() {
		Set<Node> seen = new HashSet<>();
		ArrayList<Node> current = new ArrayList<>(startNodes);
		HashMap<Node, Integer> nodeToId = new HashMap<>();
		StringBuilder sb = new StringBuilder();
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
					sb.append(n).append(" (").append(id).append(")\n");
					TreeMap<String, Node> sorted = new TreeMap<>();
					for (Node s : n.getSuccessors()) {
						sorted.put(s.getName(), s);
					}
					for (Map.Entry<String, Node> entry : sorted.entrySet()) {
						Node s = entry.getValue();
						Integer id_succ = nodeToId.get(s);
						if (id_succ == null) {
							id_succ = nodeCounter++;
							nodeToId.put(s, id_succ);
						}
						if (!seen.contains(s)) {
							newWork.add(s);
						}
						sb.append("\t-> ").append(s).append("(").append(id_succ).append(")").append("\n");
					}
					seen.add(n);
				}
			}
			current = newWork;
		}
		return sb.toString();
	}

	//	public static void clearDB() {
	//		String uri = System.getenv().getOrDefault("NEO4J_URI", "bolt://localhost");
	//		String username = System.getenv().getOrDefault("NEO4J_USERNAME", "neo4j");
	//		String password = System.getenv().getOrDefault("NEO4J_PASSWORD", "password");
	//		Configuration configuration = (new Configuration.Builder()).uri(uri).autoIndex("none").credentials(username, password).verifyConnection(true).build();
	//		SessionFactory sessionFactory = new SessionFactory(configuration, "de.fraunhofer.aisec.markmodel.fsm");
	//		Session session = sessionFactory.openSession();
	//		session.purgeDatabase();
	//		sessionFactory.close();
	//	}
	//
	//	public void pushToDB() {
	//		String uri = System.getenv().getOrDefault("NEO4J_URI", "bolt://localhost");
	//		String username = System.getenv().getOrDefault("NEO4J_USERNAME", "neo4j");
	//		String password = System.getenv().getOrDefault("NEO4J_PASSWORD", "password");
	//		Configuration configuration = (new Configuration.Builder()).uri(uri).autoIndex("none").credentials(username, password).verifyConnection(true).build();
	//		SessionFactory sessionFactory = new SessionFactory(configuration, "de.fraunhofer.aisec.markmodel.fsm");
	//		Session session = sessionFactory.openSession();
	//		startNodes.forEach(session::save);
	//		sessionFactory.close();
	//	}

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
	public void sequenceToFSM(final Expression seq) {
		Node start = new Node(null, "BEGIN");
		start.setStart(true);

		Set<Node> endNodes = new HashSet<>();
		endNodes.add(start);
		// System.out.println(MarkInterpreter.exprToString(seq));
		expressionToNodes(seq, endNodes, null);

		// not strictly needed, we could simply set end=true for all the returned nodes
		Node end = new Node(null, "END");
		end.setEnd(true);
		end.setFake(true);
		endNodes.forEach(x -> x.addSuccessor(end));

		// we could remove BEGIN here, and set begin=true for its successors
		for (Node n : start.getSuccessors()) {
			n.setStart(true);
		}
		startNodes = start.getSuccessors();
	}

	private void expressionToNodes(
			final Expression expr, final Set<Node> endNodes, final Head head) {
		if (expr instanceof Terminal) {
			Terminal inner = (Terminal) expr;
			Node n = new Node(inner.getEntity(), inner.getOp());
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
				Set<Node> remember = new HashSet<>(endNodes);
				expressionToNodes(inner.getExpr(), endNodes, head);
				endNodes.addAll(remember);
				return;
			} else if ("+".equals(op)) {
				Head innerHead = new Head();
				innerHead.addNextNode = true;
				expressionToNodes(inner.getExpr(), endNodes, innerHead);
				for (Node j : innerHead.get()) {
					// connect to innerHead
					endNodes.forEach(x -> x.addSuccessor(j));
					if (head != null && head.addNextNode) {
						head.add(j);
					}
				}
				return;
			} else if ("*".equals(op)) {
				Set<Node> remember = new HashSet<>(endNodes);
				Head innerHead = new Head();
				innerHead.addNextNode = true;
				expressionToNodes(inner.getExpr(), endNodes, innerHead);
				for (Node j : innerHead.get()) {
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
			Set<Node> remember = new HashSet<>(endNodes);
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

	public Set<Node> getStart() {
		return startNodes;
	}

	private static class Head {
		private ArrayList<Node> nodes = new ArrayList<>();
		private Boolean addNextNode = null;

		void add(Node n) {
			nodes.add(n);
		}

		ArrayList<Node> get() {
			return nodes;
		}
	}
}
