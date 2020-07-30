
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
import de.fraunhofer.aisec.crymlin.dsl.CrymlinConstants;
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
import static de.fraunhofer.aisec.crymlin.dsl.CrymlinConstants.*;
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
	@NonNull
	private final MarkContextHolder markContextHolder;
	private CPGInstanceContext instanceContext;

	@NonNull
	private OverflowDatabase<de.fraunhofer.aisec.cpg.graph.Node> odb = OverflowDatabase.getInstance();

	public TypeStateAnalysis(@NonNull MarkContextHolder markContextHolder) {
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

		odb = OverflowDatabase.getInstance();

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

		/* Create an initial automaton corresponding to the start configurations of the data flow analysis.
		   In this case, we are starting with all statements that initiate a type state transition.
		 */
		WeightedAutomaton<Stmt, Val, TypestateWeight> wnfa = InitialConfiguration.create(InitialConfiguration::FIRST_TYPESTATE_EVENT, wpds);

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
				wpdsConfigs.add(new NonNullPair<>(r.getL1(), r.getS1()));
				wpdsConfigs.add(new NonNullPair<>(r.getL2(), r.getS2()));
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
						potentialGoodFindings.add(new NonNullPair<>(tran.getLabel(), tran.getStart()));
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

		/* Create empty WPDS */
		CpgWpds wpds = new CpgWpds();

		/**
		 * For each function, create a WPDS.
		 *
		 * The (normal, push, pop) rules of the WPDS reflect the data flow, similar to a static taint analysis.
		 *
		 */
		for (Vertex functionDeclaration : crymlinTraversal.functions().toList()) {
			WPDS<Stmt, Val, TypestateWeight> funcWpds = createWpds(functionDeclaration, tsNfa, crymlinTraversal);
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
	 * @param crymlinTraversal
	 * @return
	 */
	private WPDS<Stmt, Val, TypestateWeight> createWpds(@NonNull Vertex fdVertex, NFA tsNfa, CrymlinTraversalSource crymlinTraversal) {
		// To remember already visited nodes and avoid endless iteration
		HashSet<Vertex> alreadySeen = new HashSet<>();

		// the WPDS we are creating here
		CpgWpds wpds = new CpgWpds();

		FunctionDeclaration fd = (FunctionDeclaration) odb.vertexToNode(fdVertex);
		log.info("Processing function {}", fdVertex.property(NAME).orElse(""));

		// Work list of following EOG nodes. Not all EOG nodes will result in a WPDS rule, though.
		ArrayDeque<NonNullPair<Vertex, Set<Stmt>>> worklist = new ArrayDeque<>();
		worklist.add(new NonNullPair<>(fdVertex, Set.of(new Stmt(fd.getName(), getRegion(fd)))));

		Map<Stmt, Val> skipTheseValsAtStmt = new HashMap<>();
		Set<Val> valsInScope = new HashSet<>();

		// Make sure we track all parameters inside this function
		List<ParamVariableDeclaration> params = fd.getParameters();
		for (ParamVariableDeclaration p : params) {
			valsInScope.add(new Val(p.getName(), fd.getName()));
		}

		// Start creation of WPDS rules by traversing the EOG
		while (!worklist.isEmpty()) {
			NonNullPair<Vertex, Set<Stmt>> currentPair = worklist.pop();
			Vertex v = currentPair.getValue0();

			for (Stmt previousStmt : currentPair.getValue1()) {
				// We consider only "Statements" and CallExpressions in the EOG
				if (isRelevantStmt(v)) {
					createRulesForStmt(wpds, fdVertex, previousStmt, v, valsInScope, skipTheseValsAtStmt, tsNfa, crymlinTraversal);
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
		return wpds;
	}

	@java.lang.SuppressWarnings("squid:S107")
	private void createRulesForStmt(@NonNull WPDS<Stmt, Val, TypestateWeight> wpds,
			@NonNull Vertex functionVertex,
			@NonNull Stmt previousStmt,
			@NonNull Vertex currentStmtVertex,
			@NonNull Set<Val> valsInScope,
			@NonNull Map<Stmt, Val> skipTheseValsAtStmt,
			@NonNull NFA tsNfa,
			@NonNull CrymlinTraversalSource crymlinTraversal) {

		String currentFunctionName = (String) functionVertex.property(NAME).orElse("UNKNOWN");
		Stmt currentStmt = vertexToStmt(currentStmtVertex);
		Statement stmtNode = (Statement) odb.vertexToNode(currentStmtVertex);

		/* First we create normal rules from previous stmt to the current stmt, simply propagating existing values. */
		Set<NormalRule<Stmt, Val, TypestateWeight>> normalRules = createNormalRules(previousStmt, currentStmtVertex, valsInScope, tsNfa);
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
		if (isCallExpression(currentStmtVertex) && !isPhantom((CallExpression) stmtNode)) {
			CallExpression callE = (CallExpression) stmtNode;
			/*
			 * For calls to functions whose body is known, we create push/pop rule pairs. All arguments flow into the parameters of the function. The
			 * "return site" is the statement to which flow returns after the function call.
			 */
			Set<PushRule<Stmt, Val, TypestateWeight>> pushRules = createPushRules(callE, crymlinTraversal, currentFunctionName, tsNfa,
				currentStmt,
				currentStmtVertex);
			for (PushRule<Stmt, Val, TypestateWeight> pushRule : pushRules) {
				log.debug("  Adding push rule: {}", pushRule);
				wpds.addRule(pushRule);

				// Remember that arguments flow only into callee and do not bypass it.
				skipTheseValsAtStmt.put(pushRule.getCallSite(), pushRule.getS1());
			}
		} else if (isDeclarationStatement(currentStmtVertex)) {
			/* Handle declaration of new variables.
			 "DeclarationStatements" result in a normal rule, assigning rhs to lhs.
			*/

			// Note: We might be a bit more gracious here to tolerate incorrect code. For example, a non-declared variable would be a "BinaryOperator".
			log.debug("Found variable declaration {}", currentStmtVertex.property("code")
					.orElse(""));
			DeclarationStatement ds = (DeclarationStatement) odb.vertexToNode(currentStmtVertex);
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
		} else if (isReturnStatement(currentStmtVertex)) {
			/* Return statements result in pop rules */
			ReturnStatement returnV = (ReturnStatement) odb.vertexToNode(currentStmtVertex);
			if (returnV != null && !returnV.isDummy()) {
				Set<Val> returnedVals = findReturnedVals(crymlinTraversal, currentStmtVertex);

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
				Map<String, Set<Pair<Val, Val>>> paramToValueMap = findParamToValues(functionVertex, currentStmtVertex, odb, crymlinTraversal);
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
		return isCallExpression(v) || Utils.hasLabel(v, IfStatement.class) || v.edges(Direction.IN, CrymlinConstants.STATEMENTS).hasNext() || numberOfOutgoingEogs >= 2;
	}

	private boolean isReturnStatement(Vertex v) {
		return v.label().equals(ReturnStatement.class.getSimpleName());
	}

	private boolean isDeclarationStatement(Vertex v) {
		return Utils.hasLabel(v, DeclarationStatement.class);
	}

	private Set<NormalRule<Stmt, Val, TypestateWeight>> createNormalRules(final Stmt previousStmt, final Vertex v, final Set<Val> valsInScope, final NFA tsNfa) {
		Stmt currentStmt = vertexToStmt(v);

		Statement currentStmtNode = (Statement) odb.vertexToNode(v);

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

		// Note: this method is called a few times and repeats some work. Potential for caching/optimization.

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
						.until(__().in(CrymlinConstants.STATEMENTS))
						//					.limit(10)
						.in(CrymlinConstants.STATEMENTS)
						.in(CrymlinConstants.BODY);
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
	 * @param currentStmt
	 * @param currentStmtVertex
	 * @return
	 */
	private Set<PushRule<Stmt, Val, TypestateWeight>> createPushRules(CallExpression mce, CrymlinTraversalSource crymlinTraversal, String currentFunctionName,
			NFA nfa, Stmt currentStmt, Vertex currentStmtVertex) {
		Set<NFATransition<Node>> relevantNFATransitions = nfa.getTransitions()
				.stream()
				.filter(
					tran -> belongsToOp(mce.getName(), tran.getTarget().getBase(), tran.getTarget().getOp()))
				.collect(Collectors.toSet());
		TypestateWeight weight = relevantNFATransitions.isEmpty() ? TypestateWeight.one() : new TypestateWeight(relevantNFATransitions);

		// Return site(s). Actually, multiple return sites will only occur in case of exception handling.
		List<Vertex> returnSites = CrymlinQueryWrapper.getNextStatements(crymlinTraversal, (long) currentStmtVertex.id());

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
	 * Convert a CPG vertex into a <code>Stmt</code> in context of the WPDS.
	 *
	 * @param v CPG vertex
	 * @return A <code>Stmt</code>, holding the "code" and "location->region" properties of <code>v</code>>.
	 */
	@NonNull
	private Stmt vertexToStmt(@NonNull Vertex v) {
		Region region = new Region(-1, -1, -1, -1);
		if (v.property(START_LINE).isPresent() &&
				v.property(START_COLUMN).isPresent() &&
				v.property(END_LINE).isPresent() &&
				v.property(END_COLUMN).isPresent()) {
			region = new Region(
				toIntExact((long) v.property(START_LINE)
						.value()),
				toIntExact((long) v.property(START_COLUMN)
						.value()),
				toIntExact((long) v.property(END_LINE)
						.value()),
				toIntExact((long) v.property(END_COLUMN)
						.value()));
		}
		return new Stmt(
			v.property("code")
					.orElse("")
					.toString(),
			region);
	}
}
