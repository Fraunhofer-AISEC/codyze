package de.fraunhofer.aisec.codyze.analysis.wpds

import de.breakpointsec.pushdown.IllegalTransitionException
import de.breakpointsec.pushdown.WPDS
import de.breakpointsec.pushdown.fsm.WeightedAutomaton
import de.breakpointsec.pushdown.rules.NormalRule
import de.breakpointsec.pushdown.rules.PopRule
import de.breakpointsec.pushdown.rules.PushRule
import de.breakpointsec.pushdown.rules.Rule
import de.fraunhofer.aisec.codyze.analysis.AnalysisContext
import de.fraunhofer.aisec.codyze.analysis.ErrorValue
import de.fraunhofer.aisec.codyze.analysis.Finding
import de.fraunhofer.aisec.codyze.analysis.GraphInstanceContext
import de.fraunhofer.aisec.codyze.analysis.MarkContextHolder
import de.fraunhofer.aisec.codyze.analysis.markevaluation.argumentsMatchParameters
import de.fraunhofer.aisec.codyze.analysis.markevaluation.containingFunction
import de.fraunhofer.aisec.codyze.analysis.markevaluation.functions
import de.fraunhofer.aisec.codyze.analysis.markevaluation.nextStatement
import de.fraunhofer.aisec.codyze.analysis.passes.astParent
import de.fraunhofer.aisec.codyze.analysis.resolution.ConstantValue
import de.fraunhofer.aisec.codyze.analysis.utils.Utils
import de.fraunhofer.aisec.codyze.markmodel.MRule
import de.fraunhofer.aisec.codyze.markmodel.fsm.StateNode
import de.fraunhofer.aisec.cpg.ExperimentalGraph
import de.fraunhofer.aisec.cpg.graph.Graph
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.declarations.FunctionDeclaration
import de.fraunhofer.aisec.cpg.graph.declarations.VariableDeclaration
import de.fraunhofer.aisec.cpg.graph.statements.*
import de.fraunhofer.aisec.cpg.graph.statements.expressions.CallExpression
import de.fraunhofer.aisec.cpg.graph.statements.expressions.DeclaredReferenceExpression
import de.fraunhofer.aisec.cpg.graph.statements.expressions.Expression
import de.fraunhofer.aisec.cpg.graph.types.Type
import de.fraunhofer.aisec.cpg.sarif.Region
import de.fraunhofer.aisec.mark.markDsl.OrderExpression
import de.fraunhofer.aisec.mark.markDsl.Terminal
import java.io.File
import java.lang.Math
import java.net.URI
import java.util.ArrayDeque
import java.util.ArrayList
import java.util.HashMap
import java.util.HashSet
import java.util.stream.Collectors
import org.slf4j.LoggerFactory

/**
 * Implementation of a WPDS-based typestate analysis using the code property graph (CPG).
 *
 * Legal typestates are given by a regular expression as of the MARK "order" construct. This class
 * will convert this regular expression into a "typestate NFA". The transitions of the typestate NFA
 * are then represented as "weights" for a weighted pushdown system (WPDS). The WPDS is an
 * abstraction of the data flows in the program (currently there is one WPDS per function, but we
 * can easily extend this to inter-procedural WPDS'es).
 *
 * Given an "initial configuration" in form of a "weighted automaton" P, the "post-*" algorithm [1]
 * will then "saturate" this weighted automaton P into Ps. The saturated weighted automaton Ps is a
 * representation of all type states reachable from the initial configuration, given the underlying
 * program abstraction in form of the WPDS.
 *
 * Thus when inspecting the weights (which are effectively type state transitions) of the saturated
 * weighted automaton Ps, we can check if all operations on an object (and its aliases) refer to
 * legal type state transitions, if there is an execution path in the program which reaches the end
 * of the typestate, or if any operation leads to an illegal typestate (= empty weight in a
 * transition of Ps).
 *
 * [1] Reps T., Lal A., Kidd N. (2007) Program Analysis Using Weighted Pushdown Systems. In: Arvind
 * V., Prasad S. (eds) FSTTCS 2007: Foundations of Software Technology and Theoretical Computer
 * Science. FSTTCS 2007. Lecture Notes in Computer Science, vol 4855. Springer, Berlin, Heidelberg
 */
@ExperimentalGraph
class TypestateAnalysis(private val markContextHolder: MarkContextHolder) {
    private lateinit var rule: MRule
    private lateinit var instanceContext: GraphInstanceContext

    /**
     * Starts the Typestate analysis.
     *
     * @param orderExpr the order expression
     * @param contextID
     * @param ctx
     * @param graph
     * @param rule
     * @return
     * @throws IllegalTransitionException
     */
    @Throws(IllegalTransitionException::class)
    fun analyze(
        orderExpr: OrderExpression,
        contextID: Int,
        ctx: AnalysisContext,
        graph: Graph,
        rule: MRule
    ): ConstantValue {
        log.info("Typestate analysis starting for {}", ctx)

        this.instanceContext = markContextHolder.getContext(contextID).instanceContext
        this.rule = rule

        // Remember the order expression we are analyzing
        val expr = this.rule.statement.ensure.exp
        if (expr !is OrderExpression) {
            log.error("Unexpected: TS analysis not dealing with an order expression")

            return ErrorValue.newErrorValue(
                "Unexpected: TS analysis not dealing with an order expression"
            )
        }

        val markInstance = orderExpr.markInstance
        if (markInstance == null) {
            log.error(
                "OrderExpression does not refer to a Mark instance: {}. Will not run TS analysis",
                orderExpr
            )

            return ErrorValue.newErrorValue(
                String.format(
                    "OrderExpression does not refer to a Mark instance: %s. Will not run TS analysis",
                    orderExpr
                )
            )
        }

        /* Create typestate NFA, representing the regular expression of a MARK typestate rule. */
        val tsNFA = NFA.of(orderExpr.exp)
        log.debug("Initial typestate NFA:\n{}", tsNFA)

        // Create a weighted pushdown system
        val wpds = createWpds(graph, tsNFA)

        /*
         * Create a weighted automaton (= a weighted NFA) that describes the initial configurations. The initial configuration is the statement containing the declaration
         * of the program variable (e.g., "x = Botan2()") that corresponds to the current Mark instance.
         *
         * (e.g., "b").
         */
        val currentFile = getFileFromMarkInstance(markInstance) ?: File("FIXME")

        /* Create an initial automaton corresponding to the start configurations of the data flow analysis.
          In this case, we are starting with all statements that initiate a type state transition.
        */
        val wnfa =
            InitialConfiguration.create({ InitialConfiguration.FIRST_TYPESTATE_EVENT(it) }, wpds)

        // No transition - no finding.
        if (wnfa.initialState == null) {
            return ConstantValue.of(java.lang.Boolean.FALSE)
        }

        // For debugging only: Print WPDS rules
        if (log.isDebugEnabled) {
            for (r in
                wpds.allRules.sortedBy { it.l1.region.startLine }.sortedBy {
                    it.l1.region.startColumn
                }) {
                log.debug("rule: {}", r)
            }

            // For debugging only: Print the non-saturated NFA
            log.debug("Non saturated NFA {}", wnfa)
        }
        // Saturate the NFA from the WPDS, using the post-* algorithm.
        wpds.poststar(wnfa)

        // For debugging only: Print the post-*-saturated NFA
        log.debug("Saturated WNFA {}", wnfa)

        // Evaluate saturated WNFA for any MARK violations
        val findings = getFindingsFromWpds(wpds, wnfa, currentFile.toURI())
        if (markContextHolder.isCreateFindingsDuringEvaluation) {
            ctx.findings.addAll(findings)
        }

        val of = ConstantValue.of(findings.isEmpty())
        if (markContextHolder.isCreateFindingsDuringEvaluation) {
            markContextHolder.getContext(contextID).isFindingAlreadyAdded = true
        }

        return of
    }

    private fun getFileFromMarkInstance(markInstance: String): File? {
        val node = instanceContext.getNode(markInstance)
        if (node == null) {
            log.error("No node found for Mark instance: {}. Will not run TS analysis", markInstance)
            return null
        }

        // Find the function in which the vertex is located, so we can use the first statement in
        // function as a start
        val containingFunction = node.containingFunction
        if (containingFunction == null) {
            log.error(
                "Node {} not located within a function. Cannot start TS analysis for rule {}",
                node.code,
                rule
            )
            return null
        }

        containingFunction.file?.let {
            return File(it)
        }

        return null
    }

    /** Returns the MARK instance of this order expression, if it exists. */
    private val OrderExpression.markInstance: String?
        get() {
            val treeIt = this.eAllContents()

            while (treeIt.hasNext()) {
                val eObj = treeIt.next()
                if (eObj is Terminal) {
                    return eObj.entity
                }
            }

            return null
        }

    /**
     * Evaluates a saturated WNFA.
     *
     * This method receives a post-*-saturated WNFA and creates Findings if any violations of the
     * given MARK rule are found.
     *
     * 1) Transitions in WNFA with *empty weights* or weights into an ERROR type state indicate an
     * error. Type state requirements are violated at this point.
     *
     * 2) If there is a path through the automaton leading to the END state, the type state
     * specification is completely covered by this path
     *
     * 3) If all transitions have proper type state weights but none of them leads to END, the type
     * state is correct but incomplete.
     *
     * @param wpds
     * @param wnfa Weighted NFA, representing a set of configurations of the WPDS
     * @param currentFile
     * @return
     */
    private fun getFindingsFromWpds(
        wpds: CpgWpds,
        wnfa: WeightedAutomaton<Stmt, Val?, TypestateWeight>,
        currentFile: URI
    ): Set<Finding> {
        // Final findings
        val findings: MutableSet<Finding> = HashSet()
        // We collect good findings first, but add them only if TS machine reaches END state
        val potentialGoodFindings: MutableSet<Pair<Stmt, Val?>> = HashSet()
        var endReached = false

        // All configurations for which we have rules. Ignoring Weight.ONE
        val wpdsConfigs: MutableSet<Pair<Stmt, Val>> = HashSet()
        for (r in wpds.allRules) {
            if (r.weight != TypestateWeight.one()) {
                wpdsConfigs.add(Pair(r.l1, r.s1))
                wpdsConfigs.add(Pair(r.l2, r.s2))
            }
        }
        for (tran in wnfa.transitions) {
            val w = wnfa.getWeightFor(tran)
            if (w.value() is Set<*>) {
                val reachableTypestates = w.value() as Set<NFATransition<StateNode>>
                for (reachableTypestate in reachableTypestates) {
                    if (reachableTypestate.target.isError) {
                        findings.add(createBadFinding(tran.label, tran.start, currentFile))
                    } else {
                        potentialGoodFindings.add(Pair(tran.label, tran.start))
                    }
                    endReached = endReached or reachableTypestate.target.isEnd
                }
            } else if (w == TypestateWeight.zero()) {
                // Check if this is actually a feasible configuration
                val (first, second) = Pair(tran.label, tran.start)
                if (wpdsConfigs.stream().anyMatch { (first1, second1) ->
                        first1 == first && second1 == second
                    }
                ) {
                    findings.add(createBadFinding(first, second, currentFile, java.util.Set.of()))
                }
            }
        }
        if (endReached && findings.isEmpty()) {
            findings.addAll(
                potentialGoodFindings
                    .stream()
                    .map { (first, second) -> createGoodFinding(first, second, currentFile) }
                    .collect(Collectors.toSet())
            )
        }
        return findings
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
    private fun createBadFinding(
        stmt: Stmt,
        `val`: Val?,
        currentFile: URI,
        expected: Collection<NFATransition<StateNode>> = listOf()
    ): Finding {
        var name =
            "Invalid typestate of variable " +
                `val` +
                " at statement: " +
                stmt +
                " . Violates order of " +
                rule.name
        name +=
            if (!expected.isEmpty()) {
                " Expected one of " +
                    expected
                        .stream()
                        .map { obj: NFATransition<StateNode> -> obj.toString() }
                        .collect(Collectors.joining(", "))
            } else {
                " Expected no further operations."
            }

        // lines are human-readable, i.e., off-by-one
        val startLine = Math.toIntExact(stmt.region.startLine.toLong()) - 1
        val endLine = Math.toIntExact(stmt.region.endLine.toLong()) - 1
        val startColumn = Math.toIntExact(stmt.region.startColumn.toLong()) - 1
        val endColumn = Math.toIntExact(stmt.region.endColumn.toLong()) - 1
        return Finding(
            name,
            rule.errorMessage,
            currentFile,
            startLine,
            endLine,
            startColumn,
            endColumn
        )
    }

    /**
     * Create a "non-finding" (i.e. positive confirmation) Lines are human-readable, i.e.,
     * off-by-one.
     *
     * @param stmt
     * @param val
     * @param currentFile
     * @return
     */
    private fun createGoodFinding(stmt: Stmt, `val`: Val?, currentFile: URI): Finding {
        val startLine = Math.toIntExact(stmt.region.startLine.toLong()) - 1
        val endLine = Math.toIntExact(stmt.region.endLine.toLong()) - 1
        val startColumn = Math.toIntExact(stmt.region.startColumn.toLong()) - 1
        val endColumn = Math.toIntExact(stmt.region.endColumn.toLong()) - 1
        return Finding(
            "Good: $`val` at $stmt",
            currentFile,
            rule!!.errorMessage,
            java.util.List.of(Region(startLine, endLine, startColumn, endColumn)),
            false
        )
    }

    /**
     * Creates a weighted pushdown system (WPDS), linked to a typestate NFA.
     *
     * When populating the WPDS using post-* algorithm, the result will be an automaton capturing
     * the reachable type states.
     *
     * @param graph
     * @param tsNfa
     *
     * @return
     */
    private fun createWpds(graph: Graph, tsNfa: NFA): CpgWpds {
        log.info("-----  Creating WPDS ----------")

        /* Create empty WPDS */
        val wpds = CpgWpds()

        /*
         * For each function, create a WPDS.
         *
         * The (normal, push, pop) rules of the WPDS reflect the data flow, similar to a static taint analysis.
         *
         */ for (functionDeclaration in graph.functions) {
            val funcWpds = createWpds(functionDeclaration, tsNfa)
            for (r in funcWpds.allRules) {
                wpds.addRule(r)
            }
        }

        /*
         * Typestate analysis is finished. The results are as follows: 1) Transitions in WNFA with *empty weights* or weights into an ZERO type state indicate an error.
         * Type state requirements are violated at this point. 2) If there is a path through the automaton leading to the END state, the type state specification is
         * completely covered by this path 3) If all transitions have proper type state weights but none of them leads to END, the type state is correct but incomplete.
         */ return wpds
    }

    /**
     * Turns a single function into a WPDS.
     *
     * @param tsNfa
     * @return
     */
    private fun createWpds(fd: FunctionDeclaration, tsNfa: NFA): WPDS<Stmt, Val, TypestateWeight> {
        // To remember already visited nodes and avoid endless iteration
        val alreadySeen = HashSet<Node>()

        // the WPDS we are creating here
        val wpds = CpgWpds()
        log.info("Processing function {}", fd.name)

        // Work list of following EOG nodes. Not all EOG nodes will result in a WPDS rule, though.
        val worklist = ArrayDeque<Pair<Node, Set<Stmt>>>()
        worklist.add(
            Pair<Node, Set<Stmt>>(fd, java.util.Set.of(Stmt(fd.name, Utils.getRegion(fd))))
        )
        val skipTheseValsAtStmt = HashMap<Stmt, Val?>()
        val valsInScope = HashSet<Val>()

        // Make sure we track all parameters inside this function
        val params = fd.parameters
        for (p in params) {
            valsInScope.add(Val(p.name, fd.name))
        }

        // Start creation of WPDS rules by traversing the EOG
        while (!worklist.isEmpty()) {
            val (v, second) = worklist.pop()
            for (previousStmt in second) {
                // We consider only "Statements" and CallExpressions in the EOG
                if (isRelevantStmt(v)) {
                    createRulesForStmt(
                        wpds,
                        fd,
                        previousStmt,
                        v,
                        valsInScope,
                        skipTheseValsAtStmt,
                        tsNfa
                    )
                } // End isRelevantStmt()
            }

            // Add successors to work list
            val successors = getSuccessors(v, alreadySeen)
            for (succ in successors) {
                if (isRelevantStmt(v)) {
                    worklist.add(Pair(succ, java.util.Set.of(vertexToStmt(v))))
                } else {
                    worklist.add(Pair(succ, second))
                }
            }
        }
        return wpds
    }

    private fun createRulesForStmt(
        wpds: WPDS<Stmt, Val, TypestateWeight>,
        functionVertex: FunctionDeclaration,
        previousStmt: Stmt,
        stmtNode: Node,
        valsInScope: MutableSet<Val>,
        skipTheseValsAtStmt: MutableMap<Stmt, Val?>,
        tsNfa: NFA
    ) {
        val currentFunctionName = functionVertex.name
        val currentStmt = vertexToStmt(stmtNode)

        /* First we create normal rules from previous stmt to the current stmt, simply propagating existing values. */
        val normalRules = createNormalRules(previousStmt, stmtNode, valsInScope, tsNfa)
        for (normalRule in normalRules) {
            /*
             If this is a call into a known method, we skip immediate propagation. In that case, data flows *into* the method.
            */
            val skipIt = shouldBeSkipped(normalRule, skipTheseValsAtStmt)
            if (!skipIt) {
                wpds.addRule(normalRule)
            }
        }

        /*
         Handle calls into known methods (not a "phantom" method) by creating push rule.
        */ if (stmtNode is CallExpression && !Utils.isPhantom(stmtNode)) {
            /*
             * For calls to functions whose body is known, we create push/pop rule pairs. All arguments flow into the parameters of the function. The
             * "return site" is the statement to which flow returns after the function call.
             */
            val pushRules = createPushRules(stmtNode, currentFunctionName, currentStmt, stmtNode)
            for (pushRule in pushRules) {
                log.debug("  Adding push rule: {}", pushRule)
                wpds.addRule(pushRule)

                // Remember that arguments flow only into callee and do not bypass it.
                skipTheseValsAtStmt[pushRule.callSite] = pushRule.s1
            }
        } else if (stmtNode is VariableDeclaration) {
            // Add declVal to set of currently tracked variables
            val declVal = Val(stmtNode.name, currentFunctionName)
            valsInScope.add(declVal)
        } else if (stmtNode is DeclarationStatement) {
            /* Handle declaration of new variables.
             "DeclarationStatements" result in a normal rule, assigning rhs to lhs.
            */

            // Note: We might be a bit more gracious here to tolerate incorrect code. For example, a
            // non-declared variable would be a "BinaryOperator".
            log.debug("Found variable declaration {}", stmtNode.code)
            for (decl in stmtNode.declarations) {
                if (decl !is VariableDeclaration) {
                    continue
                }

                val declVal = Val(decl.name, currentFunctionName)
                val rhs: Expression? = decl.initializer
                if (rhs is CallExpression) {
                    /* Handle function/method calls whose return value is assigned to a declared variable.
                      A new data flow for the declared variable (declVal) is introduced.
                    */
                    val normaleRuleDeclared: Rule<Stmt, Val, TypestateWeight> =
                        NormalRule(
                            Val(CpgWpds.EPSILON, currentFunctionName),
                            previousStmt,
                            declVal,
                            currentStmt,
                            TypestateWeight.one()
                        )
                    log.debug("Adding normal rule for declaration {}", normaleRuleDeclared)
                    wpds.addRule(normaleRuleDeclared)

                    // Add declVal to set of currently tracked variables
                    valsInScope.add(declVal)
                } else if (rhs != null) {
                    /**
                     * Handle assignment from right side (rhs) to left side (lhs).
                     *
                     * We simply take rhs.getName() as a data source. This might be imprecise and
                     * need further differentiation. For instance, if rhs is an expression (other
                     * than CallExpression), we might want to recursively handle data flows within
                     * that expression. This is currently not implemented as it is not needed for
                     * our use case and would add unneeded complexity.
                     */
                    val rhsVal = Val(rhs.name, currentFunctionName)

                    // Add declVal to set of currently tracked variables
                    valsInScope.add(declVal)
                    val normalRulePropagate: Rule<Stmt, Val, TypestateWeight> =
                        NormalRule(
                            rhsVal,
                            previousStmt,
                            declVal,
                            currentStmt,
                            TypestateWeight.one()
                        )
                    log.debug("Adding normal rule for assignment {}", normalRulePropagate)
                    wpds.addRule(normalRulePropagate)
                }
            }
        } else if (stmtNode is ReturnStatement) {
            /* Return statements result in pop rules */
            val returnV = stmtNode
            if (!returnV.isDummy) {
                val returnedVals = findReturnedVals(stmtNode)
                for (returnedVal in returnedVals) {
                    val relevantNFATransitions =
                        tsNfa
                            .transitions
                            .stream()
                            .filter { tran: NFATransition<StateNode> ->
                                (tran.target.op == returnedVal.variable)
                            }
                            .collect(Collectors.toSet())
                    val weight =
                        if (relevantNFATransitions.isEmpty()) TypestateWeight.one()
                        else TypestateWeight(relevantNFATransitions)

                    // Pop Rule for actually returned value
                    val returnPopRule =
                        PopRule(
                            Val(returnV.returnValue.name, currentFunctionName),
                            currentStmt,
                            returnedVal,
                            weight
                        )
                    wpds.addRule(returnPopRule)
                    log.debug("Adding pop rule {}", returnPopRule)
                }

                // Pop Rules for side effects on parameters
                val paramToValueMap = findParamToValues(functionVertex)
                if (paramToValueMap.containsKey(currentFunctionName)) {
                    for ((first, second) in paramToValueMap[currentFunctionName]!!) {
                        val popRule = PopRule(first, currentStmt, second, TypestateWeight.one())
                        wpds.addRule(popRule)
                        log.debug("Adding pop rule {}", popRule)
                    }
                }
            }
            // Create normal rule. Flow remains where it is.
            for (valInScope in valsInScope) {
                val normalRule: Rule<Stmt, Val, TypestateWeight> =
                    NormalRule(
                        valInScope,
                        previousStmt,
                        valInScope,
                        currentStmt,
                        TypestateWeight.one()
                    )
                var skipIt = false
                if (skipTheseValsAtStmt[normalRule.l2] != null) {
                    val forbiddenVal = skipTheseValsAtStmt[normalRule.l2]
                    if (normalRule.s1 != forbiddenVal) {
                        skipIt = true
                    }
                }
                if (!skipIt) {
                    log.debug("Adding normal rule!!! {}", normalRule)
                    wpds.addRule(normalRule)
                }
            }
        } // End isReturnStatement
    }

    private fun shouldBeSkipped(
        normalRule: NormalRule<Stmt, Val, TypestateWeight>,
        skipTheseValsAtStmt: Map<Stmt, Val?>
    ): Boolean {
        var skipIt = false
        if (skipTheseValsAtStmt[normalRule.l2] != null) {
            val forbiddenVal = skipTheseValsAtStmt[normalRule.l2]
            if (normalRule.s1 != forbiddenVal) {
                skipIt = true
            }
        }
        return skipIt
    }

    /**
     * Returns a set of Vertices which are successors of `v` in the EOG and are not contained in
     * `alreadySeen`.
     *
     * @param n
     * @param alreadySeen
     * @return
     */
    private fun getSuccessors(n: Node, alreadySeen: HashSet<Node>): HashSet<Node> {
        val unseenSuccessors = HashSet<Node>()
        for (succ in n.nextEOG) {
            if (!alreadySeen.contains(succ)) {
                unseenSuccessors.add(succ)
                alreadySeen.add(succ)
            }
        }
        return unseenSuccessors
    }

    /**
     * We do not convert all EOG nodes into WPDS rules, but only "relevant" ones, i.e. statements
     * and call expressions.
     *
     * @param node
     * @return
     */
    private fun isRelevantStmt(node: Node): Boolean {
        val numberOfOutgoingEogs = node.nextEOG.size
        return (node is CallExpression ||
            node is DeclarationStatement ||
            node is VariableDeclaration ||
            node is IfStatement ||
            node.astParent is CompoundStatement ||
            numberOfOutgoingEogs >= 2)
    }

    private fun createNormalRules(
        previousStmt: Stmt,
        v: Node,
        valsInScope: Set<Val>,
        tsNfa: NFA
    ): Set<NormalRule<Stmt, Val, TypestateWeight>> {
        val currentStmt = vertexToStmt(v)
        val result: MutableSet<NormalRule<Stmt, Val, TypestateWeight>> = HashSet()

        // Create normal rule. Flow remains where it is.
        for (valInScope in valsInScope) {
            // Determine weight
            val relevantNFATransitions =
                tsNfa
                    .transitions
                    .stream()
                    .filter { tran: NFATransition<StateNode> ->
                        triggersTypestateTransition(v, tran.target.base, tran.target.op)
                    }
                    .collect(Collectors.toSet())
            val weight =
                if (relevantNFATransitions.isEmpty()) TypestateWeight.one()
                else TypestateWeight(relevantNFATransitions)
            val normalRule = NormalRule(valInScope, previousStmt, valInScope, currentStmt, weight)
            log.debug("Adding normal rule {}", normalRule)
            result.add(normalRule)
        }
        return result
    }

    /**
     * Returns true if the given CPG `Node` will result in a transition from any typestate into the
     * typestate detened by `op`.
     *
     * @param cpgNode A CPG node - typically a `CallExpression` or anything that contains a call
     * (e.g., a `VariableDeclaration`)
     * @param markInstance The current MARK instance.
     * @param op The target typestate, indicated by a MARK op.
     * @return
     */
    private fun triggersTypestateTransition(
        cpgNode: Node,
        markInstance: String?,
        op: String
    ): Boolean {
        /*
        TODO Future improvement: This method is repeatedly called for different "ops" and thus repeats quite some work.
        The "op" paramenter should be removed and the method should return a (possibly empty) set of target typestates (=ops)
        that would be reached, so this method needs to be called only once per CPG node.
        */
        if (markInstance == null || cpgNode.name == "") {
            return false
        }

        // Get the MARK entity of the markInstance
        val mEntity = rule!!.entityReferences[markInstance]
        if (mEntity == null || mEntity.second == null) {
            return false
        }

        // For non-OO languages, we need to check valInScope against function args and return value
        // (=assignee)
        var assigneeVar: String? = null
        var assignerFqn: String? = null
        if (cpgNode is VariableDeclaration) {
            assigneeVar = cpgNode.name
            if (cpgNode.initializer is CallExpression) {
                assignerFqn = (cpgNode.initializer as CallExpression).fqn
            }
        } else if (cpgNode is CallExpression) {
            assignerFqn = cpgNode.fqn
        }

        // For method calls we collect the "base", its type(s), and the method arguments.
        var types: Set<Type> = HashSet()
        val arguments: MutableList<Expression> = ArrayList()
        if (cpgNode is CallExpression) {
            val base: Expression? = cpgNode.base

            // even though base is annotated @NotNull, it sometimes is null
            if (base != null) {
                types = base.possibleSubTypes
            }
            arguments.addAll(cpgNode.arguments)
        }
        for (o in mEntity.second!!.ops) {
            if (op != o.name) {
                continue
            }
            for (opStatement in o.statements) {
                if (types.isEmpty()) {
                    /* Failure to resolve type or a function call (e.g. EVP_EncryptInit), rather than a method call.
                      In case of function calls/non-OO languages, we ignore the type of the (non-existing) base
                      and simply check if the call stmt matches the one in the "op" spec.
                      "Matches" means that it matches the function and valInScope is either one of the arguments or is assigned the call's return value.
                    */
                    if (assignerFqn != null &&
                            opStatement.call.name == assignerFqn &&
                            (assigneeVar != null // is return value assigned to valInScope?
                            ||
                                arguments.isEmpty() ||
                                argumentsMatchParameters(
                                    opStatement.call.params,
                                    (cpgNode as CallExpression).arguments
                                ))
                    ) {
                        return true
                    }
                } else {
                    for (type in types) {
                        if (type.typeName.startsWith(
                                Utils.getScope(opStatement.call.name).replace("::", ".")
                            ) // Dirty: startsWith() to ignore modifiers (such as "*").
                            && opStatement.call.name.endsWith(cpgNode.name)
                        ) {
                            // TODO should rather compare fully qualified names instead of
                            // "endsWith"
                            return true
                        }
                    }
                }
            }
        }
        return false
    }

    /**
     * Finds the mapping from function parameters to arguments of calls to this method. This is
     * needed for later construction of pop rules.
     *
     * @param callee
     * @return
     */
    private fun findParamToValues(callee: FunctionDeclaration?): Map<String, Set<Pair<Val, Val>>> {
        val result: MutableMap<String, Set<Pair<Val, Val>>> = HashMap()
        if (callee == null) {
            log.error("Unexpected: FunctionDeclaration of callee is null.")
            return result
        }
        val calleeName = callee.name

        // the function declaration is connected to their call expressions by a DFG edge
        val calls =
            callee
                .nextDFG
                .stream()
                .filter { obj: Node? -> CallExpression::class.java.isInstance(obj) }
                .map { obj: Node? -> CallExpression::class.java.cast(obj) }
                .collect(Collectors.toList())
        for (ce in calls) {
            val caller = ce.containingFunction
            if (caller == null) {
                log.error("Unexpected: Null Node object for FunctionDeclaration")
                continue
            }
            val args = ce.arguments
            val params = callee.parameters
            val pToA = HashSet<Pair<Val, Val>>()
            for (i in 0 until Math.min(params.size, args.size)) {
                pToA.add(Pair(Val(params[i].name, calleeName), Val(args[i].name, caller.name)))
            }
            result[calleeName] = pToA
        }
        return result
    }

    /**
     * Given a return statement, this method finds all variables at the caller site that might be
     * assigned the returned value.
     *
     * In the following example, given the return statement in bla(), this method will return a Val
     * for "x".
     *
     * void blubb() { int x = bla(); }
     *
     * int bla() { return 42; }
     *
     * @param node
     * @return
     */
    private fun findReturnedVals(node: Node): Set<Val> {
        /*
         * Follow along "DFG" edges from the return statement to the CallExpression that initiated the call. Then check if there is a "DFG" edge from that CallExpression
         * to a VariableDeclaration.
         */
        val returnedVals: MutableSet<Val> = HashSet()
        val calls =
            node.nextDFG
                .stream()
                .filter { obj: Node? -> CallExpression::class.java.isInstance(obj) }
                .collect(Collectors.toList())
        for (call in calls) {
            // We found the call site into our method. Now see if the return value is used.
            val nextDfgAftercall = call.nextDFG.stream().findFirst()
            var returnVar = ""
            if (nextDfgAftercall.isPresent) {
                if (nextDfgAftercall.get() is VariableDeclaration ||
                        nextDfgAftercall.get() is DeclaredReferenceExpression
                ) {
                    // return value is used. Remember variable name.
                    returnVar = nextDfgAftercall.get().name
                }

                // Finally we need to find out in which function the call site actually is
                var callerFunctionName: String? = null
                val callerFunction = call.containingFunction
                if (callerFunction != null) {
                    callerFunctionName = callerFunction.name
                }
                if (callerFunctionName != null) {
                    returnedVals.add(Val(returnVar, callerFunctionName))
                }
            }
        }
        return returnedVals
    }

    /**
     * Creates push rules for a given call expression.
     *
     * Typically, there will be only a single push rule per call expression. Only in case of
     * multiple return sites, such as when considering exception handling, the resulting set may
     * contain more than one rule.
     *
     * @param mce
     * @param currentFunctionName
     * @param currentStmt
     * @param currentStmtVertex
     * @return
     */
    private fun createPushRules(
        mce: CallExpression,
        currentFunctionName: String,
        currentStmt: Stmt,
        currentStmtVertex: Node
    ): Set<PushRule<Stmt, Val, TypestateWeight>> {
        // Return site(s). Actually, multiple return sites will only occur in case of exception
        // handling.
        // TODO: support multiple return sites
        val returnSite = currentStmtVertex.nextStatement
        val returnSites: List<Statement>
        returnSites = returnSite?.let { listOf(it) } ?: emptyList()

        // Arguments of function call
        val argVals = argumentsToVals(mce, currentFunctionName)
        val pushRules: MutableSet<PushRule<Stmt, Val, TypestateWeight>> = HashSet()
        for (potentialCallee in mce.invokes) {
            // Parameters of function
            if (potentialCallee.parameters.size != argVals.size) {
                log.warn(
                    "Skipping call from {} to {} due different argument/parameter counts.",
                    currentFunctionName,
                    potentialCallee.signature
                )
                continue
            }
            val parmVals = parametersToVals(potentialCallee)

            // Get first statement of callee. This is the jump target of our Push Rule.
            val firstStmt = potentialCallee.firstStatementOrNull
            if (firstStmt?.code != null) {
                for (i in argVals.indices) {
                    for (returnSiteVertex in returnSites) {
                        val stmt = vertexToStmt(returnSiteVertex)
                        val pushRule =
                            PushRule(
                                argVals[i],
                                currentStmt,
                                parmVals[i],
                                Stmt(potentialCallee.name, Utils.getRegion(potentialCallee)),
                                stmt,
                                TypestateWeight.one()
                            ) // A push rule does not trigger any typestate transitions.
                        pushRules.add(pushRule)
                    }
                }
            } else {
                log.error(
                    "Unexpected: Found a method with body, but no first statement relevant for WPDS: {}",
                    potentialCallee.name
                )
            }
        }
        return pushRules
    }

    private val FunctionDeclaration.firstStatementOrNull: Statement?
        get() {
            if (this.body != null) {
                var firstStmt = this.body
                while (firstStmt is CompoundStatement) {
                    firstStmt = firstStmt.statements.firstOrNull()
                }

                return firstStmt
            }

            log.error("Function does not have a body: {}", this.name)

            return null
        }

    /**
     * Returns a (mutable) list of the function parameters of `func`, each wrapped as a `Val`.
     *
     * @param func
     * @return
     */
    private fun parametersToVals(func: FunctionDeclaration): List<Val> {
        val parmVals: MutableList<Val> = ArrayList()
        for (p in func.parameters) {
            parmVals.add(Val(p.name, func.name))
        }
        return parmVals
    }

    private fun argumentsToVals(
        callExpression: CallExpression,
        currentFunctionName: String
    ): List<Val> {
        val argVals: MutableList<Val> = ArrayList()
        val args = callExpression.arguments
        for (arg in args) {
            argVals.add(Val(arg.name, currentFunctionName))
        }
        return argVals
    }

    /**
     * Convert a CPG node into a `Stmt` in context of the WPDS.
     *
     * @param n CPG node
     * @return A `Stmt`, holding the "code" and "location->region" properties of `n`>.
     */
    private fun vertexToStmt(n: Node): Stmt {
        val region = Utils.getRegion(n)
        return Stmt(n.code!!, region)
    }

    companion object {
        private val log = LoggerFactory.getLogger(TypestateAnalysis::class.java)
    }
}
