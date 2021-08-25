
package de.fraunhofer.aisec.codyze.analysis.markevaluation;

import de.fraunhofer.aisec.codyze.analysis.*;
import de.fraunhofer.aisec.codyze.analysis.resolution.ConstantValue;
import de.fraunhofer.aisec.codyze.analysis.utils.Utils;
import de.fraunhofer.aisec.cpg.graph.Graph;
import de.fraunhofer.aisec.cpg.graph.Node;
import de.fraunhofer.aisec.cpg.graph.declarations.VariableDeclaration;
import de.fraunhofer.aisec.cpg.graph.statements.expressions.CallExpression;
import de.fraunhofer.aisec.cpg.graph.statements.expressions.ConstructExpression;
import de.fraunhofer.aisec.cpg.graph.statements.expressions.DeclaredReferenceExpression;
import de.fraunhofer.aisec.cpg.graph.statements.expressions.MemberCallExpression;
import de.fraunhofer.aisec.mark.markDsl.OrderExpression;
import de.fraunhofer.aisec.codyze.markmodel.MEntity;
import de.fraunhofer.aisec.codyze.markmodel.MOp;
import de.fraunhofer.aisec.codyze.markmodel.MRule;
import de.fraunhofer.aisec.codyze.markmodel.fsm.FSM;
import de.fraunhofer.aisec.codyze.markmodel.fsm.StateNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static de.fraunhofer.aisec.codyze.analysis.markevaluation.EvaluationHelperKt.getBaseOfCallExpressionUsingArgument;
import static de.fraunhofer.aisec.codyze.analysis.passes.EdgeCachePassKt.getAstParent;
import static de.fraunhofer.aisec.codyze.analysis.markevaluation.EvaluationHelperKt.getContainingFunction;

public class OrderNFAEvaluator {

	private static final Logger log = LoggerFactory.getLogger(OrderNFAEvaluator.class);
	private final MRule rule;
	private final MarkContextHolder markContextHolder;

	public OrderNFAEvaluator(MRule rule, MarkContextHolder markContextHolder) {
		this.rule = rule;
		this.markContextHolder = markContextHolder;
	}

	@SuppressWarnings({ "java:S1905", "java:S125" })
	public ConstantValue evaluate(OrderExpression orderExpression, Integer contextID, AnalysisContext ctx,
			Graph graph) {
		// We also look through forbidden nodes. The fact that these are forbidden is checked elsewhere
		// Any function calls to functions which are not specified in an entity are _ignored_

		// it is NOT allowed to use different entities in one order
		//  rule UseOfBotan_CipherMode {
		//      using Forbidden as cm, Foo as f
		//  ensure order cm.start(), cm.finish(), f.done()
		//  onfail WrongUseOfBotan_CipherMode
		//  } -> this does not work, as we store for each base, where in the FSM it is.
		// in this case, an instance of cm would always have a different base than f.

		// For this analysis, Aliasing is not analysed!
		// I.e. order x.a, x.b, x.c
		// x i1;
		// i1.a();
		// i1.b();
		// x i2 = i1;
		// i2.c(); -> we do not necessarily know if i2 is a copy, or an alias?
		// -> currently this will result in:
		//   Violation against Order: i2.c(); (c) is not allowed. Expected one of: x.a
		//   Violation against Order: Base i1 is not correctly terminated. Expected one of [x.c] to follow the last call on this base.

		var instanceContext = markContextHolder.getContext(contextID).getInstanceContext();

		var markInstances = new HashSet<String>();
		ExpressionHelper.collectMarkInstances(orderExpression.getExp(), markInstances); // extract all used markvars from the expression

		if (markInstances.size() > 1) {
			log.warn("Order statement contains more than one base. Not supported.");
			return ErrorValue.newErrorValue("Order statement contains more than one base. Not supported.");
		}
		if (markInstances.isEmpty()) {
			log.warn("Order statement does not contain any ops. Invalid order");
			return ErrorValue.newErrorValue("Order statement does not contain any ops. Invalid order");
		}

		String markVar = markInstances.iterator().next();

		var variableDecl = instanceContext.getNode(markVar);
		if (variableDecl == null) {
			log.warn("No instance for markvar {} set in the instancecontext. Invalid evaluation.", markVar);
			return ErrorValue.newErrorValue(String.format("No instance for markvar %s set in the instancecontext. Invalid evaluation.", markVar));
		}

		var containingFunction = getContainingFunction(variableDecl);
		if (containingFunction == null) {
			log.error("Instance vertex {} is not contained in a method/function", variableDecl.getCode());
			return ErrorValue.newErrorValue(String.format("Instance vertex %s is not contained in a method/function", variableDecl.getCode()));
		}

		boolean isOrderValid = true;

		// rule.getFSM().pushToDB(); //debug only
		log.info("\tEvaluating rule {}", rule.getName());

		// Cache which Vertex belongs to which Op/Entity
		// a vertex can _only_ belong to one entity/op!
		var verticesToOp = new HashMap<de.fraunhofer.aisec.cpg.graph.Node, MOp>();
		for (var entry : rule.getEntityReferences().entrySet()) {
			MEntity ent = entry.getValue().getSecond();

			if (ent == null) {
				continue;
			}

			for (MOp op : ent.getOps()) {
				op.getAllNodes().forEach(v -> verticesToOp.put(v, op));
			}
		}

		if (verticesToOp.isEmpty()) {
			log.info("no nodes match this rule. Skipping rule.");
			return ErrorValue.newErrorValue("no nodes match this rule. Skipping rule.");
		}

		// collect all instances used in this order
		// TODO: same as in line67?
		var entityReferences = new HashSet<String>();
		ExpressionHelper.collectMarkInstances(orderExpression.getExp(), entityReferences);

		var referencedVertices = new HashSet<Long>();
		for (String alias : entityReferences) {
			var v = instanceContext.getNode(alias);
			if (v == null) {
				log.error("alias {} is not referenced in this rule {}", alias, rule.getName());
				return ErrorValue.newErrorValue(String.format("alias %s is not referenced in this rule %s", alias, rule.getName()));
			}
			referencedVertices.add(v.getId());
		}

		var fsm = new FSM();
		fsm.sequenceToFSM(orderExpression.getExp());

		log.info("Evaluating function {}", containingFunction.getName());

		var currentWorklist = new HashSet<de.fraunhofer.aisec.cpg.graph.Node>();
		currentWorklist.add(containingFunction);

		// which bases did we already see, but are not initialized correctly base to set of eogpaths
		HashMap<String, HashSet<String>> disallowedBases = new HashMap<>();
		// stores the current markings in the FSM (i.e., which base is at which FSM-node)
		HashMap<String, HashSet<StateNode>> baseToFSMNodes = new HashMap<>();
		// last usage of base
		HashMap<String, de.fraunhofer.aisec.cpg.graph.Node> lastBaseUsage = new HashMap<>();

		HashMap<Long, HashSet<String>> nodeIDtoEOGPathSet = new HashMap<>();
		HashSet<String> startEOG = new HashSet<>();
		startEOG.add("0");
		nodeIDtoEOGPathSet.put(containingFunction.getId(), startEOG);

		HashSet<String> seenStates = new HashSet<>();
		long visitedNodes = 0;

		while (!currentWorklist.isEmpty()) {
			HashSet<de.fraunhofer.aisec.cpg.graph.Node> nextWorklist = new HashSet<>();

			for (var vertex : currentWorklist) {
				visitedNodes++;

				String currentState = getStateSnapshot(vertex, baseToFSMNodes);
				seenStates.add(currentState);

				HashSet<String> eogPathSet = nodeIDtoEOGPathSet.get(vertex.getId());
				if (eogPathSet == null) {
					log.warn("Error during Order-evaluation, no path set for node {}", vertex.getId());
					continue;
				}
				for (String eogPath : eogPathSet) {
					// ... no direct access to the labels TreeSet of Neo4JVertex
					if ((vertex instanceof CallExpression)
							// is the vertex part of any op of any mentioned entity? If not, ignore
							&& verticesToOp.get(vertex) != null) {

						MOp op = verticesToOp.get(vertex);
						// check if the vertex actually belongs to a entity used in this rule
						if (rule.getEntityReferences()
								.values()
								.stream()
								.anyMatch(x -> Objects.equals(x.getSecond(), op.getParent()))) {

							String base = null;
							String ref = null;
							de.fraunhofer.aisec.cpg.graph.Node refNode = null;
							if (vertex instanceof MemberCallExpression) {
								var baseVertex = ((MemberCallExpression) vertex).getBase();
								base = baseVertex.getName();
								if (baseVertex instanceof DeclaredReferenceExpression) {
									refNode = ((DeclaredReferenceExpression) baseVertex).getRefersTo();
									if (refNode != null) {
										ref = refNode.getId().toString();
									}
								}
							} else if (vertex instanceof ConstructExpression) { // ctor
								var initializerBase = getAstParent(vertex);
								if (initializerBase != null) {
									var it = initializerBase.getNextDFG().iterator();
									if (it.hasNext()) {
										var baseVertex = it.next();
										base = baseVertex.getName();
										// for ctor, the DFG points already to the variabledecl
										refNode = baseVertex;
										ref = refNode.getId().toString();
									}
								}
							} else if (vertex instanceof CallExpression) {
								var foundUsingThis = false;
								var opstmt = op.getNodesToStatements().get(vertex);
								if (opstmt.size() == 1) {
									var params = opstmt.iterator().next().getCall().getParams();
									var thisPositions = IntStream.range(0, params.size())
											.filter(i -> "this".equals(params.get(i).getVar()))
											.toArray();
									if (thisPositions.length == 1) {
										refNode = getBaseOfCallExpressionUsingArgument((CallExpression) vertex, thisPositions[0]);
										base = refNode.getName();
										ref = refNode.getId().toString();
										foundUsingThis = true;
									}
								}
								if (!foundUsingThis) {
									// There could potentially be multiple DFG targets. this can lead to
									// inconsistent results. Therefore, we filter the DFG targets for "interesting" types
									// and also sort them by name to make this more consistent.
									var next = vertex.getNextDFG()
											.stream()
											.filter(node -> node instanceof ConstructExpression || node instanceof VariableDeclaration
													|| node instanceof DeclaredReferenceExpression)
											.sorted(Comparator.comparing(Node::getName))
											.collect(Collectors.toList());

									if (!next.isEmpty()) {
										var baseVertex = next.get(0);

										base = baseVertex.getName();
										if (baseVertex instanceof ConstructExpression) {
											var it = baseVertex.getNextDFG().iterator();
											// this potentially has the same problem as above
											if (it.hasNext()) {
												baseVertex = it.next();
												base = baseVertex.getName();
											}
										}
										if (baseVertex instanceof VariableDeclaration) {
											// this is already the reference
											refNode = baseVertex;
											ref = refNode.getId().toString();
										} else {
											if (baseVertex instanceof DeclaredReferenceExpression) {
												refNode = ((DeclaredReferenceExpression) baseVertex).getRefersTo();
												if (refNode != null) {
													ref = refNode.getId().toString();
												}
											}
										}
									}
								}
							}

							if (base == null) {
								log.error("base must not be null for {}", vertex.getClass().getSimpleName());
							} else {
								if (refNode != null
										&& !referencedVertices.contains(refNode.getId())) {
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
										// we hide base errors for now!
									} else {
										Set<StateNode> nodesInFSM;
										if (baseToFSMNodes.get(prefixedBase) == null) {
											// we have not seen this base before. check if this is the start of an order
											nodesInFSM = fsm.getStart(); // start nodes
										} else {
											nodesInFSM = baseToFSMNodes.get(prefixedBase); // nodes calculated in previous step
										}

										HashSet<StateNode> nextNodesInFSM = new HashSet<>();

										// did at least one fsm-Node-match occur?
										boolean match = false;
										for (StateNode n : nodesInFSM) {
											// are there any ops corresponding to the current base and the current function name?
											if (op.getName().equals(n.getOp())) {
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

											var region = Utils.getRegionByNode(vertex);
											Finding f = new Finding(
												"Violation against Order: "
														+ vertex.getCode()
														+ " ("
														+ op.getName()
														+ ") is not allowed. Expected one of: "
														+ nodesInFSM.stream()
																.map(StateNode::getName)
																.sorted()
																.collect(Collectors.joining(", "))
														+ " ("
														+ rule.getErrorMessage()
														+ ")",
												rule.getErrorMessage(),
												new File(vertex.getFile()).toURI(),
												region.getStartLine(),
												region.getEndLine(),
												region.getStartColumn(),
												region.getEndColumn());
											if (markContextHolder.isCreateFindingsDuringEvaluation()) {
												ctx.getFindings().add(f);
											}
											log.info("Finding: {}", f);
											disallowedBases.computeIfAbsent(base, x -> new HashSet<>()).add(eogPath);
										} else {
											var region = Utils.getRegionByNode(vertex);
											var vertex1 = lastBaseUsage.get(prefixedBase);
											long prevMaxLine = 0;
											if (vertex1 != null) {
												var region1 = Utils.getRegionByNode(vertex1);
												prevMaxLine = region1.getStartLine();
											}
											long newLine = region.getStartLine();
											if (prevMaxLine <= newLine) {
												lastBaseUsage.put(prefixedBase, vertex);
											}
											baseToFSMNodes.put(prefixedBase, nextNodesInFSM);
										}
									}
								}
							}
						}
					}

					var outVertices = new ArrayList<>(vertex.getNextEOG());

					// if more than one vertex follows the current one, we need to branch the eogPath
					if (outVertices.size() > 1) { // split
						HashSet<String> oldBases = new HashSet<>();
						HashMap<String, HashSet<StateNode>> newBases = new HashMap<>();
						// first we collect all entries which we need to remove from the baseToFSMNodes
						// map we also store these entries without the eog path prefix, to update later
						// in (1)
						for (Map.Entry<String, HashSet<StateNode>> entry : baseToFSMNodes.entrySet()) {
							if (entry.getKey().startsWith(eogPath)) {
								oldBases.add(entry.getKey());
								// keep the "." before the real base, as we need it later anyway
								newBases.put(entry.getKey().substring(eogPath.length()),
									entry.getValue());
							}
						}
						oldBases.forEach(baseToFSMNodes::remove);

						// (1) update all entries previously removed from the baseToFSMNodes map with
						// the new eog-path as prefix to the base
						for (int i = outVertices.size() - 1; i >= 0; i--) {
							// also update them in the baseToFSMNodes map
							String newEOGPath = eogPath + i;
							newBases.forEach((k, v) -> baseToFSMNodes.put(newEOGPath + k, v));

							String stateOfNext = getStateSnapshot(outVertices.get(i), baseToFSMNodes);
							if (seenStates.contains(stateOfNext)) {
								log.debug("node/FSM state already visited: {}. Do not split into this.", stateOfNext);
								outVertices.remove(i);
								newBases.forEach((k, v) -> baseToFSMNodes.remove(newEOGPath + k));
							} else {
								// update the eog-path directly in the vertices for the next step
								nodeIDtoEOGPathSet.computeIfAbsent(outVertices.get(i).getId(),
									x -> new HashSet<>()).add(newEOGPath);
							}
						}
					} else if (outVertices.size() == 1) {
						// else, if we only have one vertex following this
						// vertex, simply propagate the current eogpath to the next vertex
						nodeIDtoEOGPathSet.computeIfAbsent(outVertices.get(0).getId(),
							x -> new HashSet<>()).add(eogPath);
					}

					nextWorklist.addAll(outVertices);
				}
				// the current vertex has been analyzed with all these eogpath. remove from map, if we
				// visit it in another iteration
				nodeIDtoEOGPathSet.remove(vertex.getId());
			}
			currentWorklist = nextWorklist;
		}

		log.info("Done evaluating function {}, rule {}. Visited Nodes: {}", containingFunction.getName(), rule.getName(), visitedNodes);

		// now the whole function was evaluated.
		// Check that the FSM is in its end/beginning state for all bases
		HashMap<String, HashSet<String>> nonterminatedBases = new HashMap<>();
		for (Map.Entry<String, HashSet<StateNode>> entry : baseToFSMNodes.entrySet()) {
			boolean hasEnd = false;
			HashSet<String> notEnded = new HashSet<>();
			for (StateNode n : entry.getValue()) {
				if (n.isEnd()) {
					// if one of the nodes in this fsm is at an END-node, this is fine.
					hasEnd = true;
					break;
				} else {
					notEnded.add(n.getName());
				}
			}
			if (!hasEnd) {
				// extract the real base name from eog-path.base
				HashSet<String> next = nonterminatedBases.computeIfAbsent(entry.getKey(),
					x -> new HashSet<>());
				next.addAll(notEnded);
			}
		}
		for (Map.Entry<String, HashSet<String>> entry : nonterminatedBases.entrySet()) {
			isOrderValid = false;

			var vertex = lastBaseUsage.get(entry.getKey());
			if (vertex == null) {
				String[] split = entry.getKey().split("\\.");
				if (split.length != 2) {
					log.warn("Invalid eog-path");
				} else {
					if (split[0].length() <= 1) {
						log.warn("Invalid eog-path length");
					} else {
						while (vertex == null || split[0].length() > 2) { // remove a number of branches until we find a last usage
							split[0] = split[0].substring(0, split[0].length() - 1);
							vertex = lastBaseUsage.get(split[0] + "." + split[1]);
						}
					}
				}
			}
			String base = entry.getKey().split("\\|")[0]; // remove potential refers_to local
			if (base.contains(".")) {
				base = base.substring(base.indexOf('.') + 1); // remove eogpath
			}
			URI file = null;
			int startLine = -1;
			int endLine = -1;
			int startCol = -1;
			int endCol = -1;
			if (vertex != null) {
				file = new File(vertex.getFile()).toURI();
				var region = Utils.getRegionByNode(vertex);
				startLine = region.getStartLine();
				endLine = region.getEndLine();
				startCol = region.getStartColumn();
				endCol = region.getEndColumn();
			}
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
				file,
				startLine,
				endLine,
				startCol,
				endCol);
			if (markContextHolder.isCreateFindingsDuringEvaluation()) {
				ctx.getFindings()
						.add(f);
			}
			log.info("Finding: {}", f);
		}
		ConstantValue of = ConstantValue.of(isOrderValid);
		if (markContextHolder.isCreateFindingsDuringEvaluation()) {
			markContextHolder.getContext(contextID).setFindingAlreadyAdded(true);
		}
		return of;
	}

	private boolean isDisallowedBase(
			HashMap<String, HashSet<String>> disallowedBases, String eogpath, String base) {
		HashSet<String> disallowedEOGPaths = disallowedBases.get(base);
		if (disallowedEOGPaths != null) {
			return disallowedEOGPaths.stream().anyMatch(eogpath::startsWith);
		}
		return false;
	}

	private String getStateSnapshot(de.fraunhofer.aisec.cpg.graph.Node v, HashMap<String, HashSet<StateNode>> baseToFSMNodes) {
		Map<String, HashSet<StateNode>> simplified = new HashMap<>();

		for (Map.Entry<String, HashSet<StateNode>> entry : baseToFSMNodes.entrySet()) {
			simplified.computeIfAbsent(entry.getKey().split("\\.")[1], x -> new HashSet<>()).addAll(entry.getValue());
		}

		List<String> fsmStates = simplified.entrySet()
				.stream()
				.map(
					x -> x.getKey()
							+ "("
							+ x.getValue().stream().map(StateNode::toString).collect(Collectors.joining(","))
							+ ")")
				.distinct()
				.sorted()
				.collect(Collectors.toList());

		return v.getId() + " " + String.join(",", fsmStates);
	}

}
