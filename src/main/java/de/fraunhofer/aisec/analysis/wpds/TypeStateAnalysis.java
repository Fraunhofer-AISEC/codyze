
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
import de.fraunhofer.aisec.cpg.helpers.Benchmark;
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
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

import static de.fraunhofer.aisec.crymlin.CrymlinQueryWrapper.isCallExpression;
import static de.fraunhofer.aisec.crymlin.dsl.__.__;
import static java.lang.Math.toIntExact;

/**
 * Implementation of a WPDS-based typestate analysis using the code property graph.
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

	public TypeStateAnalysis(MarkContextHolder markContextHolder) {
		this.markContextHolder = markContextHolder;
	}

	public ConstantValue analyze(OrderExpression orderExpr, Integer contextID, AnalysisContext ctx,
			CrymlinTraversalSource crymlinTraversal,
			MRule rule) throws IllegalTransitionException {
		log.info("Typestate analysis starting for {} and {}", ctx, crymlinTraversal);

		CPGInstanceContext instanceContext = markContextHolder.getContext(contextID).getInstanceContext();

		this.rule = rule;

		// Remember map from MARK ops to Vertices
		Map<MOp, Set<Vertex>> verticeMap = getVerticesOfRule(rule);

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

		// Creating a WPDS from CPG, starting at seeds. Note that this will neglect alias which have been defined before the seed.
		HashSet<Node> seedExpression = null; // TODO Seeds must be vertices with calls which MAY be followed by a typestate violation

		/* Create typestate NFA, representing the regular expression of a MARK typestate rule. */
		NFA tsNFA = NFA.of(orderExpr.getExp());
		log.debug("Initial typestate NFA:\n{}", tsNFA);

		// Create a weighted pushdown system
		CpgWpds wpds = createWpds(seedExpression, verticeMap, crymlinTraversal, tsNFA);

		/*
		 * Create a weighted automaton (= a weighted NFA) that describes the initial configurations. We use the whole current function as an initial configuration. The
		 * "current function" is the function containing the declaration of the program variable (e.g., "x = Botan2()") that corresponds to the current Mark instance
		 * (e.g., "b").
		 */
		Vertex v = instanceContext.getVertex(markInstance);
		if (v == null) {
			log.error("No vertex found for Mark instance: {}. Will not run TS analysis", markInstance);
			return ErrorValue.newErrorValue(String.format("No vertex found for Mark instance: %s. Will not run TS analysis", markInstance));
		}

		// Find the function in which the vertex is located, so we can use the first statement in function as a start
		Optional<Vertex> containingFunctionOpt = CrymlinQueryWrapper.getContainingFunction(v, crymlinTraversal);
		if (containingFunctionOpt.isEmpty()) {
			log.error("Vertex {} not located within a function. Cannot start TS analysis for rule {}", v.property("code").orElse(""), rule);
			return ErrorValue.newErrorValue(String.format("Vertex %s not located within a function. Cannot start TS analysis for rule %s", v.property("code").orElse(""),
				rule.toString()));
		}

		// Turn function vertex into a FunctionDeclaration so we can work with it
		FunctionDeclaration funcDecl = (FunctionDeclaration) OverflowDatabase.getInstance()
				.vertexToNode(containingFunctionOpt.get());
		if (funcDecl == null) {
			log.error("Function {} could not be retrieved as a FunctionDeclaration. Cannot start TS analysis for rule {}",
				containingFunctionOpt.get().property("name").orElse(""), rule);
			return ErrorValue.newErrorValue(String.format("Function %s could not be retrieved as a FunctionDeclaration. Cannot start TS analysis for rule %s",
				containingFunctionOpt.get().property("name").orElse(""), rule.toString()));
		}

		WeightedAutomaton<Stmt, Val, Weight> wnfa = createInitialConfiguration("EPSILON", funcDecl, tsNFA, wpds);

		// For debugging only: Print WPDS rules
		for (Rule r : wpds.getAllRules()
				.stream()
				.sorted(Comparator.comparing(r -> r.getL1().getRegion().getStartLine()))
				.sorted(Comparator.comparing(r -> r.getL1().getRegion().getStartColumn()))
				.collect(Collectors.toList())) {
			log.debug("rule: {}", r);
		}

		// For debugging only: Print the non-saturated NFA
		log.debug("Non saturated NFA {}", wnfa);
		log.debug(wnfa.toDotString());

		// Saturate the NFA from the WPDS, using the post-* algorithm.
		wpds.poststar(wnfa);

		// For debugging only: Print the post-*-saturated NFA
		log.debug("Saturated WNFA {}", wnfa);
		log.debug("Saturated WNFA {}", wnfa.toDotString());

		// Evaluate saturated WNFA for any MARK violations
		Set<Finding> findings = getFindingsFromWpds(wpds, wnfa, tsNFA, rule, funcDecl.getFile());

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
	 * @param tsNFA The typestate NFA
	 * @param rule The MARK rule to check
	 * @param currentFile
	 * @return
	 */
	@NonNull
	private Set<Finding> getFindingsFromWpds(CpgWpds wpds, @NonNull WeightedAutomaton<Stmt, Val, Weight> wnfa, NFA tsNFA, MRule rule,
			String currentFile) {
		Set<Finding> findings = new HashSet<>();
		for (Transition<Stmt, Val> tran : wnfa.getTransitions()) {
			Weight w = wnfa.getWeightFor(tran);
			if (w.value() instanceof Set) {
				Set<NFATransition<Node>> reachableTypestates = (Set<NFATransition<Node>>) w.value();
				for (NFATransition<Node> reachableTypestate : reachableTypestates) {
					if (reachableTypestate.getTarget().isError()) {
						findings.add(createBadFinding(tran, currentFile));
					}
				}
			}
		}

		return findings;
	}

	/**
	 * Creates a finding indicating a typestate error in the program.
	 *
	 * @param t
	 * @param currentFile
	 * @param expected
	 * @return
	 */
	private Finding createBadFinding(@NonNull Transition<Stmt, Val> t, @NonNull String currentFile, @NonNull Collection<NFATransition<Node>> expected) {
		String name = "Invalid typestate of variable " + t.getStart() + " at statement: " + t.getLabel() + " . Violates order of " + rule.getName();
		if (!expected.isEmpty()) {
			name += " Expected one of " + String.join(", ", expected.stream().map(tran -> tran.toString()).collect(Collectors.toList()));
		} else {
			name += " Expected no further operations.";
		}

		// lines are human-readable, i.e., off-by-one
		int startLine = toIntExact(t.getLabel().getRegion().getStartLine()) - 1;
		int endLine = toIntExact(t.getLabel().getRegion().getEndLine()) - 1;
		int startColumn = toIntExact(t.getLabel().getRegion().getStartColumn()) - 1;
		int endColumn = toIntExact(t.getLabel().getRegion().getEndColumn()) - 1;
		return new Finding(name, rule.getErrorMessage(), currentFile, startLine, endLine, startColumn, endColumn);
	}

	private Finding createBadFinding(Transition<Stmt, Val> t, String currentFile) {
		return createBadFinding(t, currentFile, List.of());
	}

	/**
	 * Create a "non-finding" (i.e. positive confirmation)
	 * Lines are human-readable, i.e., off-by-one.
	 *
	 * @param t
	 * @param currentFile
	 * @return
	 */
	private Finding createGoodFinding(Transition<Stmt, Val> t, String currentFile) {
		int startLine = toIntExact(t.getLabel().getRegion().getStartLine()) - 1;
		int endLine = toIntExact(t.getLabel().getRegion().getEndLine()) - 1;
		int startColumn = toIntExact(t.getLabel().getRegion().getStartColumn()) - 1;
		int endColumn = toIntExact(t.getLabel().getRegion().getEndColumn()) - 1;
		return new Finding("Good: " + t.getStart() + " at " + t.getLabel(), rule.getErrorMessage(), currentFile,
			List.of(new Range(new Position(startLine, endLine), new Position(startColumn, endColumn))), false);
	}

	/**
	 * Creates a weighted pushdown system (WPDS), linked to a typestate NFA.
	 * <p>
	 * When populating the WPDS using post-* algorithm, the result will be an automaton capturing the reachable type states.
	 *
	 * @param seedExpressions
	 * @param verticeMap
	 * @param crymlinTraversal
	 * @param tsNfa
	 * @return
	 * @throws IllegalTransitionException
	 */
	private CpgWpds createWpds(@Nullable HashSet<Node> seedExpressions, Map<MOp, Set<Vertex>> verticeMap, CrymlinTraversalSource crymlinTraversal, NFA tsNfa) {
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
			// Work list of following EOG nodes. Not all EOG nodes will result in a WPDS rule, though.
			FunctionDeclaration fd = (FunctionDeclaration) odb.vertexToNode(functionDeclaration);
			if (fd == null) {
				log.error("Unexpected: Got FunctionDeclaration vertex but could not convert to Node object: {}", functionDeclaration);
				continue;
			}
			String currentFunctionName = fd.getName();
			log.info("Processing function {}", currentFunctionName);
			ArrayDeque<Vertex> worklist = new ArrayDeque<>();
			worklist.add(functionDeclaration);
			Stmt previousStmt = new Stmt(fd.getName(), fd.getRegion());
			Map<Stmt, Val> skipTheseValsAtStmt = new HashMap<>();

			Set<Val> valsInScope = new HashSet<>();

			// Make sure we track all parameters inside this function
			List<ParamVariableDeclaration> params = fd.getParameters();
			for (ParamVariableDeclaration p : params) {
				valsInScope.add(new Val(p.getName(), currentFunctionName));
			}
			while (!worklist.isEmpty()) {
				Vertex v = worklist.pop();
				log.debug("TYPE: {}", v.property("labels").value());
				String code = (String) v.property("code").orElse("");
				log.debug("CODE: {}", code);

				// We consider only "Statements" and CallExpressions in the EOG
				if (isCallExpression(v) || v.edges(Direction.IN, "STATEMENTS").hasNext()) {

					Stmt currentStmt = new Stmt(
						v.property("code").orElse("").toString(),
						new Region(
							toIntExact((long) v.property("startLine").value()),
							toIntExact((long) v.property("startColumn").value()),
							toIntExact((long) v.property("endLine").value()),
							toIntExact((long) v.property("startColumn").value())));

					if (isCallExpression(v)) {

						CallExpression mce = (CallExpression) odb.vertexToNode(v);
						if (mce != null) {

							/* First we create a normal rule from previous stmt to the current (=the call) */
							Set<NormalRule<Stmt, Val, Weight>> normalRules = createNormalRules(tsNfa, mce, previousStmt, currentStmt, valsInScope);
							for (NormalRule<Stmt, Val, Weight> normalRule : normalRules) {
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
							previousStmt = currentStmt;

							if (!isPhantom(mce)) {
								/*
								 * For calls to functions whose body is known, we create push/pop rule pairs. All arguments flow into the parameters of the function. The
								 * "return site" is the statement to which flow returns after the function call.
								 */
								Set<PushRule<Stmt, Val, Weight>> pushRules = createPushRules(mce, crymlinTraversal, currentFunctionName, tsNfa, previousStmt, currentStmt,
									v, worklist);
								for (PushRule<Stmt, Val, Weight> pushRule : pushRules) {
									log.debug("  Adding push rule: {}", pushRule);
									wpds.addRule(pushRule);

									// Remember that arguments flow only into callee and do not bypass it.
									skipTheseValsAtStmt.put(pushRule.getCallSite(), pushRule.getS1());
									previousStmt = pushRule.getCallSite(); // Previous stmt is return location
								}
							}

						}
						/* "DeclarationStatements" result in a normal rule, assigning rhs to lhs. */
					}

					Iterator<Edge> declarations = v.edges(Direction.OUT, "DECLARATIONS");
					if (isDeclarationStatement(v) && declarations.hasNext()) {
						log.debug("Found variable declaration {}", v.property("code").orElse(""));

						Vertex decl = declarations.next().inVertex();
						Val declVal = new Val((String) decl.property("name").value(), currentFunctionName);

						Optional<Vertex> rhs = Optional.empty();
						if (decl.edges(Direction.OUT, "INITIALIZER").hasNext()) {
							// TODO Do not simply assume that the target of an INITIALIZER edge is a variable
							rhs = Optional.of(decl.edges(Direction.OUT, "INITIALIZER").next().inVertex());
						}

						if (rhs.isPresent()) {
							Vertex rhsVar = rhs.get();
							if (rhsVar.property("name").isPresent() && !"".equals(rhsVar.property("name").value())) {
								log.debug("  Has name on right hand side {}", rhsVar.property("name").value());
								// Handle only member calls
								if (Utils.hasLabel(rhsVar, MemberCallExpression.class)) {

									System.out.println("Is member call");
									MemberCallExpression call = (MemberCallExpression) OverflowDatabase.getInstance().vertexToNode(rhsVar);
									if (call == null) {
										log.error("Unexpected: null base of MemberCallExpression " + rhsVar);
										continue;
									}
									de.fraunhofer.aisec.cpg.graph.Node base = call.getBase();
									System.out.println("BASE NAME: " + base);
									Val rhsVal = new Val(base.getName(), currentFunctionName);

									// Add declVal to set of currently tracked variables
									valsInScope.add(declVal);

									Rule<Stmt, Val, Weight> normalRuleSelf = new NormalRule<>(rhsVal, previousStmt, rhsVal, currentStmt, Weight.one());
									log.debug("Adding normal rule {}", normalRuleSelf);
									wpds.addRule(normalRuleSelf);

									Rule<Stmt, Val, Weight> normalRuleCopy = new NormalRule<>(rhsVal, previousStmt, declVal, currentStmt, Weight.one());
									log.debug("Adding normal rule {}", normalRuleCopy);
									wpds.addRule(normalRuleCopy);
								}
							} else {
								// handle new instantiations of objects
								log.debug("  Has no name on right hand side: {}", v.property("code").orElse(""));

								// Normal copy of all values in scope
								for (Val valInScope : valsInScope) {
									Rule<Stmt, Val, Weight> normalRule = new NormalRule<>(valInScope, previousStmt, valInScope, currentStmt,
										Weight.one());
									if (skipTheseValsAtStmt.get(normalRule.getL2()) != null) {
										Val forbiddenVal = skipTheseValsAtStmt.get(normalRule.getL2());
										if (!normalRule.getS1().equals(forbiddenVal)) {
											log.debug("Adding normal rule {}", normalRule);
											wpds.addRule(normalRule);
										}
									}
								}

								// Add declVal to set of currently tracked variables
								valsInScope.add(declVal);

								// Create normal rule
								Rule<Stmt, Val, Weight> normalRule = new NormalRule<>(new Val("EPSILON", currentFunctionName), previousStmt, declVal, currentStmt,
									Weight.one());
								log.debug("Adding normal rule {}", normalRule);
								wpds.addRule(normalRule);

							}
							previousStmt = currentStmt;
						}
					} else if (isReturnStatement(v)) {
						/* Return statements result in pop rules */
						// TODO Proper handling of variables in scope
						ReturnStatement returnV = (ReturnStatement) odb.vertexToNode(v);
						if (returnV != null && !returnV.isDummy()) {
							Set<Val> returnedVals = findReturnedVals(crymlinTraversal, v);

							for (Val returnedVal : returnedVals) {
								Set<NFATransition<Node>> relevantNFATransitions = tsNfa.getTransitions()
										.stream()
										.filter(
											tran -> tran.getTarget().getOp().equals(returnedVal.getVariable()))
										.collect(Collectors.toSet());
								Weight weight = relevantNFATransitions.isEmpty() ? Weight.one() : new Weight(relevantNFATransitions);

								// Pop Rule for actually returned value
								PopRule<Stmt, Val, Weight> returnPopRule = new PopRule<>(new Val(returnV.getReturnValue().getName(), currentFunctionName),
									currentStmt, returnedVal, weight);
								wpds.addRule(returnPopRule);
								log.debug("Adding pop rule {}", returnPopRule);
							}

							// Pop Rules for side effects on parameters
							Map<String, Set<Pair<Val, Val>>> paramToValueMap = findParamToValues(functionDeclaration, v, odb, crymlinTraversal);
							if (paramToValueMap.containsKey(currentFunctionName)) {
								for (Pair<Val, Val> pToA : paramToValueMap.get(currentFunctionName)) {
									PopRule<Stmt, Val, Weight> popRule = new PopRule<>(pToA.getValue0(), currentStmt, pToA.getValue1(), Weight.one());
									wpds.addRule(popRule);
									log.debug("Adding pop rule {}", popRule);
								}
							}

						}
						// Create normal rule. Flow remains where it is.  // TODO should be outside of dummy, but should avoid cyclic rules
						for (Val valInScope : valsInScope) {
							Rule<Stmt, Val, Weight> normalRule = new NormalRule<>(valInScope, previousStmt, valInScope, currentStmt, Weight.one());
							boolean skipIt = false;
							if (skipTheseValsAtStmt.get(normalRule.getL2()) != null) {
								Val forbiddenVal = skipTheseValsAtStmt.get(normalRule.getL2());
								if (!normalRule.getS1().equals(forbiddenVal)) {
									skipIt = true;
								}
							}
							if (!skipIt) {
								log.debug("Adding normal rule!!! {}", normalRule);
								wpds.addRule(normalRule);
							}
						}

						previousStmt = currentStmt;

					}
				}

				// Add successors to work list
				Iterator<Edge> successors = v.edges(Direction.OUT, "EOG");
				while (successors.hasNext()) {
					Vertex succ = successors.next().inVertex();
					if (!alreadySeen.contains(succ)) {
						worklist.add(succ);
						alreadySeen.add(succ);
					}
				}
			}
		}

		/*
		 * Typestate analysis is finished. The results are as follows: 1) Transitions in WNFA with *empty weights* or weights into an ERROR type state indicate an error.
		 * Type state requirements are violated at this point. 2) If there is a path through the automaton leading to the END state, the type state specification is
		 * completely covered by this path 3) If all transitions have proper type state weights but none of them leads to END, the type state is correct but incomplete.
		 */

		return wpds;
	}

	private boolean isReturnStatement(Vertex v) {
		return v.label().equals(ReturnStatement.class.getSimpleName());
	}

	private boolean isDeclarationStatement(Vertex v) {
		return v.label().equals(DeclarationStatement.class.getSimpleName());
	}

	private Set<NormalRule<Stmt, Val, Weight>> createNormalRules(final NFA tsNfa, final CallExpression mce, final Stmt previousStmt, final Stmt currentStmt,
			final Set<Val> valsInScope) {
		Set<NormalRule<Stmt, Val, Weight>> result = new HashSet<>();
		Set<NFATransition<Node>> relevantNFATransitions = tsNfa.getTransitions()
				.stream()
				.filter(
					tran -> belongsToOp(mce.getName(), tran.getTarget().getBase(), tran.getTarget().getOp()))
				.collect(Collectors.toSet());
		Weight weight = relevantNFATransitions.isEmpty() ? Weight.one() : new Weight(relevantNFATransitions);

		// Create normal rule. Flow remains where it is.
		for (Val valInScope : valsInScope) {
			NormalRule<Stmt, Val, Weight> normalRule = new NormalRule<>(valInScope, previousStmt, valInScope, currentStmt, weight);
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
	private boolean belongsToOp(String call, @Nullable String markInstance, String op) {
		if (markInstance == null) {
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
				log.error("Unexpected: FunctionDeclaraion of callee is null.");
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
				//					.outV()
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
	private Set<PushRule<Stmt, Val, Weight>> createPushRules(CallExpression mce, CrymlinTraversalSource crymlinTraversal, String currentFunctionName,
			NFA nfa, Stmt previousStmt, Stmt currentStmt, Vertex v, ArrayDeque<Vertex> worklist) {
		Set<NFATransition<Node>> relevantNFATransitions = nfa.getTransitions()
				.stream()
				.filter(
					tran -> belongsToOp(mce.getName(), tran.getTarget().getBase(), tran.getTarget().getOp()))
				.collect(Collectors.toSet());
		Weight weight = relevantNFATransitions.isEmpty() ? Weight.one() : new Weight(relevantNFATransitions);

		// Return site(s). Actually, multiple return sites will only occur in case of exception handling.
		List<Vertex> returnSites = CrymlinQueryWrapper.getNextStatements(crymlinTraversal, (long) v.id());

		// Arguments of function call
		List<Val> argVals = argumentsToVals(mce, currentFunctionName);

		Set<PushRule<Stmt, Val, Weight>> pushRules = new HashSet<>();
		for (FunctionDeclaration potentialCallee : mce.getInvokes()) {
			// Parameters of function
			if (potentialCallee.getParameters().size() != argVals.size()) {
				log.warn("Skipping call from {} to {} due different argument/parameter counts.", currentFunctionName,
					potentialCallee.getSignature());
				continue;
			}
			List<Val> parmVals = parametersToVals(potentialCallee);

			// Get first statement of callee. This is the jump target of our Push Rule.
			Statement firstStmt = getFirstStmtOfMethod(crymlinTraversal, potentialCallee);

			if (firstStmt != null && firstStmt.getCode() != null) {
				for (int i = 0; i < argVals.size(); i++) {
					for (Vertex returnSiteVertex : returnSites) {
						Stmt returnSite = new Stmt(
							returnSiteVertex.property("code").orElse("").toString(),
							new Region(
								toIntExact((long) returnSiteVertex.property("startLine").value()),
								toIntExact((long) returnSiteVertex.property("startColumn").value()),
								toIntExact((long) returnSiteVertex.property("endLine").value()),
								toIntExact((long) returnSiteVertex.property("endColumn").value())));

						PushRule<Stmt, Val, Weight> pushRule = new PushRule<Stmt, Val, Weight>(
							argVals.get(i),
							currentStmt,
							parmVals.get(i),
							new Stmt(potentialCallee.getName(), potentialCallee.getRegion()),
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
	private Vertex getFirstVertexInFunctionBody(CrymlinTraversalSource crymlinTraversalSource, FunctionDeclaration function) {
		Benchmark query = new Benchmark(this.getClass(), "query");
		Optional<Vertex> vOpt = crymlinTraversalSource.byID(function.getId())
				.outE("BODY")
				.inV()
				.tryNext();
		query.stop();
		return vOpt.orElse(null);
	}

	@Nullable
	private Statement getFirstStmtOfMethod(@NonNull CrymlinTraversalSource crymlinTraversalSource, @NonNull FunctionDeclaration potentialCallee) {
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

	private List<Val> parametersToVals(FunctionDeclaration potentialCallee) {
		List<Val> parmVals = new ArrayList<>();
		for (ParamVariableDeclaration p : potentialCallee.getParameters()) {
			parmVals.add(new Val(p.getName(), potentialCallee.getName()));
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

	private Map<MOp, Set<Vertex>> getVerticesOfRule(MRule rule) {
		Map<MOp, Set<Vertex>> opToVertex = new HashMap<>();
		for (Map.Entry<String, Pair<String, MEntity>> entry : rule.getEntityReferences().entrySet()) {
			MEntity ent = entry.getValue().getValue1();
			if (ent == null) {
				continue;
			}
			for (MOp op : ent.getOps()) {
				Set<Vertex> vertices = op.getAllVertices();
				opToVertex.put(op, vertices);
			}
		}
		return opToVertex;
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
	 * @param variable
	 * @return
	 */
	private WeightedAutomaton createInitialConfiguration(String variable, FunctionDeclaration funcDecl, NFA tsNfa, WPDS wpds) {
		Map<MOp, Set<Vertex>> verticeMap = getVerticesOfRule(rule);
		// Follow funcDecl EOG nodes until we find a call of an OP
		for (Map.Entry<MOp, Set<Vertex>> entry : verticeMap.entrySet()) {
			System.out.println("   OP: " + entry.getKey().getName());
			for (Vertex v : entry.getValue()) {
				System.out.println("       " + v.property("code").value());
				System.out.println("        BASE: " + v.property("base").orElse("NONE"));
			}
		}

		Val initialState = null;
		Stmt stmt = null;
		Weight initialWeight = null;
		Set<NormalRule<Stmt, Val, Weight>> normalRules = wpds.getNormalRules();
		System.out.println(normalRules.size() + " normal rules!!");
		for (NormalRule<Stmt, Val, Weight> nr : normalRules) {
			if (nr.getWeight().value() instanceof Set) {
				Set<NFATransition> weight = (Set<NFATransition>) nr.getWeight().value();
				Val target = nr.getS2();

				for (NFATransition<Node> t : weight) {
					if (t.getSource().getName().equals("START.START")) {
						System.out.println("This is it: " + nr.getS1() + " at " + nr.getL1());

						initialState = nr.getS1();
						stmt = nr.getL1();
						initialWeight = new Weight(Set.of(t));
					}
				}
			}
		}

		if (initialState == null) {
			// TODO For debug only. remove later.
			System.out.println("Null initialstate");
			initialState = new Val("v", "foo");
			stmt = new Stmt("Vector v = new Vector();", new Region(250, 5, 250, 29));
		}

		// Create statement for start configuration and create start CONFIG
		//		Stmt stmt = new Stmt(funcDecl.getName(), funcDecl.getRegion());
		//		System.out.println("Creating initial configuration for " + variable + " at statement " + stmt + " in " + funcDecl.getName());
		//		Val initialState = new Val(variable, funcDecl.getName());
		WeightedAutomaton<Stmt, Val, Weight> wnfa = new WeightedAutomaton<Stmt, Val, Weight>(initialState) {
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
			public Weight getZero() {
				return Weight.zero();
			}

			@Override
			public Weight getOne() {
				return Weight.one();
			}
		};
		Val ACCEPTING = new Val("ACCEPT", "ACCEPT");
		// Create an automaton for the initial configuration from where post* will start.
		wnfa.addTransition(new Transition<>(initialState, stmt, ACCEPTING), initialWeight != null ? initialWeight : Weight.one());
		// Add final ("accepting") states to NFA.
		wnfa.addFinalState(ACCEPTING);

		return wnfa;
	}
}
