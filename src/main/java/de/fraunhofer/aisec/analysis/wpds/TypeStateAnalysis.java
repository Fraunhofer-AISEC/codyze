
package de.fraunhofer.aisec.analysis.wpds;

import de.breakpointsec.pushdown.IllegalTransitionException;
import de.breakpointsec.pushdown.WPDS;
import de.breakpointsec.pushdown.fsm.Transition;
import de.breakpointsec.pushdown.fsm.WeightedAutomaton;
import de.breakpointsec.pushdown.rules.NormalRule;
import de.breakpointsec.pushdown.rules.PopRule;
import de.breakpointsec.pushdown.rules.PushRule;
import de.breakpointsec.pushdown.rules.Rule;
import de.fraunhofer.aisec.analysis.structures.*;
import de.fraunhofer.aisec.analysis.utils.Utils;
import de.fraunhofer.aisec.cpg.graph.*;
import de.fraunhofer.aisec.cpg.sarif.PhysicalLocation;
import de.fraunhofer.aisec.cpg.sarif.Region;
import de.fraunhofer.aisec.crymlin.CrymlinQueryWrapper;
import de.fraunhofer.aisec.crymlin.connectors.db.OverflowDatabase;
import de.fraunhofer.aisec.crymlin.dsl.CrymlinTraversal;
import de.fraunhofer.aisec.crymlin.dsl.CrymlinTraversalSource;
import de.fraunhofer.aisec.mark.markDsl.OpStatement;
import de.fraunhofer.aisec.mark.markDsl.OrderExpression;
import de.fraunhofer.aisec.mark.markDsl.Terminal;
import de.fraunhofer.aisec.markmodel.MEntity;
import de.fraunhofer.aisec.markmodel.MOp;
import de.fraunhofer.aisec.markmodel.MRule;
import de.fraunhofer.aisec.markmodel.fsm.Node;
import org.apache.tinkerpop.gremlin.process.traversal.util.FastNoSuchElementException;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

import static de.fraunhofer.aisec.crymlin.CrymlinQueryWrapper.isCallExpression;
import static de.fraunhofer.aisec.crymlin.dsl.__.__;
import static java.lang.Math.toIntExact;

/**
 * Implementation of a WPDS-based typestate analysis using the code property graph (CPG).
 * <p>
 * Legal typestates are given by a regular expression as of the MARK "order" construct. This class will convert this regular expression into a "typestate NFA". The
 * transitions of the typestate NFA are then represented as "weights" for a weighted pushdown system (WPDS). The WPDS is an abstraction of the data flows in the program
 * (currently there is one WPDS per function, but we can easily extend this to inter-procedural WPDS'es).
 * <p>
 * Given an "initial configuration" in form of a "weighted automaton" P, the "post-*" algorithm [1] will then "saturate" this weighted automaton P into Ps. The saturated
 * weighted automaton Ps is a representation of all type states reachable from the initial configuration, given the underlying program abstraction in form of the WPDS.
 * <p>
 * Thus when inspecting the weights (which are effectively type state transitions) of the saturated weighted automaton Ps, we can check if all operations on an object
 * (and its aliases) refer to legal type state transitions, if there is an execution path in the program which reaches the end of the typestate, or if any operation leads
 * to an illegal typestate (= empty weight in a transition of Ps).
 * <p>
 * <p>
 * [1] Reps T., Lal A., Kidd N. (2007) Program Analysis Using Weighted Pushdown Systems. In: Arvind V., Prasad S. (eds) FSTTCS 2007: Foundations of Software Technology
 * and Theoretical Computer Science. FSTTCS 2007. Lecture Notes in Computer Science, vol 4855. Springer, Berlin, Heidelberg
 */
public class TypeStateAnalysis {
	private static final Logger log = LoggerFactory.getLogger(TypeStateAnalysis.class);
	private MRule rule;
	private final MarkContextHolder markContextHolder;
	private CPGInstanceContext instanceContext;

	public TypeStateAnalysis(MarkContextHolder markContextHolder) {
		this.markContextHolder = markContextHolder;
	}

	/**
	 * Starts the Typestate analysis.
	 *
	 * @param orderExpr
	 * @param contextID
	 * @param ctx
	 * @param crymlinTraversal
	 * @param rule
	 * @return
	 * @throws IllegalTransitionException
	 */
	public ConstantValue analyze(OrderExpression orderExpr, Integer contextID, AnalysisContext ctx,
			CrymlinTraversalSource crymlinTraversal,
			MRule rule) throws IllegalTransitionException {
		log.info("Typestate analysis starting for {} and {}", ctx, crymlinTraversal);

		instanceContext = markContextHolder.getContext(contextID).getInstanceContext();
		this.rule = rule;

		// Remember the order expression we are analyzing
		de.fraunhofer.aisec.mark.markDsl.Expression expr = this.rule.getStatement().getEnsure().getExp();
		if (!(expr instanceof OrderExpression)) {
			log.error("Unexpected: TS analysis not dealing with an order expression");
			return ErrorValue.newErrorValue("Unexpected: TS analysis not dealing with an order expression");
		}

		String markInstance = getMarkInstanceOrderExpression(orderExpr);
		if (markInstance == null) {
			log.error("OrderExpression does not refer to a Mark instance: {}. Will not run TS analysis", orderExpr);
			return ErrorValue.newErrorValue(String.format("OrderExpression does not refer to a Mark instance: %s. Will not run TS analysis", orderExpr.toString()));
		}

		/* Create typestate NFA, representing the regular expression of a MARK typestate rule. */
		NFA tsNFA = NFA.of(orderExpr.getExp());
		log.debug("Initial typestate NFA:\n{}", tsNFA);

		// Create a weighted pushdown system
		CpgWpds wpds = createWpds(crymlinTraversal, tsNFA);

		/*
		 * Create a weighted automaton (= a weighted NFA) that describes the initial configurations. The initial configuration is the statement containing the declaration
		 * of the program variable (e.g., "x = Botan2()") that corresponds to the current Mark instance.
		 *
		 * (e.g., "b").
		 */
		File currentFile = getFileFromMarkInstance(markInstance, crymlinTraversal);
		if (currentFile == null) {
			currentFile = new File("FIXME");
		}

		WeightedAutomaton<Stmt, Val, TypestateWeight> wnfa = createInitialConfiguration(wpds);

		// For debugging only: Print WPDS rules
		if (log.isDebugEnabled()) {
			for (Rule r : wpds.getAllRules()
					.stream()
					.sorted(Comparator.comparing(r -> r.getL1()
							.getRegion()
							.getStartLine()))
					.sorted(Comparator.comparing(r -> r.getL1()
							.getRegion()
							.getStartColumn()))
					.collect(Collectors.toList())) {
				log.debug("rule: {}", r);
			}

			// For debugging only: Print the non-saturated NFA
			log.debug("Non saturated NFA {}", wnfa);
		}
		// Saturate the NFA from the WPDS, using the post-* algorithm.
		wpds.poststar(wnfa);

		// For debugging only: Print the post-*-saturated NFA
		log.debug("Saturated WNFA {}", wnfa);

		// Evaluate saturated WNFA for any MARK violations
		Set<Finding> findings = getFindingsFromWpds(wpds, wnfa, currentFile.toURI());

		if (markContextHolder.isCreateFindingsDuringEvaluation()) {
			ctx.getFindings().addAll(findings);
		}

		ConstantValue of = ConstantValue.of(findings.isEmpty());
		if (markContextHolder.isCreateFindingsDuringEvaluation()) {
			markContextHolder.getContext(contextID).setFindingAlreadyAdded(true);
		}
		return of;
	}

	@Nullable
	private File getFileFromMarkInstance(String markInstance, CrymlinTraversalSource crymlinTraversal) {
		Vertex v = instanceContext.getVertex(markInstance);
		if (v == null) {
			log.error("No vertex found for Mark instance: {}. Will not run TS analysis", markInstance);
			return null;
		}

		// Find the function in which the vertex is located, so we can use the first statement in function as a start
		Optional<Vertex> containingFunctionOpt = CrymlinQueryWrapper.getContainingFunction(v, crymlinTraversal);
		if (containingFunctionOpt.isEmpty()) {
			log.error("Vertex {} not located within a function. Cannot start TS analysis for rule {}", v.property("code").orElse(""), rule);
			return null;
		}

		// Turn function vertex into a FunctionDeclaration so we can work with it
		FunctionDeclaration funcDecl = (FunctionDeclaration) OverflowDatabase.getInstance()
				.vertexToNode(containingFunctionOpt.get());
		if (funcDecl == null) {
			log.error("Function {} could not be retrieved as a FunctionDeclaration. Cannot start TS analysis for rule {}",
				containingFunctionOpt.get().property("name").orElse(""), rule);
			return null;
		}
		return new File(funcDecl.getFile());
	}

	@Nullable
	private String getMarkInstanceOrderExpression(OrderExpression orderExpr) {
		TreeIterator<EObject> treeIt = orderExpr.eAllContents();
		while (treeIt.hasNext()) {
			EObject eObj = treeIt.next();
			if (eObj instanceof Terminal) {
				return ((Terminal) eObj).getEntity();
			}
		}
		return null;
	}

	/**
	 * Evaluates a saturated WNFA.
	 * <p>
	 * This method receives a post-*-saturated WNFA and creates Findings if any violations of the given MARK rule are found.
	 * <p>
	 * 1) Transitions in WNFA with *empty weights* or weights into an ERROR type state indicate an error. Type state requirements are violated at this point.
	 *
	 * 2) If there is a path through the automaton leading to the END state, the type state specification is completely covered by this path
	 *
	 * 3) If all transitions have proper type state weights but none of them leads to END, the type state is correct but incomplete.
	 *
	 *
	 * @param wpds
	 * @param wnfa Weighted NFA, representing a set of configurations of the WPDS
	 * @param currentFile
	 * @return
	 */
	@NonNull
	private Set<Finding> getFindingsFromWpds(CpgWpds wpds, @NonNull WeightedAutomaton<Stmt, Val, TypestateWeight> wnfa,
			URI currentFile) {
		// Final findings
		Set<Finding> findings = new HashSet<>();
		// We collect good findings first, but add them only if TS machine reaches END state
		Set<NonNullPair<Stmt, Val>> potentialGoodFindings = new HashSet<>();
		boolean endReached = false;

		// All configurations for which we have rules. Ignoring Weight.ONE
		Set<NonNullPair<Stmt, Val>> wpdsConfigs = new HashSet<>();
		for (Rule<Stmt, Val, TypestateWeight> r : wpds.getAllRules()) {
			if (!r.getWeight().equals(TypestateWeight.one())) {
				wpdsConfigs.add(new NonNullPair<Stmt, Val>(r.getL1(), r.getS1()));
				wpdsConfigs.add(new NonNullPair<Stmt, Val>(r.getL2(), r.getS2()));
			}

		}

		for (Transition<Stmt, Val> tran : wnfa.getTransitions()) {
			TypestateWeight w = wnfa.getWeightFor(tran);
			if (w.value() instanceof Set) {
				Set<NFATransition<Node>> reachableTypestates = (Set<NFATransition<Node>>) w.value();
				for (NFATransition<Node> reachableTypestate : reachableTypestates) {
					if (reachableTypestate.getTarget().isError()) {
						findings.add(createBadFinding(tran, currentFile));
					} else {
						potentialGoodFindings.add(new NonNullPair(tran.getLabel(), tran.getStart()));
					}

					endReached |= reachableTypestate.getTarget().isEnd();
				}
			} else if (w.equals(TypestateWeight.zero())) {
				// Check if this is actually a feasible configuration
				NonNullPair<Stmt, Val> conf = new NonNullPair<>(tran.getLabel(), tran.getStart());
				if (wpdsConfigs.stream().anyMatch(c -> c.getValue0().equals(conf.getValue0()) && c.getValue1().equals(conf.getValue1()))) {
					findings.add(createBadFinding(conf.getValue0(), conf.getValue1(), currentFile, Set.of()));
				}
			}
		}

		if (endReached && findings.isEmpty()) {
			findings.addAll(potentialGoodFindings.stream().map(p -> createGoodFinding(p.getValue0(), p.getValue1(), currentFile)).collect(Collectors.toSet()));
		}

		return findings;
	}

	/**
	 * Creates a finding indicating a typestate error in the program.
	 *
	 * @param stmt
	 * @param val
	 * @param currentFile
	 * @param expected
	 * @return
	 */
	private Finding createBadFinding(Stmt stmt, Val val, @NonNull URI currentFile, @NonNull Collection<NFATransition<Node>> expected) {
		String name = "Invalid typestate of variable " + val + " at statement: " + stmt + " . Violates order of " + rule.getName();
		if (!expected.isEmpty()) {
			name += " Expected one of " + expected.stream().map(NFATransition::toString).collect(Collectors.joining(", "));
		} else {
			name += " Expected no further operations.";
		}

		// lines are human-readable, i.e., off-by-one
		int startLine = toIntExact(stmt.getRegion().getStartLine()) - 1;
		int endLine = toIntExact(stmt.getRegion().getEndLine()) - 1;
		int startColumn = toIntExact(stmt.getRegion().getStartColumn()) - 1;
		int endColumn = toIntExact(stmt.getRegion().getEndColumn()) - 1;
		return new Finding(name, rule.getErrorMessage(), currentFile, startLine, endLine, startColumn, endColumn);
	}

	private Finding createBadFinding(Transition<Stmt, Val> t, URI currentFile) {
		return createBadFinding(t.getLabel(), t.getStart(), currentFile, List.of());
	}

	/**
	 * Create a "non-finding" (i.e. positive confirmation)
	 * Lines are human-readable, i.e., off-by-one.
	 *
	 * @param stmt
	 * @param val
	 * @param currentFile
	 * @return
	 */
	private Finding createGoodFinding(Stmt stmt, Val val, URI currentFile) {
		int startLine = toIntExact(stmt.getRegion().getStartLine()) - 1;
		int endLine = toIntExact(stmt.getRegion().getEndLine()) - 1;
		int startColumn = toIntExact(stmt.getRegion().getStartColumn()) - 1;
		int endColumn = toIntExact(stmt.getRegion().getEndColumn()) - 1;
		return new Finding("Good: " + val + " at " + stmt, currentFile, rule.getErrorMessage(),
			List.of(new Region(startLine, endLine, startColumn, endColumn)), false);
	}

	/**
	 * Creates a weighted pushdown system (WPDS), linked to a typestate NFA.
	 * <p>
	 * When populating the WPDS using post-* algorithm, the result will be an automaton capturing the reachable type states.
	 *
	 * @param crymlinTraversal
	 * @param tsNfa
	 * @return
	 * @throws IllegalTransitionException
	 */
	private CpgWpds createWpds(CrymlinTraversalSource crymlinTraversal, NFA tsNfa) {
		log.info("-----  Creating WPDS ----------");
		HashSet<Vertex> alreadySeen = new HashSet<>();
		/**
		 * We need OverflowDB to convert vertices back to CPG nodes.
		 */
		OverflowDatabase<de.fraunhofer.aisec.cpg.graph.Node> odb = OverflowDatabase.getInstance();

		/* Create empty WPDS */
		CpgWpds wpds = new CpgWpds();

		// TODO Optimization: WPDS can be limited to the slicing for the relevant statements. Currently we transform whole functions into a WPDS.

		/**
		 * For each function, create a WPDS
		 *
		 * The (normal, push, pop) rules of the WPDS reflect the data flow, similar to a static taint analysis.
		 *
		 */
		for (Vertex functionDeclaration : crymlinTraversal.functiondeclarations().toList()) {
			FunctionDeclaration fd = (FunctionDeclaration) odb.vertexToNode(functionDeclaration);
			if (fd == null) {
				log.error("Unexpected: Got FunctionDeclaration vertex but could not convert to Node object: {}", functionDeclaration);
				continue;
			}
			String currentFunctionName = fd.getName();
			log.info("Processing function {}", currentFunctionName);

			// Work list of following EOG nodes. Not all EOG nodes will result in a WPDS rule, though.
			ArrayDeque<NonNullPair<Vertex, Set<Stmt>>> worklist = new ArrayDeque<>();
			worklist.add(new NonNullPair<>(functionDeclaration, Set.of(new Stmt(fd.getName(), getRegion(fd)))));

			Map<Stmt, Val> skipTheseValsAtStmt = new HashMap<>();
			Set<Val> valsInScope = new HashSet<>();

			// Make sure we track all parameters inside this function
			List<ParamVariableDeclaration> params = fd.getParameters();
			for (ParamVariableDeclaration p : params) {
				valsInScope.add(new Val(p.getName(), currentFunctionName));
			}

			// Start creation of WPDS rules by traversing the EOG
			while (!worklist.isEmpty()) {
				NonNullPair<Vertex, Set<Stmt>> currentPair = worklist.pop();
				Vertex v = currentPair.getValue0();
				for (Stmt previousStmt : currentPair.getValue1()) {
					// We consider only "Statements" and CallExpressions in the EOG
					if (isRelevantStmt(v)) {

						Stmt currentStmt = vertexToStmt(v);

						Statement stmtNode = (Statement) odb.vertexToNode(v);
						if (stmtNode != null) {
							/* First we create a normal rule from previous stmt to the current (=the call) */
							Set<NormalRule<Stmt, Val, TypestateWeight>> normalRules = createNormalRules(tsNfa, stmtNode, previousStmt, currentStmt, valsInScope);
							for (NormalRule<Stmt, Val, TypestateWeight> normalRule : normalRules) {
								boolean skipIt = false;
								if (skipTheseValsAtStmt.get(normalRule.getL2()) != null) {
									Val forbiddenVal = skipTheseValsAtStmt.get(normalRule.getL2());
									if (!normalRule.getS1()
											.equals(forbiddenVal)) {
										skipIt = true;
									}
								}
								if (!skipIt) {
									wpds.addRule(normalRule);
								}
							}

							if (isCallExpression(v) && !isPhantom((CallExpression) stmtNode)) {
								CallExpression callE = (CallExpression) stmtNode;
								/*
								 * For calls to functions whose body is known, we create push/pop rule pairs. All arguments flow into the parameters of the function. The
								 * "return site" is the statement to which flow returns after the function call.
								 */
								Set<PushRule<Stmt, Val, TypestateWeight>> pushRules = createPushRules(callE, crymlinTraversal, currentFunctionName, tsNfa, previousStmt,
									currentStmt,
									v, worklist);
								for (PushRule<Stmt, Val, TypestateWeight> pushRule : pushRules) {
									log.debug("  Adding push rule: {}", pushRule);
									wpds.addRule(pushRule);

									// Remember that arguments flow only into callee and do not bypass it.
									skipTheseValsAtStmt.put(pushRule.getCallSite(), pushRule.getS1());
								}
							}

						}
						/* "DeclarationStatements" result in a normal rule, assigning rhs to lhs. */

						// TODO We might be a bit more gracious here to tolerate incorrect code. For example, a non-declared variable would be a BinaryOperator.
						Iterator<Edge> declarations = v.edges(Direction.OUT, "DECLARATIONS");
						if (isDeclarationStatement(v) && declarations.hasNext()) {
							log.debug("Found variable declaration {}", v.property("code")
									.orElse(""));

							Vertex decl = declarations.next()
									.inVertex();
							Val declVal = new Val((String) decl.property("name")
									.value(),
								currentFunctionName);

							Optional<Vertex> rhs = Optional.empty();
							if (decl.edges(Direction.OUT, "INITIALIZER")
									.hasNext()) {
								// TODO Do not simply assume that the target of an INITIALIZER edge is a variable
								rhs = Optional.of(decl.edges(Direction.OUT, "INITIALIZER")
										.next()
										.inVertex());
							}

							if (rhs.isPresent()) {
								Vertex rhsVertex = rhs.get();
								if (rhsVertex.property("name")
										.isPresent()
										&& !"".equals(rhsVertex.property("name")
												.value())) {
									log.debug("  Has name on right hand side {}", rhsVertex.property("name")
											.value());
									if (Utils.hasLabel(rhsVertex, MemberCallExpression.class)) {
										// handle member calls
										MemberCallExpression call = (MemberCallExpression) OverflowDatabase.getInstance()
												.vertexToNode(rhsVertex);
										if (call == null) {
											log.error("Unexpected: null base of MemberCallExpression " + rhsVertex);
											continue;
										}
										de.fraunhofer.aisec.cpg.graph.Node base = call.getBase();
										Val rhsVal = new Val(base.getName(), currentFunctionName);

										// Add declVal to set of currently tracked variables
										valsInScope.add(declVal);

										Rule<Stmt, Val, TypestateWeight> normalRuleSelf = new NormalRule<>(rhsVal, previousStmt, rhsVal, currentStmt,
											TypestateWeight.one());
										log.debug("Adding normal rule for member call {}", normalRuleSelf);
										wpds.addRule(normalRuleSelf);
									} else if (Utils.hasLabel(rhsVertex, CallExpression.class)) {
										// handle function calls
										CallExpression call = (CallExpression) OverflowDatabase.getInstance()
												.vertexToNode(rhsVertex);
										if (call == null) {
											log.error("Unexpected: null base of CallExpression " + rhsVertex);
											continue;
										}
										// Add declVal to set of currently tracked variables
										valsInScope.add(declVal);

										for (Val val : valsInScope) {
											Rule<Stmt, Val, TypestateWeight> normalRuleSelf = new NormalRule<>(val, previousStmt, val, currentStmt,
												TypestateWeight.one());
											log.debug("Adding normal rule for function call {}", normalRuleSelf);
											wpds.addRule(normalRuleSelf);
										}
									} else {
										// Propagate flow from right-hand to left-hand side (variable declaration)
										Val rhsVal = new Val((String) rhsVertex.property("name")
												.value(),
											currentFunctionName);
										// Add declVal to set of currently tracked variables
										valsInScope.add(declVal);
										Rule<Stmt, Val, TypestateWeight> normalRulePropagate = new NormalRule<>(rhsVal, previousStmt, declVal, currentStmt,
											TypestateWeight.one());
										log.debug("Adding normal rule for assignment {}", normalRulePropagate);
										wpds.addRule(normalRulePropagate);
									}

									// Additionally, flow remains at rhs.
									Val rhsVal = new Val((String) rhsVertex.property("name")
											.value(),
										currentFunctionName);
									Rule<Stmt, Val, TypestateWeight> normalRuleCopy = new NormalRule<>(rhsVal, previousStmt, rhsVal, currentStmt, TypestateWeight.one());
									log.debug("Adding normal rule, propagating unchanged values {}", normalRuleCopy);
									wpds.addRule(normalRuleCopy);

								} else {
									// handle new instantiations of objects
									log.debug("  Has no name on right hand side: {}", v.property("code")
											.orElse(""));

									// Normal copy of all values in scope
									for (Val valInScope : valsInScope) {
										Rule<Stmt, Val, TypestateWeight> normalRule = new NormalRule<>(valInScope, previousStmt, valInScope, currentStmt,
											TypestateWeight.one());
										if (!skipTheseValsAtStmt.containsKey(normalRule.getL2())) {
											Val forbiddenVal = skipTheseValsAtStmt.get(normalRule.getL2());
											if (!normalRule.getS1()
													.equals(forbiddenVal)) {
												log.debug("Adding normal rule, propagating vars {}", normalRule);
												wpds.addRule(normalRule);
											}
										}
									}

									// Add declVal to set of currently tracked variables
									valsInScope.add(declVal);

									// Create normal rule
									Rule<Stmt, Val, TypestateWeight> normalRule = new NormalRule<>(new Val("EPSILON", currentFunctionName), previousStmt, declVal,
										currentStmt,
										TypestateWeight.one());
									log.debug("Adding normal rule for epsilon {}", normalRule);
									wpds.addRule(normalRule);

								}
							}
						} else if (isReturnStatement(v)) {
							/* Return statements result in pop rules */
							ReturnStatement returnV = (ReturnStatement) odb.vertexToNode(v);
							if (returnV != null && !returnV.isDummy()) {
								Set<Val> returnedVals = findReturnedVals(crymlinTraversal, v);

								for (Val returnedVal : returnedVals) {
									Set<NFATransition<Node>> relevantNFATransitions = tsNfa.getTransitions()
											.stream()
											.filter(
												tran -> tran.getTarget()
														.getOp()
														.equals(returnedVal.getVariable()))
											.collect(Collectors.toSet());
									TypestateWeight weight = relevantNFATransitions.isEmpty() ? TypestateWeight.one() : new TypestateWeight(relevantNFATransitions);

									// Pop Rule for actually returned value
									PopRule<Stmt, Val, TypestateWeight> returnPopRule = new PopRule<>(new Val(returnV.getReturnValue()
											.getName(),
										currentFunctionName),
										currentStmt, returnedVal, weight);
									wpds.addRule(returnPopRule);
									log.debug("Adding pop rule {}", returnPopRule);
								}

								// Pop Rules for side effects on parameters
								Map<String, Set<Pair<Val, Val>>> paramToValueMap = findParamToValues(functionDeclaration, v, odb, crymlinTraversal);
								if (paramToValueMap.containsKey(currentFunctionName)) {
									for (Pair<Val, Val> pToA : paramToValueMap.get(currentFunctionName)) {
										PopRule<Stmt, Val, TypestateWeight> popRule = new PopRule<>(pToA.getValue0(), currentStmt, pToA.getValue1(),
											TypestateWeight.one());
										wpds.addRule(popRule);
										log.debug("Adding pop rule {}", popRule);
									}
								}

							}
							// Create normal rule. Flow remains where it is.
							for (Val valInScope : valsInScope) {
								Rule<Stmt, Val, TypestateWeight> normalRule = new NormalRule<>(valInScope, previousStmt, valInScope, currentStmt, TypestateWeight.one());
								boolean skipIt = false;
								if (skipTheseValsAtStmt.get(normalRule.getL2()) != null) {
									Val forbiddenVal = skipTheseValsAtStmt.get(normalRule.getL2());
									if (!normalRule.getS1()
											.equals(forbiddenVal)) {
										skipIt = true;
									}
								}
								if (!skipIt) {
									log.debug("Adding normal rule!!! {}", normalRule);
									wpds.addRule(normalRule);
								}
							}

						}
					} // End isRelevantStmt()
				}

				// Add successors to work list
				Collection<? extends Vertex> successors = getSuccessors(v, alreadySeen);
				for (Vertex succ : successors) {
					if (isRelevantStmt(v)) {
						worklist.add(new NonNullPair<>(succ, Set.of(vertexToStmt(v))));
					} else {
						worklist.add(new NonNullPair<>(succ, currentPair.getValue1()));
					}
				}
			}
		}

		/*
		 * Typestate analysis is finished. The results are as follows: 1) Transitions in WNFA with *empty weights* or weights into an ZERO type state indicate an error.
		 * Type state requirements are violated at this point. 2) If there is a path through the automaton leading to the END state, the type state specification is
		 * completely covered by this path 3) If all transitions have proper type state weights but none of them leads to END, the type state is correct but incomplete.
		 */

		return wpds;
	}

	@NonNull
	private Region getRegion(@NonNull FunctionDeclaration fd) {
		Region region = new Region(-1, -1, -1, -1);
		PhysicalLocation loc = fd.getLocation();
		if (loc != null) {
			return loc.getRegion();
		}
		return region;
	}

	/**
	 * Returns a set of Vertices which are successors of <code>v</code> in the EOG and are not contained in <code>alreadySeen</code>.
	 *
	 * @param v
	 * @param alreadySeen
	 * @return
	 */
	@NonNull
	private Collection<? extends Vertex> getSuccessors(@NonNull final Vertex v, @NonNull final HashSet<Vertex> alreadySeen) {
		Set<Vertex> unseenSuccessors = new HashSet<>();
		Vertex vertex = v;
		Iterator<Edge> eogSuccessors = vertex.edges(Direction.OUT, "EOG");
		while (eogSuccessors.hasNext()) {
			Vertex succ = eogSuccessors.next()
					.inVertex();
			if (!alreadySeen.contains(succ)) {
				unseenSuccessors.add(succ);
				alreadySeen.add(succ);
			}
		}
		return unseenSuccessors;
	}

	/**
	 * We do not convert all EOG nodes into WPDS rules, but only "relevant" ones, i.e. statements and call expressions.
	 *
	 * @param v
	 * @return
	 */
	private boolean isRelevantStmt(Vertex v) {
		int numberOfOutgoingEogs = 0;
		Iterator<Edge> eogs = v.edges(Direction.OUT, "EOG");
		while (eogs.hasNext()) {
			numberOfOutgoingEogs++;
			eogs.next();
		}
		return isCallExpression(v) || Utils.hasLabel(v, IfStatement.class) || v.edges(Direction.IN, "STATEMENTS").hasNext() || numberOfOutgoingEogs >= 2;
	}

	private boolean isReturnStatement(Vertex v) {
		return v.label().equals(ReturnStatement.class.getSimpleName());
	}

	private boolean isDeclarationStatement(Vertex v) {
		return v.label().equals(DeclarationStatement.class.getSimpleName());
	}

	private Set<NormalRule<Stmt, Val, TypestateWeight>> createNormalRules(final NFA tsNfa, final Statement currentStmtNode, final Stmt previousStmt,
			final Stmt currentStmt,
			final Set<Val> valsInScope) {
		Set<NormalRule<Stmt, Val, TypestateWeight>> result = new HashSet<>();
		Set<NFATransition<Node>> relevantNFATransitions = tsNfa.getTransitions()
				.stream()
				.filter(
					tran -> belongsToOp(currentStmtNode.getName(), tran.getTarget().getBase(), tran.getTarget().getOp()))
				.collect(Collectors.toSet());
		TypestateWeight weight = relevantNFATransitions.isEmpty() ? TypestateWeight.one() : new TypestateWeight(relevantNFATransitions);

		// Create normal rule. Flow remains where it is.
		for (Val valInScope : valsInScope) {
			NormalRule<Stmt, Val, TypestateWeight> normalRule = new NormalRule<>(valInScope, previousStmt, valInScope, currentStmt, weight);
			log.debug("Adding normal rule {}", normalRule);
			result.add(normalRule);
		}

		return result;
	}

	/**
	 * Returns true if [markInstance].[call] refers to [op] of [entity.
	 *
	 * @param call
	 * @param markInstance
	 * @param op
	 * @return
	 */
	private boolean belongsToOp(@NonNull String call, @Nullable String markInstance, String op) {
		if (markInstance == null || call.equals("")) {
			return false;
		}

		// Get the MARK entity of the markInstance
		Pair<String, MEntity> mEntity = this.rule.getEntityReferences().get(markInstance);
		if (mEntity == null || mEntity.getValue1() == null) {
			return false;
		}

		// TODO this method is called a few times and repeats some work. Potential for caching/optimization.

		for (MOp o : mEntity.getValue1().getOps()) {
			if (!op.equals(o.getName())) {
				continue;
			}
			for (OpStatement opStatement : o.getStatements()) {
				if (opStatement.getCall().getName().endsWith(call)) {
					// TODO should rather compare fully qualified names instead of "endsWith"
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns true if the given CallExpression refers to a function whose Body is not available.
	 *
	 * @param mce
	 * @return
	 */
	private boolean isPhantom(CallExpression mce) {
		return mce.getInvokes().isEmpty();
	}

	/**
	 * Finds the mapping from function parameters to arguments of calls to this method. This is needed for later construction of pop rules.
	 *
	 * @param functionDeclaration
	 * @param returnV
	 * @param odb
	 * @param crymlinTraversalSource
	 * @return
	 */
	@NonNull
	private Map<String, Set<Pair<Val, Val>>> findParamToValues(Vertex functionDeclaration, Vertex returnV,
			OverflowDatabase<de.fraunhofer.aisec.cpg.graph.Node> odb, CrymlinTraversalSource crymlinTraversalSource) {
		Map<String, Set<Pair<Val, Val>>> result = new HashMap<>();
		try {
			FunctionDeclaration calleeFD = (FunctionDeclaration) odb.vertexToNode(functionDeclaration);
			if (calleeFD == null) {
				log.error("Unexpected: FunctionDeclaration of callee is null.");
				return result;
			}
			String calleeName = calleeFD.getName();

			List<Vertex> calls = crymlinTraversalSource.byID((long) returnV.id())
					.repeat(__().out("DFG"))
					.until(
						__().hasLabel(CallExpression.class.getSimpleName()))
					.limit(5)
					.toList();

			for (Vertex call : calls) {
				CallExpression ce = (CallExpression) odb.vertexToNode(call);
				if (ce == null) {
					continue;
				}

				/*
				 * Find name of calling function ("caller") TODO This is not an optimal way to find out the calling function, as we need to traverse potentially many EOG
				 * edges.
				 */
				List<Vertex> callers = crymlinTraversalSource.byID((long) call.id())
						.repeat(__().in("EOG"))
						.until(
							__().hasLabel(FunctionDeclaration.class.getSimpleName()))
						.limit(50)
						.toList();

				for (Vertex callerV : callers) {
					FunctionDeclaration caller = (FunctionDeclaration) odb.vertexToNode(callerV);
					if (caller == null) {
						log.error("Unexpected: Null Node object for FunctionDeclaration vertex {}", callerV.id());
						continue;
					}
					List<Expression> args = ce.getArguments();
					FunctionDeclaration callee = ce.getInvokes()
							.get(
								0); // TODO we assume there is exactly one (=our) called function ("callee"). In case of fuzzy resolution, there might be more.
					List<ParamVariableDeclaration> params = callee.getParameters();

					Set<Pair<Val, Val>> pToA = new HashSet<>();
					for (int i = 0; i < Math.min(params.size(), args.size()); i++) {
						pToA.add(new Pair<>(new Val(params.get(i).getName(), calleeName), new Val(args.get(i).getName(), caller.getName())));
					}
					result.put(calleeName, pToA);
				}
			}
		}
		catch (FastNoSuchElementException e) {
			log.error("FastNoSuchElementException", e);
		}

		return result;
	}

	/**
	 * Given a return statement, this method finds all variables at the caller site that might be assigned the returned value.
	 * <p>
	 * In the following example, given the return statement in bla(), this method will return a Val for "x".
	 * <p>
	 * void blubb() { int x = bla(); }
	 * <p>
	 * int bla() { return 42; }
	 *
	 * @param v
	 * @return
	 */
	private Set<Val> findReturnedVals(CrymlinTraversalSource crymlinTraversalSource, Vertex v) {
		/*
		 * Follow along "DFG" edges from the return statement to the CallExpression that initiated the call. Then check if there is a "DFG" edge from that CallExpression
		 * to a VariableDeclaration.
		 */
		Set<Val> returnedVals = new HashSet<>();
		List<Vertex> calls = crymlinTraversalSource.byID((long) v.id())
				.repeat(__().out("DFG"))
				.until(__().hasLabel(CallExpression.class.getSimpleName()))
				.limit(
					5)
				.toList();

		for (Vertex call : calls) {
			// We found the call site into our method. Now see if the return value is used.
			Optional<Vertex> nextDfgAftercall = crymlinTraversalSource.byID((long) call.id()).out("DFG").tryNext();
			String returnVar = "";
			if (nextDfgAftercall.isPresent()) {
				if (nextDfgAftercall.get().label().equals(VariableDeclaration.class.getSimpleName())
						|| nextDfgAftercall.get().label().equals(DeclaredReferenceExpression.class.getSimpleName())) {
					// return value is used. Remember variable name.
					returnVar = nextDfgAftercall.get().property("name").value().toString();
				}

				// Finally we need to find out in which function the call site actually is
				String callerFunctionName = null;
				CrymlinTraversal<Vertex, Vertex> traversal = crymlinTraversalSource.byID((long) call.id())
						.repeat(__().out("EOG"))
						.until(__().in("STATEMENTS"))
						//					.limit(10)
						.in("STATEMENTS")
						.in("BODY");
				if (traversal.hasNext()) {
					Vertex callerFunction = traversal.next();
					if (callerFunction != null) {
						callerFunctionName = callerFunction.property("name").value().toString();
					}

					if (callerFunctionName != null) {
						returnedVals.add(new Val(returnVar, callerFunctionName));
					}
				}
			}
		}

		return returnedVals;
	}

	/**
	 * Creates push rules for a given call expression.
	 * <p>
	 * Typically, there will be only a single push rule per call expression. Only in case of multiple return sites, such as when considering exception handling, the
	 * resulting set may contain more than one rule.
	 *
	 * @param mce
	 * @param crymlinTraversal
	 * @param currentFunctionName
	 * @param nfa
	 * @param previousStmt
	 * @param currentStmt
	 * @param v
	 * @return
	 */
	private Set<PushRule<Stmt, Val, TypestateWeight>> createPushRules(CallExpression mce, CrymlinTraversalSource crymlinTraversal, String currentFunctionName,
			NFA nfa, Stmt previousStmt, Stmt currentStmt, Vertex v, ArrayDeque<NonNullPair<Vertex, Set<Stmt>>> worklist) {
		Set<NFATransition<Node>> relevantNFATransitions = nfa.getTransitions()
				.stream()
				.filter(
					tran -> belongsToOp(mce.getName(), tran.getTarget().getBase(), tran.getTarget().getOp()))
				.collect(Collectors.toSet());
		TypestateWeight weight = relevantNFATransitions.isEmpty() ? TypestateWeight.one() : new TypestateWeight(relevantNFATransitions);

		// Return site(s). Actually, multiple return sites will only occur in case of exception handling.
		List<Vertex> returnSites = CrymlinQueryWrapper.getNextStatements(crymlinTraversal, (long) v.id());

		// Arguments of function call
		List<Val> argVals = argumentsToVals(mce, currentFunctionName);

		Set<PushRule<Stmt, Val, TypestateWeight>> pushRules = new HashSet<>();
		for (FunctionDeclaration potentialCallee : mce.getInvokes()) {
			// Parameters of function
			if (potentialCallee.getParameters().size() != argVals.size()) {
				log.warn("Skipping call from {} to {} due different argument/parameter counts.", currentFunctionName,
					potentialCallee.getSignature());
				continue;
			}
			List<Val> parmVals = parametersToVals(potentialCallee);

			// Get first statement of callee. This is the jump target of our Push Rule.
			Statement firstStmt = getFirstStmtOfMethod(potentialCallee);

			if (firstStmt != null && firstStmt.getCode() != null) {
				for (int i = 0; i < argVals.size(); i++) {
					for (Vertex returnSiteVertex : returnSites) {
						Stmt returnSite = vertexToStmt(returnSiteVertex);

						PushRule<Stmt, Val, TypestateWeight> pushRule = new PushRule<>(
							argVals.get(i),
							currentStmt,
							parmVals.get(i),
							new Stmt(potentialCallee.getName(), getRegion(potentialCallee)),
							returnSite,
							weight);
						pushRules.add(pushRule);
					}
				}
			} else {
				log.error("Unexpected: Found a method with body, but no first statement relevant for WPDS: {}", potentialCallee.getName());
			}
		}

		return pushRules;
	}

	@Nullable
	private Statement getFirstStmtOfMethod(@NonNull FunctionDeclaration potentialCallee) {
		if (potentialCallee.getBody() != null) {
			Statement firstStmt = potentialCallee.getBody();
			while (firstStmt instanceof CompoundStatement) {
				firstStmt = ((CompoundStatement) firstStmt).getStatements().get(0);
			}
			return firstStmt;
		}

		log.error("Function does not have a body: {}", potentialCallee.getName());
		return null;
	}

	/**
	 * Returns a (mutable) list of the function parameters of <code>func</code>, each wrapped as a <code>Val</code>.
	 *
	 * @param func
	 * @return
	 */
	@NonNull
	private List<Val> parametersToVals(@NonNull FunctionDeclaration func) {
		List<Val> parmVals = new ArrayList<>();
		for (ParamVariableDeclaration p : func.getParameters()) {
			parmVals.add(new Val(p.getName(), func.getName()));
		}
		return parmVals;
	}

	private List<Val> argumentsToVals(CallExpression mce, String currentFunctionName) {
		List<Val> argVals = new ArrayList<>();
		List<Expression> args = mce.getArguments();
		for (Expression arg : args) {
			argVals.add(new Val(arg.getName(), currentFunctionName));
		}
		return argVals;
	}

	/**
	 * Creates an initial configuration of a WPDS from where post* runs.
	 * <p>
	 * The initial configuration comprises the set of states (i.e. statements and variables on the stack) which are relevant for following typestate analysis and is given
	 * in form of a weighted automaton P.
	 * <p>
	 * Typically, the initial configuration will refer to a single "trigger" statement from where typestate analysis should start. This statement i
	 *
	 *
	 * @return
	 */
	@NonNull
	private WeightedAutomaton createInitialConfiguration(@NonNull WPDS wpds) {
		// Get START state from WPDS
		Val initialState = null;
		Stmt stmt = null;
		Set<NormalRule<Stmt, Val, TypestateWeight>> normalRules = wpds.getNormalRules();
		for (NormalRule<Stmt, Val, TypestateWeight> nr : normalRules) {
			if (nr.getWeight().value() instanceof Set) {
				Set<NFATransition> weight = (Set<NFATransition>) nr.getWeight().value();

				for (NFATransition<Node> t : weight) {
					if (t.getSource().getName().equals("START.START")) {
						log.debug("Found start configuration for typestate analysis: " + nr.getS1() + " at " + nr.getL1());

						initialState = nr.getS1();
						stmt = nr.getL1();
					}
				}
			}
		}

		if (initialState == null || stmt == null) {
			log.error("Did not find initial configuration for typestate analysis. Will fail soon.");
		}

		// Create statement for start configuration and create start CONFIG
		WeightedAutomaton<Stmt, Val, TypestateWeight> wnfa = new WeightedAutomaton<>(initialState) {
			@Override
			public Val createState(Val val, Stmt stmt) {
				return val;
			}

			@Override
			public boolean isGeneratedState(Val val) {
				return false;
			}

			@Override
			public Stmt epsilon() {
				return new Stmt("EPSILON", new Region(-1, -1, -1, -1));
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
		Val ACCEPTING = new Val("ACCEPT", "ACCEPT");
		// Create an automaton for the initial configuration from where post* will start.
		wnfa.addTransition(new Transition<>(initialState, stmt, ACCEPTING), TypestateWeight.one());
		// Add final ("accepting") states to NFA.
		wnfa.addFinalState(ACCEPTING);

		return wnfa;
	}

	/**
	 * Convert a CPG vertex into a <code>Stmt</code> in context of the WPDS.
	 *
	 * @param v CPG vertex
	 * @return A <code>Stmt</code>, holding the "code" and "location->region" properties of <code>v</code>>.
	 */
	@NonNull
	private Stmt vertexToStmt(@NonNull Vertex v) {
		Region region = new Region(-1, -1, -1, -1);
		if (v.property("startLine").isPresent() &&
				v.property("startColumn").isPresent() &&
				v.property("endLine").isPresent() &&
				v.property("endColumn").isPresent()) {
			region = new Region(
				toIntExact((long) v.property("startLine")
						.value()),
				toIntExact((long) v.property("startColumn")
						.value()),
				toIntExact((long) v.property("endLine")
						.value()),
				toIntExact((long) v.property("endColumn")
						.value()));
		}
		Stmt stmt = new Stmt(
			v.property("code")
					.orElse("")
					.toString(),
			region);
		return stmt;
	}
}
