
package de.fraunhofer.aisec.analysis.markevaluation;

import de.fraunhofer.aisec.analysis.structures.AnalysisContext;
import de.fraunhofer.aisec.analysis.structures.CPGInstanceContext;
import de.fraunhofer.aisec.analysis.structures.Finding;
import de.fraunhofer.aisec.analysis.structures.Pair;
import de.fraunhofer.aisec.analysis.structures.ResultWithContext;
import de.fraunhofer.aisec.crymlin.dsl.CrymlinTraversalSource;
import de.fraunhofer.aisec.mark.markDsl.OrderExpression;
import de.fraunhofer.aisec.markmodel.MEntity;
import de.fraunhofer.aisec.markmodel.MOp;
import de.fraunhofer.aisec.markmodel.MRule;
import de.fraunhofer.aisec.markmodel.fsm.FSM;
import de.fraunhofer.aisec.markmodel.fsm.Node;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.Math.toIntExact;

public class OrderNFAEvaluator {

	private static final Logger log = LoggerFactory.getLogger(OrderEvaluator.class);
	private final MRule rule;

	public OrderNFAEvaluator(MRule rule) {
		this.rule = rule;
	}

	public ResultWithContext evaluate(OrderExpression orderExpression, CPGInstanceContext instanceContext, AnalysisContext ctx,
			CrymlinTraversalSource crymlinTraversal) {
		/*
		 * We also look through forbidden nodes. The fact that these are forbidden is checked elsewhere Any function calls to functions which are not specified in an
		 * entity are _ignored_
		 */

		// fixme only look at instances of instanceContext!! currently this analyzes __ALL__ orders!!

		boolean isOrderValid = true;

		// rule.getFSM().pushToDB(); //debug only
		log.info("\tEvaluating rule {}", rule.getName());

		// Cache which Vertex belongs to which Op/Entity
		// a vertex can _only_ belong to one entity/op!
		HashMap<Vertex, MOp> verticesToOp = new HashMap<>();
		for (Map.Entry<String, Pair<String, MEntity>> entry : rule.getEntityReferences().entrySet()) {
			MEntity ent = entry.getValue().getValue1();
			if (ent == null) {
				continue;
			}
			for (MOp op : ent.getOps()) {
				op.getAllVertices().forEach(v -> verticesToOp.put(v, op));
			}
		}

		if (verticesToOp.isEmpty()) {
			log.info("no nodes match this rule. Skipping rule.");
			return null;
		}

		// collect all instances used in this order
		HashSet<Pair<String, String>> instances = new HashSet<>();
		ExpressionHelper.collectInstanceAndOps(orderExpression.getExp(), instances);

		HashSet<String> entityReferences = instances.stream().map(Pair::getValue0).collect(Collectors.toCollection(HashSet::new));
		HashSet<Object> referencedVertices = new HashSet<>();
		for (String alias : entityReferences) {
			Vertex v = instanceContext.entityGet(alias);
			if (v == null) {
				log.error("alias {} is not referenced in this rule {}", alias, rule.getName());
				return null;
			}
			referencedVertices.add(v.id());
		}

		FSM fsm = new FSM();
		fsm.sequenceToFSM(orderExpression.getExp());

		// fixme this needs to be optimized to only look at functions where the base of an at least op is in referencedVertices
		for (Vertex functionDeclaration : crymlinTraversal.functiondeclarations().toList()) {
			log.info("Evaluating function {}", (Object) functionDeclaration.value("name"));

			// todo DT: should we allow this different entities in an order?
			//  rule UseOfBotan_CipherMode {
			//      using Forbidden as cm, Foo as f
			//  ensure order cm.start(), cm.finish(), f.done()
			//  onfail WrongUseOfBotan_CipherMode
			//  } -> this does currently not work, as we store for each base, where in the FSM it is.
			// BUT in this case, an instance of cm would always have a different base than f.
			//
			// is aliasing inside an order rule allowed?
			// I.e. order x.a, x.b, x.c
			// x i1;
			// i1.a();
			// i1.b();
			// x i2 = i1;
			// i2.c(); -> do we know if i2 is a copy, or an alias?
			// -> always mark as error?
			// -> currently this will result in:
			//   Violation against Order: i2.c(); (c) is not allowed. Expected one of: x.a
			//   Violation against Order: Base i1 is not correctly terminated. Expected one of [x.c] to follow the last call on this base.

			HashSet<Vertex> currentWorklist = new HashSet<>();
			currentWorklist.add(functionDeclaration);

			// which bases did we already see, but are not initialized correctly base to set of eogpaths
			HashMap<String, HashSet<String>> disallowedBases = new HashMap<>();
			// stores the current markings in the FSM (i.e., which base is at which FSM-node)
			HashMap<String, HashSet<Node>> baseToFSMNodes = new HashMap<>();
			// last usage of base
			HashMap<String, Vertex> lastBaseUsage = new HashMap<>();

			HashMap<Long, HashSet<String>> nodeIDtoEOGPathSet = new HashMap<>();
			HashSet<String> startEOG = new HashSet<>();
			startEOG.add("0");
			nodeIDtoEOGPathSet.put((Long) functionDeclaration.id(), startEOG);

			HashSet<String> seenStates = new HashSet<>();
			long visitedNodes = 0;

			while (!currentWorklist.isEmpty()) {
				HashSet<Vertex> nextWorklist = new HashSet<>();
				//            System.out.println("SEEN: " + String.join(", ", seenStates));
				//            printWorklist(currentWorklist, nodeIDtoEOGPathSet);
				//            System.out.println();

				for (Vertex vertex : currentWorklist) {
					visitedNodes++;

					String currentState = getStateSnapshot(vertex, baseToFSMNodes);
					seenStates.add(currentState);

					HashSet<String> eogPathSet = nodeIDtoEOGPathSet.get((Long) vertex.id());
					for (String eogPath : eogPathSet) {

						// ... no direct access to the labels TreeSet of Neo4JVertex
						// TODO JS: This might need to be adapted to non-multilabels with OverflowDB.
						if (vertex.label().contains("MemberCallExpression")
								// is the vertex part of any op of any mentioned entity? If not, ignore
								&& verticesToOp.get(vertex) != null) {

							MOp op = verticesToOp.get(vertex);
							// check if the vertex actually belongs to a entity used in this rule
							if (rule.getEntityReferences()
									.values()
									.stream()
									.anyMatch(x -> x.getValue1()
											.equals(op.getParent()))) {

								Iterator<Edge> it = vertex.edges(Direction.OUT, "BASE");
								String base = null;
								String ref = null;
								Vertex refNode = null;
								if (it.hasNext()) {
									Vertex baseVertex = it.next()
											.inVertex();
									base = baseVertex.value("name");
									Iterator<Edge> it_ref = baseVertex.edges(Direction.OUT, "REFERS_TO");
									if (it_ref.hasNext()) {
										refNode = it_ref.next()
												.inVertex();
										ref = refNode.id().toString();
									}
								} else {
									log.error("base must not be null for MemberCallExpressions");
									assert false;
								}

								if (refNode != null
										&& !referencedVertices.contains(refNode.id())) {
									log.info("this call does not reference the function we are looking at, skipping.");

								} else {
									// if we have a reference to a node in the cpg, we add this to the prefixed
									// base this way, we could differentiate between nodes with the same base
									// name, but referencing different variables (e.g., if they are used in
									// different blocks)
									if (ref != null) {
										base += "|" + ref;
									}

									String prefixedBase = eogPath + "." + base;

									if (isDisallowedBase(disallowedBases, eogPath, base)) {
										//                      Finding f =
										//                          new Finding(
										//                              "Violation against Order: "
										//                                  + vertex.value("code")
										//                                  + " is not allowed. Base contains errors already."
										//                                  + " ("
										//                                  + rule.getErrorMessage()
										//                                  + ")",
										//                              vertex.value("startLine"),
										//                              vertex.value("endLine"),
										//                              vertex.value("startColumn"),
										//                              vertex.value("endColumn"));
										// we hide base errors for now!
										// ctx.getFindings().add(f);
										// log.info("Finding: {}", f.toString());
									} else {
										HashSet<Node> nodesInFSM;
										if (baseToFSMNodes.get(prefixedBase) == null) {
											// we have not seen this base before. check if this is the start of an order
											nodesInFSM = fsm.getStart(); // start nodes
										} else {
											nodesInFSM = baseToFSMNodes.get(prefixedBase); // nodes calculated in previous step
										}

										HashSet<Node> nextNodesInFSM = new HashSet<>();

										// did at least one fsm-Node-match occur?
										boolean match = false;
										for (Node n : nodesInFSM) {
											// are there any ops corresponding to the current base and the current function name?
											if (op != null && op.getName().equals(n.getOp())) {
												// this also has as effect, that if the FSM is in a end-state and a
												// intermediate state, and we follow the intermediate state, the
												// end-state is removed again, which is correct!
												nextNodesInFSM.addAll(n.getSuccessors());
												match = true;
											}
										}
										if (!match) {
											// if not, this call is not allowed, and this base must not be used in the
											// following eog
											isOrderValid = false;
											Finding f = new Finding(
												"Violation against Order: "
														+ vertex.value("code")
														+ " ("
														+ (op == null ? "null" : op.getName())
														+ ") is not allowed. Expected one of: "
														+ nodesInFSM.stream()
																.map(Node::getName)
																.sorted()
																.collect(Collectors.joining(", "))
														+ " ("
														+ rule.getErrorMessage()
														+ ")",
												rule.getErrorMessage(),
												toIntExact(vertex.value("startLine")) - 1,
												toIntExact(vertex.value("endLine")) - 1,
												toIntExact(vertex.value("startColumn")) - 1,
												toIntExact(vertex.value("endColumn")) - 1);
											ctx.getFindings().add(f);
											log.info("Finding: {}", f);
											disallowedBases.computeIfAbsent(base, x -> new HashSet<>()).add(eogPath);
										} else {
											String baseLocal = prefixedBase.split("\\.")[1]; // remove eogpath
											Vertex vertex1 = lastBaseUsage.get(baseLocal);
											long prevMaxLine = 0;
											if (vertex1 != null) {
												prevMaxLine = vertex1.value("startLine");
											}
											long newLine = vertex.value("startLine");
											if (prevMaxLine <= newLine) {
												lastBaseUsage.put(baseLocal, vertex);
											}
											baseToFSMNodes.put(prefixedBase, nextNodesInFSM);
										}
									}
								}
							}
						}
						ArrayList<Vertex> outVertices = new ArrayList<>();
						vertex.edges(Direction.OUT, "EOG")
								.forEachRemaining(edge -> outVertices.add(edge.inVertex()));

						// if more than one vertex follows the curreant one, we need to branch the eogPath
						if (outVertices.size() > 1) { // split
							HashSet<String> oldBases = new HashSet<>();
							HashMap<String, HashSet<Node>> newBases = new HashMap<>();
							// first we collect all entries which we need to remove from the baseToFSMNodes
							// map we also store these entries without the eog path prefix, to update later
							// in (1)
							for (Map.Entry<String, HashSet<Node>> entry : baseToFSMNodes.entrySet()) {
								if (entry.getKey().startsWith(eogPath)) {
									oldBases.add(entry.getKey());
									// keep the "." before the real base, as we need it later anyway
									newBases.put(entry.getKey().substring(eogPath.length()),
										entry.getValue());
								}
							}
							oldBases.forEach(baseToFSMNodes::remove);

							// (1) update all entries previously removed from the baseToFSMNodes map with
							// the new eogpath as prefix to the base
							for (int i = outVertices.size() - 1; i >= 0; i--) {
								// also update them in the baseToFSMNodes map
								String newEOGPath = eogPath + i;
								newBases.forEach((k, v) -> baseToFSMNodes.put(newEOGPath + k, v));

								String stateOfNext = getStateSnapshot(outVertices.get(i), baseToFSMNodes);
								if (seenStates.contains(stateOfNext)) {
									log.debug("node/FSM state already visited: {}. Do not split into this.", stateOfNext);
									outVertices.remove(i);
								} else {
									// update the eogpath directly in the vertices for the next step
									nodeIDtoEOGPathSet.computeIfAbsent((Long) outVertices.get(i).id(),
										x -> new HashSet<>()).add(newEOGPath);
								}
							}
						} else if (outVertices.size() == 1) {
							// else, if we only have one vertex following this
							// vertex, simply propagate the current eogpath to the next vertex
							nodeIDtoEOGPathSet.computeIfAbsent((Long) outVertices.get(0).id(),
								x -> new HashSet<>()).add(eogPath);
						}

						nextWorklist.addAll(outVertices);
					}
					// the current vertex has been analyzed with all these eogpath. remove from map, if we
					// visit it in another iteration
					nodeIDtoEOGPathSet.remove((Long) vertex.id());
				}
				currentWorklist = nextWorklist;
			}

			log.info("Done evaluating function {}, rule {}. Visited Nodes: {}", functionDeclaration.value("name"), rule.getName(), visitedNodes);
			// now the whole function was evaluated.
			// Check that the FSM is in its end/beginning state for all bases
			HashMap<String, HashSet<String>> nonterminatedBases = new HashMap<>();
			for (Map.Entry<String, HashSet<Node>> entry : baseToFSMNodes.entrySet()) {
				boolean hasEnd = false;
				HashSet<String> notEnded = new HashSet<>();
				for (Node n : entry.getValue()) {
					if (n.isEnd()) {
						// if one of the nodes in this fsm is at an END-node, this is fine.
						hasEnd = true;
						break;
					} else {
						notEnded.add(n.getName());
					}
				}
				if (!hasEnd) {
					// extract the real base name from eogpath.base
					HashSet<String> next = nonterminatedBases.computeIfAbsent(entry.getKey()
							.substring(entry.getKey().indexOf('.') + 1),
						x -> new HashSet<>());
					next.addAll(notEnded);
				}
			}
			for (Map.Entry<String, HashSet<String>> entry : nonterminatedBases.entrySet()) {
				isOrderValid = false;
				Vertex vertex = lastBaseUsage.get(entry.getKey());
				String base = entry.getKey().split("\\|")[0]; // remove potential refers_to local
				Finding f = new Finding(
					"Violation against Order: Base "
							+ base
							+ " is not correctly terminated. Expected one of ["
							+ entry.getValue()
									.stream()
									.sorted()
									.collect(Collectors.joining(", "))
							+ "] to follow the correct last call on this base."
							+ " ("
							+ rule.getErrorMessage()
							+ ")",
					rule.getErrorMessage(),
					toIntExact(vertex.value("startLine")) - 1,
					toIntExact(vertex.value("endLine")) - 1,
					toIntExact(vertex.value("startColumn")) - 1,
					toIntExact(vertex.value("endColumn")) - 1);
				ctx.getFindings()
						.add(f);
				log.info("Finding: {}", f);
			}
		}
		ResultWithContext result = ResultWithContext.fromLiteralOrOperand(isOrderValid);
		result.setFindingAlreadyAdded(true);
		return result;
	}

	private boolean isDisallowedBase(
			HashMap<String, HashSet<String>> disallowedBases, String eogpath, String base) {
		HashSet<String> disallowedEOGPaths = disallowedBases.get(base);
		if (disallowedEOGPaths != null) {
			return disallowedEOGPaths.stream().anyMatch(eogpath::startsWith);
		}
		return false;
	}

	private String getStateSnapshot(Vertex v, HashMap<String, HashSet<Node>> baseToFSMNodes) {
		HashMap<String, HashSet<Node>> simplified = new HashMap<>();

		for (Map.Entry<String, HashSet<Node>> entry : baseToFSMNodes.entrySet()) {
			simplified.computeIfAbsent(entry.getKey().split("\\.")[1], x -> new HashSet<>()).addAll(entry.getValue());
		}

		List<String> fsmStates = simplified.entrySet()
				.stream()
				.map(
					x -> x.getKey()
							+ "("
							+ x.getValue().stream().map(Node::toString).collect(Collectors.joining(","))
							+ ")")
				.distinct()
				.sorted()
				.collect(Collectors.toList());

		return v.id() + " " + String.join(",", fsmStates);
	}

}
