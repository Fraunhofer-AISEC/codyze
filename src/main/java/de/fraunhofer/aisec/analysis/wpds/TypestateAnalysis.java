
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
import de.fraunhofer.aisec.cpg.graph.Graph;
import de.fraunhofer.aisec.cpg.graph.declarations.Declaration;
import de.fraunhofer.aisec.cpg.graph.declarations.FunctionDeclaration;
import de.fraunhofer.aisec.cpg.graph.declarations.ParamVariableDeclaration;
import de.fraunhofer.aisec.cpg.graph.declarations.VariableDeclaration;
import de.fraunhofer.aisec.cpg.graph.statements.*;
import de.fraunhofer.aisec.cpg.graph.statements.expressions.CallExpression;
import de.fraunhofer.aisec.cpg.graph.statements.expressions.DeclaredReferenceExpression;
import de.fraunhofer.aisec.cpg.graph.statements.expressions.Expression;
import de.fraunhofer.aisec.cpg.graph.types.Type;
import de.fraunhofer.aisec.cpg.sarif.Region;
import de.fraunhofer.aisec.mark.markDsl.OpStatement;
import de.fraunhofer.aisec.mark.markDsl.OrderExpression;
import de.fraunhofer.aisec.mark.markDsl.Terminal;
import de.fraunhofer.aisec.markmodel.MEntity;
import de.fraunhofer.aisec.markmodel.MOp;
import de.fraunhofer.aisec.markmodel.MRule;
import de.fraunhofer.aisec.markmodel.fsm.Node;
import kotlin.Pair;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

import static de.fraunhofer.aisec.analysis.cpgpasses.EdgeCachePassKt.getAstParent;
import static de.fraunhofer.aisec.analysis.markevaluation.EvaluationHelperKt.*;
import static de.fraunhofer.aisec.analysis.markevaluation.NodeExtensionsKt.getFunctions;
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
public class TypestateAnalysis {
	private static final Logger log = LoggerFactory.getLogger(TypestateAnalysis.class);
	private MRule rule;
	@NonNull
	private final MarkContextHolder markContextHolder;
	private GraphInstanceContext instanceContext;

	public TypestateAnalysis(@NonNull MarkContextHolder markContextHolder) {
		this.markContextHolder = markContextHolder;
	}

	/**
	 * Starts the Typestate analysis.
	 *
	 * @param orderExpr
	 * @param contextID
	 * @param ctx
	 * @param graph
	 * @param rule
	 * @return
	 * @throws IllegalTransitionException
	 */
	public ConstantValue analyze(OrderExpression orderExpr, Integer contextID, AnalysisContext ctx,
			Graph graph,
			MRule rule) throws IllegalTransitionException {
		log.info("Typestate analysis starting for {}", ctx);

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
			return ErrorValue.newErrorValue(String.format("OrderExpression does not refer to a Mark instance: %s. Will not run TS analysis", orderExpr));
		}

		/* Create typestate NFA, representing the regular expression of a MARK typestate rule. */
		NFA tsNFA = NFA.of(orderExpr.getExp());
		log.debug("Initial typestate NFA:\n{}", tsNFA);

		// Create a weighted pushdown system
		var wpds = createWpds(graph, tsNFA);

		/*
		 * Create a weighted automaton (= a weighted NFA) that describes the initial configurations. The initial configuration is the statement containing the declaration
		 * of the program variable (e.g., "x = Botan2()") that corresponds to the current Mark instance.
		 *
		 * (e.g., "b").
		 */
		File currentFile = getFileFromMarkInstance(markInstance);
		if (currentFile == null) {
			currentFile = new File("FIXME");
		}

		/* Create an initial automaton corresponding to the start configurations of the data flow analysis.
		   In this case, we are starting with all statements that initiate a type state transition.
		 */
		WeightedAutomaton<Stmt, Val, TypestateWeight> wnfa = InitialConfiguration.create(InitialConfiguration::FIRST_TYPESTATE_EVENT, wpds);
		// No transition - no finding.
		if (wnfa.getInitialState() == null) {
			return ConstantValue.of(Boolean.FALSE);
		}

		// For debugging only: Print WPDS rules
		if (log.isDebugEnabled()) {
			for (Rule<Stmt, Val, TypestateWeight> r : wpds.getAllRules()
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
	private File getFileFromMarkInstance(String markInstance) {
		var v = instanceContext.getNode(markInstance);
		if (v == null) {
			log.error("No vertex found for Mark instance: {}. Will not run TS analysis", markInstance);
			return null;
		}

		// Find the function in which the vertex is located, so we can use the first statement in function as a start
		var containingFunction = getContainingFunction(v);
		if (containingFunction == null) {
			log.error("Vertex {} not located within a function. Cannot start TS analysis for rule {}", v.getCode(), rule);
			return null;
		}

		return new File(containingFunction.getFile());
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
	 * <p>
	 * 2) If there is a path through the automaton leading to the END state, the type state specification is completely covered by this path
	 * <p>
	 * 3) If all transitions have proper type state weights but none of them leads to END, the type state is correct but incomplete.
	 *
	 * @param wpds
	 * @param wnfa        Weighted NFA, representing a set of configurations of the WPDS
	 * @param currentFile
	 * @return
	 */
	@NonNull
	private Set<Finding> getFindingsFromWpds(CpgWpds wpds, @NonNull WeightedAutomaton<Stmt, Val, TypestateWeight> wnfa,
			URI currentFile) {
		// Final findings
		Set<Finding> findings = new HashSet<>();
		// We collect good findings first, but add them only if TS machine reaches END state
		Set<Pair<Stmt, Val>> potentialGoodFindings = new HashSet<>();
		boolean endReached = false;

		// All configurations for which we have rules. Ignoring Weight.ONE
		Set<Pair<Stmt, Val>> wpdsConfigs = new HashSet<>();
		for (Rule<Stmt, Val, TypestateWeight> r : wpds.getAllRules()) {
			if (!r.getWeight().equals(TypestateWeight.one())) {
				wpdsConfigs.add(new Pair<>(r.getL1(), r.getS1()));
				wpdsConfigs.add(new Pair<>(r.getL2(), r.getS2()));
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
						potentialGoodFindings.add(new Pair<>(tran.getLabel(), tran.getStart()));
					}

					endReached |= reachableTypestate.getTarget().isEnd();
				}
			} else if (w.equals(TypestateWeight.zero())) {
				// Check if this is actually a feasible configuration
				var conf = new Pair<>(tran.getLabel(), tran.getStart());
				if (wpdsConfigs.stream().anyMatch(c -> c.getFirst().equals(conf.getFirst()) && c.getSecond().equals(conf.getSecond()))) {
					findings.add(createBadFinding(conf.getFirst(), conf.getSecond(), currentFile, Set.of()));
				}
			}
		}

		if (endReached && findings.isEmpty()) {
			findings.addAll(potentialGoodFindings.stream().map(p -> createGoodFinding(p.getFirst(), p.getSecond(), currentFile)).collect(Collectors.toSet()));
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
	 * @param graph
	 * @param tsNfa
	 *
	 * @return
	 */
	private CpgWpds createWpds(Graph graph, NFA tsNfa) {
		log.info("-----  Creating WPDS ----------");

		/* Create empty WPDS */
		CpgWpds wpds = new CpgWpds();

		/*
		 * For each function, create a WPDS.
		 *
		 * The (normal, push, pop) rules of the WPDS reflect the data flow, similar to a static taint analysis.
		 *
		 */
		for (var functionDeclaration : getFunctions(graph)) {
			WPDS<Stmt, Val, TypestateWeight> funcWpds = createWpds(functionDeclaration, tsNfa);
			for (Rule<Stmt, Val, TypestateWeight> r : funcWpds.getAllRules()) {
				wpds.addRule(r);
			}
		}

		/*
		 * Typestate analysis is finished. The results are as follows: 1) Transitions in WNFA with *empty weights* or weights into an ZERO type state indicate an error.
		 * Type state requirements are violated at this point. 2) If there is a path through the automaton leading to the END state, the type state specification is
		 * completely covered by this path 3) If all transitions have proper type state weights but none of them leads to END, the type state is correct but incomplete.
		 */

		return wpds;
	}

	/**
	 * Turns a single function into a WPDS.
	 *
	 * @param tsNfa
	 * @return
	 */
	private WPDS<Stmt, Val, TypestateWeight> createWpds(@NonNull FunctionDeclaration fd, NFA tsNfa) {
		// To remember already visited nodes and avoid endless iteration
		var alreadySeen = new HashSet<de.fraunhofer.aisec.cpg.graph.Node>();

		// the WPDS we are creating here
		CpgWpds wpds = new CpgWpds();

		log.info("Processing function {}", fd.getName());

		// Work list of following EOG nodes. Not all EOG nodes will result in a WPDS rule, though.
		var worklist = new ArrayDeque<Pair<de.fraunhofer.aisec.cpg.graph.Node, Set<Stmt>>>();
		worklist.add(new Pair<>(fd, Set.of(new Stmt(fd.getName(), Utils.getRegion(fd)))));

		var skipTheseValsAtStmt = new HashMap<Stmt, Val>();
		var valsInScope = new HashSet<Val>();

		// Make sure we track all parameters inside this function
		var params = fd.getParameters();
		for (ParamVariableDeclaration p : params) {
			valsInScope.add(new Val(p.getName(), fd.getName()));
		}

		// Start creation of WPDS rules by traversing the EOG
		while (!worklist.isEmpty()) {
			var currentPair = worklist.pop();
			var v = currentPair.getFirst();

			for (Stmt previousStmt : currentPair.getSecond()) {
				// We consider only "Statements" and CallExpressions in the EOG
				if (isRelevantStmt(v)) {
					createRulesForStmt(wpds, fd, previousStmt, v, valsInScope, skipTheseValsAtStmt, tsNfa);
				} // End isRelevantStmt()
			}

			// Add successors to work list
			var successors = getSuccessors(v, alreadySeen);
			for (var succ : successors) {
				if (isRelevantStmt(v)) {
					worklist.add(new Pair<>(succ, Set.of(vertexToStmt(v))));
				} else {
					worklist.add(new Pair<>(succ, currentPair.getSecond()));
				}
			}
		}
		return wpds;
	}

	@SuppressWarnings("squid:S107")
	private void createRulesForStmt(@NonNull WPDS<Stmt, Val, TypestateWeight> wpds,
			@NonNull FunctionDeclaration functionVertex,
			@NonNull Stmt previousStmt,
			de.fraunhofer.aisec.cpg.graph.Node stmtNode,
			@NonNull Set<Val> valsInScope,
			@NonNull Map<Stmt, Val> skipTheseValsAtStmt,
			@NonNull NFA tsNfa) {
		String currentFunctionName = functionVertex.getName();
		Stmt currentStmt = vertexToStmt(stmtNode);

		/* First we create normal rules from previous stmt to the current stmt, simply propagating existing values. */
		Set<NormalRule<Stmt, Val, TypestateWeight>> normalRules = createNormalRules(previousStmt, stmtNode, valsInScope, tsNfa);
		for (NormalRule<Stmt, Val, TypestateWeight> normalRule : normalRules) {
			/*
			  If this is a call into a known method, we skip immediate propagation. In that case, data flows *into* the method.
			 */
			boolean skipIt = shouldBeSkipped(normalRule, skipTheseValsAtStmt);
			if (!skipIt) {
				wpds.addRule(normalRule);
			}
		}

		/*
		  Handle calls into known methods (not a "phantom" method) by creating push rule.
		 */
		if (stmtNode instanceof CallExpression && !Utils.isPhantom((CallExpression) stmtNode)) {
			CallExpression callE = (CallExpression) stmtNode;
			/*
			 * For calls to functions whose body is known, we create push/pop rule pairs. All arguments flow into the parameters of the function. The
			 * "return site" is the statement to which flow returns after the function call.
			 */
			Set<PushRule<Stmt, Val, TypestateWeight>> pushRules = createPushRules(callE, currentFunctionName, currentStmt, stmtNode);
			for (PushRule<Stmt, Val, TypestateWeight> pushRule : pushRules) {
				log.debug("  Adding push rule: {}", pushRule);
				wpds.addRule(pushRule);

				// Remember that arguments flow only into callee and do not bypass it.
				skipTheseValsAtStmt.put(pushRule.getCallSite(), pushRule.getS1());
			}
		} else if (stmtNode instanceof VariableDeclaration) {
			// Add declVal to set of currently tracked variables
			VariableDeclaration decl = (VariableDeclaration) stmtNode;
			Val declVal = new Val(decl.getName(), currentFunctionName);
			valsInScope.add(declVal);
		} else if (stmtNode instanceof DeclarationStatement) {
			/* Handle declaration of new variables.
			 "DeclarationStatements" result in a normal rule, assigning rhs to lhs.
			*/

			// Note: We might be a bit more gracious here to tolerate incorrect code. For example, a non-declared variable would be a "BinaryOperator".
			log.debug("Found variable declaration {}", stmtNode.getCode());
			DeclarationStatement ds = (DeclarationStatement) stmtNode;
			for (Declaration decl : ds.getDeclarations()) {
				if (!(decl instanceof VariableDeclaration)) {
					continue;
				}
				Val declVal = new Val(decl.getName(), currentFunctionName);
				Expression rhs = ((VariableDeclaration) decl).getInitializer();

				if (rhs instanceof CallExpression) {
					/* Handle function/method calls whose return value is assigned to a declared variable.
					   A new data flow for the declared variable (declVal) is introduced.
					 */
					Rule<Stmt, Val, TypestateWeight> normaleRuleDeclared = new NormalRule<>(new Val(CpgWpds.EPSILON, currentFunctionName),
						previousStmt,
						declVal,
						currentStmt,
						TypestateWeight.one());
					log.debug("Adding normal rule for declaration {}", normaleRuleDeclared);
					wpds.addRule(normaleRuleDeclared);

					// Add declVal to set of currently tracked variables
					valsInScope.add(declVal);
				} else if (rhs != null) {
					/**
					 * Handle assignment from right side (rhs) to left side (lhs).
					 *
					 * We simply take rhs.getName() as a data source. This might be imprecise and need further differentiation. For instance, if rhs is an expression (other than CallExpression), we might want to recursively handle data flows within that expression. This is currently not implemented as it is not needed for our use case and would add unneeded complexity.
					 */
					Val rhsVal = new Val(rhs.getName(), currentFunctionName);

					// Add declVal to set of currently tracked variables
					valsInScope.add(declVal);
					Rule<Stmt, Val, TypestateWeight> normalRulePropagate = new NormalRule<>(rhsVal, previousStmt, declVal, currentStmt,
						TypestateWeight.one());
					log.debug("Adding normal rule for assignment {}", normalRulePropagate);
					wpds.addRule(normalRulePropagate);
				}
			}
		} else if (stmtNode instanceof ReturnStatement) {
			/* Return statements result in pop rules */
			ReturnStatement returnV = (ReturnStatement) stmtNode;
			if (!returnV.isDummy()) {
				Set<Val> returnedVals = findReturnedVals(stmtNode);

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
				Map<String, Set<Pair<Val, Val>>> paramToValueMap = findParamToValues(functionVertex);
				if (paramToValueMap.containsKey(currentFunctionName)) {
					for (Pair<Val, Val> pToA : paramToValueMap.get(currentFunctionName)) {
						PopRule<Stmt, Val, TypestateWeight> popRule = new PopRule<>(pToA.getFirst(), currentStmt, pToA.getSecond(),
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

		} // End isReturnStatement
	}

	private boolean shouldBeSkipped(NormalRule<Stmt, Val, TypestateWeight> normalRule,
			Map<Stmt, Val> skipTheseValsAtStmt) {
		boolean skipIt = false;
		if (skipTheseValsAtStmt.get(normalRule.getL2()) != null) {
			Val forbiddenVal = skipTheseValsAtStmt.get(normalRule.getL2());
			if (!normalRule.getS1()
					.equals(forbiddenVal)) {
				skipIt = true;
			}
		}
		return skipIt;
	}

	/**
	 * Returns a set of Vertices which are successors of <code>v</code> in the EOG and are not contained in <code>alreadySeen</code>.
	 *
	 * @param n
	 * @param alreadySeen
	 * @return
	 */
	private HashSet<de.fraunhofer.aisec.cpg.graph.Node> getSuccessors(final de.fraunhofer.aisec.cpg.graph.Node n,
			@NonNull final HashSet<de.fraunhofer.aisec.cpg.graph.Node> alreadySeen) {
		var unseenSuccessors = new HashSet<de.fraunhofer.aisec.cpg.graph.Node>();

		for (de.fraunhofer.aisec.cpg.graph.Node succ : n.getNextEOG()) {
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
	 * @param node
	 * @return
	 */
	private boolean isRelevantStmt(de.fraunhofer.aisec.cpg.graph.Node node) {
		int numberOfOutgoingEogs = node.getNextEOG().size();

		return node instanceof CallExpression
				|| node instanceof DeclarationStatement
				|| node instanceof VariableDeclaration
				|| node instanceof IfStatement
				|| getAstParent(node) instanceof CompoundStatement
				|| numberOfOutgoingEogs >= 2;
	}

	private Set<NormalRule<Stmt, Val, TypestateWeight>> createNormalRules(final Stmt previousStmt, final de.fraunhofer.aisec.cpg.graph.Node v, final Set<Val> valsInScope,
			final NFA tsNfa) {
		var currentStmt = vertexToStmt(v);

		Set<NormalRule<Stmt, Val, TypestateWeight>> result = new HashSet<>();

		// Create normal rule. Flow remains where it is.
		for (Val valInScope : valsInScope) {
			// Determine weight
			Set<NFATransition<Node>> relevantNFATransitions = tsNfa.getTransitions()
					.stream()
					.filter(
						tran -> triggersTypestateTransition(v, tran.getTarget().getBase(), tran.getTarget().getOp()))
					.collect(Collectors.toSet());
			TypestateWeight weight = relevantNFATransitions.isEmpty() ? TypestateWeight.one() : new TypestateWeight(relevantNFATransitions);

			NormalRule<Stmt, Val, TypestateWeight> normalRule = new NormalRule<>(valInScope, previousStmt, valInScope, currentStmt, weight);
			log.debug("Adding normal rule {}", normalRule);
			result.add(normalRule);
		}

		return result;
	}

	/**
	 * Returns true if the given CPG {@code Node} will result in a transition from any typestate into the typestate detened by {@code op}.
	 *
	 * @param cpgNode A CPG node - typically a {@code CallExpression} or anything that contains a call (e.g., a {@code VariableDeclaration})
	 * @param markInstance The current MARK instance.
	 * @param op The target typestate, indicated by a MARK op.
	 * @return
	 */
	private boolean triggersTypestateTransition(de.fraunhofer.aisec.cpg.graph.Node cpgNode, @Nullable String markInstance, String op) {
		/*
		 TODO Future improvement: This method is repeatedly called for different "ops" and thus repeats quite some work.
		 The "op" paramenter should be removed and the method should return a (possibly empty) set of target typestates (=ops)
		 that would be reached, so this method needs to be called only once per CPG node.
		 */
		if (markInstance == null || cpgNode.getName().equals("")) {
			return false;
		}

		// Get the MARK entity of the markInstance
		Pair<String, MEntity> mEntity = this.rule.getEntityReferences().get(markInstance);
		if (mEntity == null || mEntity.getSecond() == null) {
			return false;
		}

		// For non-OO languages, we need to check valInScope against function args and return value (=assignee)
		String assigneeVar = null;
		String assignerFqn = null;
		if (cpgNode instanceof VariableDeclaration) {
			assigneeVar = cpgNode.getName();
			if (((VariableDeclaration) cpgNode).getInitializer() instanceof CallExpression) {
				assignerFqn = ((CallExpression) ((VariableDeclaration) cpgNode).getInitializer()).getFqn();
			}
		} else if (cpgNode instanceof CallExpression) {
			assignerFqn = ((CallExpression) cpgNode).getFqn();
		}

		// For method calls we collect the "base", its type(s), and the method arguments.
		Set<Type> types = new HashSet<>();
		List<Expression> arguments = new ArrayList<>();
		if (cpgNode instanceof CallExpression) {
			var base = ((CallExpression) cpgNode).getBase();

			// even though base is annotated @NotNull, it sometimes is null
			if (base != null) {
				types = base.getPossibleSubTypes();
			}

			arguments.addAll(((CallExpression) cpgNode).getArguments());
		}

		for (MOp o : mEntity.getSecond().getOps()) {
			if (!op.equals(o.getName())) {
				continue;
			}
			for (OpStatement opStatement : o.getStatements()) {
				if (types.isEmpty()) {
					/* Failure to resolve type or a function call (e.g. EVP_EncryptInit), rather than a method call.
					   In case of function calls/non-OO languages, we ignore the type of the (non-existing) base
					   and simply check if the call stmt matches the one in the "op" spec.
					   "Matches" means that it matches the function and valInScope is either one of the arguments or is assigned the call's return value.
					 */
					if ((assignerFqn != null && opStatement.getCall().getName().equals(assignerFqn))
							&& (assigneeVar != null // is return value assigned to valInScope?
									|| arguments.isEmpty()
									|| argumentsMatchParameters(opStatement.getCall().getParams(),
										((CallExpression) cpgNode).getArguments()))) {
						return true;
					}
				} else {
					for (Type type : types) {
						if (type.getTypeName().startsWith(Utils.getScope(opStatement.getCall().getName()).replace("::", ".")) // Dirty: startsWith() to ignore modifiers (such as "*").
								&& opStatement.getCall()
										.getName()
										.endsWith(cpgNode.getName())) {
							// TODO should rather compare fully qualified names instead of "endsWith"
							return true;
						}
					}
				}
			}
		}

		return false;
	}

	/**
	 * Finds the mapping from function parameters to arguments of calls to this method. This is needed for later construction of pop rules.
	 *
	 * @param callee
	 * @return
	 */
	@NonNull
	private Map<String, Set<Pair<Val, Val>>> findParamToValues(FunctionDeclaration callee) {
		Map<String, Set<Pair<Val, Val>>> result = new HashMap<>();

		if (callee == null) {
			log.error("Unexpected: FunctionDeclaration of callee is null.");
			return result;
		}
		String calleeName = callee.getName();

		// the function declaration is connected to their call expressions by a DFG edge
		var calls = callee.getNextDFG()
				.stream()
				.filter(CallExpression.class::isInstance)
				.map(CallExpression.class::cast)
				.collect(Collectors.toList());

		for (var ce : calls) {
			var caller = getContainingFunction(ce);

			if (caller == null) {
				log.error("Unexpected: Null Node object for FunctionDeclaration");
				continue;
			}

			var args = ce.getArguments();
			var params = callee.getParameters();

			var pToA = new HashSet<Pair<Val, Val>>();
			for (int i = 0; i < Math.min(params.size(), args.size()); i++) {
				pToA.add(new Pair<>(new Val(params.get(i).getName(), calleeName), new Val(args.get(i).getName(), caller.getName())));
			}

			result.put(calleeName, pToA);
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
	 * @param node
	 * @return
	 */
	private Set<Val> findReturnedVals(de.fraunhofer.aisec.cpg.graph.Node node) {
		/*
		 * Follow along "DFG" edges from the return statement to the CallExpression that initiated the call. Then check if there is a "DFG" edge from that CallExpression
		 * to a VariableDeclaration.
		 */
		Set<Val> returnedVals = new HashSet<>();
		var calls = node.getNextDFG().stream().filter(CallExpression.class::isInstance).collect(Collectors.toList());

		for (var call : calls) {
			// We found the call site into our method. Now see if the return value is used.
			var nextDfgAftercall = call.getNextDFG().stream().findFirst();
			String returnVar = "";
			if (nextDfgAftercall.isPresent()) {
				if (nextDfgAftercall.get() instanceof VariableDeclaration
						|| nextDfgAftercall.get() instanceof DeclaredReferenceExpression) {
					// return value is used. Remember variable name.
					returnVar = nextDfgAftercall.get().getName();
				}

				// Finally we need to find out in which function the call site actually is
				String callerFunctionName = null;
				var callerFunction = getContainingFunction(call);
				if (callerFunction != null) {
					callerFunctionName = callerFunction.getName();
				}

				if (callerFunctionName != null) {
					returnedVals.add(new Val(returnVar, callerFunctionName));
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
	 * @param currentFunctionName
	 * @param currentStmt
	 * @param currentStmtVertex
	 * @return
	 */
	private Set<PushRule<Stmt, Val, TypestateWeight>> createPushRules(CallExpression mce, String currentFunctionName,
			Stmt currentStmt, de.fraunhofer.aisec.cpg.graph.Node currentStmtVertex) {
		// Return site(s). Actually, multiple return sites will only occur in case of exception handling.
		// TODO: support multiple return sites
		var returnSite = getNextStatement(currentStmtVertex);
		List<Statement> returnSites;
		if (returnSite == null) {
			returnSites = Collections.emptyList();
		} else {
			returnSites = Collections.singletonList(returnSite);
		}

		// Arguments of function call
		var argVals = argumentsToVals(mce, currentFunctionName);

		Set<PushRule<Stmt, Val, TypestateWeight>> pushRules = new HashSet<>();
		for (FunctionDeclaration potentialCallee : mce.getInvokes()) {
			// Parameters of function
			if (potentialCallee.getParameters().size() != argVals.size()) {
				log.warn("Skipping call from {} to {} due different argument/parameter counts.", currentFunctionName,
					potentialCallee.getSignature());
				continue;
			}
			var parmVals = parametersToVals(potentialCallee);

			// Get first statement of callee. This is the jump target of our Push Rule.
			var firstStmt = getFirstStmtOfMethod(potentialCallee);

			if (firstStmt != null && firstStmt.getCode() != null) {
				for (int i = 0; i < argVals.size(); i++) {
					for (var returnSiteVertex : returnSites) {
						Stmt stmt = vertexToStmt(returnSiteVertex);

						PushRule<Stmt, Val, TypestateWeight> pushRule = new PushRule<>(
							argVals.get(i),
							currentStmt,
							parmVals.get(i),
							new Stmt(potentialCallee.getName(), Utils.getRegion(potentialCallee)),
							stmt,
							TypestateWeight.one()); // A push rule does not trigger any typestate transitions.
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

	private List<Val> argumentsToVals(CallExpression callExpression, String currentFunctionName) {
		List<Val> argVals = new ArrayList<>();
		List<Expression> args = callExpression.getArguments();
		for (Expression arg : args) {
			argVals.add(new Val(arg.getName(), currentFunctionName));
		}
		return argVals;
	}

	/**
	 * Convert a CPG node into a <code>Stmt</code> in context of the WPDS.
	 *
	 * @param n CPG node
	 * @return A <code>Stmt</code>, holding the "code" and "location->region" properties of <code>n</code>>.
	 */
	@NotNull
	private Stmt vertexToStmt(@NotNull de.fraunhofer.aisec.cpg.graph.Node n) {
		var region = Utils.getRegion(n);

		return new Stmt(
			n.getCode(),
			region);
	}
}
