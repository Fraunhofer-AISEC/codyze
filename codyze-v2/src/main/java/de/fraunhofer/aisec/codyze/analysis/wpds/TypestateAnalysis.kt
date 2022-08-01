package de.fraunhofer.aisec.codyze.analysis.wpds

import de.breakpointsec.pushdown.IllegalTransitionException
import de.breakpointsec.pushdown.WPDS
import de.breakpointsec.pushdown.fsm.WeightedAutomaton
import de.breakpointsec.pushdown.rules.NormalRule
import de.breakpointsec.pushdown.rules.PopRule
import de.breakpointsec.pushdown.rules.PushRule
import de.breakpointsec.pushdown.rules.Rule
import de.fraunhofer.aisec.codyze.analysis.*
import de.fraunhofer.aisec.codyze.analysis.markevaluation.*
import de.fraunhofer.aisec.codyze.analysis.resolution.ConstantValue
import de.fraunhofer.aisec.codyze.analysis.utils.Utils
import de.fraunhofer.aisec.codyze.markmodel.MRule
import de.fraunhofer.aisec.codyze.sarif.schema.Result
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
import de.fraunhofer.aisec.cpg.passes.astParent
import de.fraunhofer.aisec.mark.markDsl.OrderExpression
import de.fraunhofer.aisec.mark.markDsl.Terminal
import java.io.File
import java.net.URI
import java.util.*
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
    private lateinit var markContext: MarkContext

    /**
     * Starts the Typestate analysis.
     *
     * @param orderExpr the order expression
     * @param markContext the MARK context
     * @param ctx the analysis context
     * @param graph
     * @param rule
     * @return
     * @throws IllegalTransitionException
     */
    @Throws(IllegalTransitionException::class)
    fun analyze(
        orderExpr: OrderExpression,
        markContext: MarkContext,
        ctx: AnalysisContext,
        graph: Graph,
        rule: MRule
    ): ConstantValue {
        log.info("Typestate analysis starting for {}", ctx)

        this.markContext = markContext
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

        // Create typestate NFA, representing the regular expression of a MARK typestate rule.
        val tsNFA = NFA.of(orderExpr.exp)
        log.debug("Initial typestate NFA:\n{}", tsNFA)

        // Create a weighted pushdown system
        val wpds = createWPDS(graph, tsNFA)

        // Create a weighted automaton (= a weighted NFA) that describes the initial configurations.
        // The initial configuration is the statement containing the declaration
        // of the program variable (e.g., "x = Botan2()") that corresponds to the current Mark
        // instance (e.g., "b").
        val currentFile = getFileFromMarkInstance(markInstance) ?: File("FIXME")

        // Create an initial automaton corresponding to the start configurations of the data flow
        // analysis.
        // In this case, we are starting with all statements that initiate a type state transition.
        val wnfa =
            InitialConfiguration.create({ InitialConfiguration.FIRST_TYPESTATE_EVENT(it) }, wpds)

        // No transition - no finding.
        if (wnfa.initialState == null) {
            return ConstantValue.of(java.lang.Boolean.FALSE)
        }

        // For debugging only: Print WPDS rules
        if (log.isDebugEnabled) {
            for (r in
                wpds.allRules
                    .sortedBy { it.l1.location?.region?.startLine }
                    .sortedBy { it.l1.location?.region?.startColumn }) {
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
            markContext.isFindingAlreadyAdded = true
        }

        return of
    }

    private fun getFileFromMarkInstance(markInstance: String): File? {
        val node = markContext.instanceContext.getNode(markInstance)
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

    /**
     * Returns the MARK instance of this order expression, if it exists.
     *
     * @return the name of the MARK instance
     */
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
     * @param wpds the WPDS
     * @param wnfa Weighted NFA, representing a set of configurations of the WPDS
     * @param currentFile
     *
     * @return a set of findings
     */
    private fun getFindingsFromWpds(
        wpds: WPDS<Node, Val, TypestateWeight>,
        wnfa: WeightedAutomaton<Node, Val, TypestateWeight>,
        currentFile: URI
    ): Set<Finding> {
        // Final findings
        val findings = mutableSetOf<Finding>()

        // We collect good findings first, but add them only if TS machine reaches END state
        val potentialGoodFindings = mutableSetOf<Pair<Node, Val>>()
        var endReached = false

        // All configurations for which we have rules. Ignoring Weight.ONE
        val wpdsConfigs = mutableSetOf<Pair<Node, Val>>()
        for (r in wpds.allRules) {
            if (r.weight != TypestateWeight.one()) {
                wpdsConfigs.add(Pair(r.l1, r.s1))
                wpdsConfigs.add(Pair(r.l2, r.s2))
            }
        }

        for (tran in wnfa.transitions) {
            val w = wnfa.getWeightFor(tran)

            if (w.value() is Set<*>) {
                val reachableTypestates = w.value() as Set<NFATransition>
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
                if (wpdsConfigs.any { (first1, second1) -> first1 == first && second1 == second }) {
                    findings.add(createBadFinding(first, second, currentFile))
                }
            }
        }

        if (endReached && findings.isEmpty()) {
            findings.addAll(
                potentialGoodFindings.map { (first, second) ->
                    createGoodFinding(first, second, currentFile)
                }
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
        stmt: Node,
        `val`: Val,
        currentFile: URI,
        expected: Collection<NFATransition> = listOf()
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
                " Expected one of " + expected.joinToString(", ") { obj -> obj.toString() }
            } else {
                " Expected no further operations."
            }

        return Finding(
            rule.errorMessage ?: rule.name,
            rule.statement.action,
            name,
            currentFile,
            listOf(Utils.getRegionByNode(stmt)),
            Result.Kind.FAIL
        )
    }

    /**
     * Creates a "non-finding" (i.e. positive confirmation). Lines are human-readable, i.e.,
     * off-by-one.
     *
     * @param node the node
     * @param val
     * @param currentFile
     *
     * @return a finding
     */
    private fun createGoodFinding(node: Node, `val`: Val?, currentFile: URI): Finding {
        return Finding(
            rule.errorMessage ?: rule.name,
            rule.statement.action,
            "Good: $`val` at $node",
            currentFile,
            listOf(Utils.getRegionByNode(node)),
            Result.Kind.PASS
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
    private fun createWPDS(graph: Graph, tsNfa: NFA): WPDS<Node, Val, TypestateWeight> {
        log.info("-----  Creating WPDS ----------")

        // Create empty WPDS
        val wpds = GraphWPDS()

        // For each function, create a WPDS. The (normal, push, pop) rules of the WPDS reflect the
        // data flow, similar to a static taint analysis.
        for (functionDeclaration in graph.functions) {
            val funcWpds = createWPDS(functionDeclaration, tsNfa)
            for (r in funcWpds.allRules) {
                wpds.addRule(r)
            }
        }

        // Typestate analysis is finished. The results are as follows:
        // 1) Transitions in WNFA with *empty weights* or weights into an ZERO type state indicate
        // an error. Type state requirements are violated at this point.
        // 2) If there is a path through the automaton leading to the END state, the type state
        // specification is completely covered by this path
        // 3) If all transitions have proper type state weights but none of them leads to END, the
        // type state is correct but incomplete.
        return wpds
    }

    /**
     * Turns a single function into a WPDS.
     *
     * @param tsNfa
     * @return
     */
    private fun createWPDS(fd: FunctionDeclaration, tsNfa: NFA): WPDS<Node, Val, TypestateWeight> {
        // To remember already visited nodes and avoid endless iteration
        val alreadySeen = HashSet<Node>()

        // the WPDS we are creating here
        val wpds = GraphWPDS()
        log.info("Processing function {}", fd.name)

        // Work list of following EOG nodes. Not all EOG nodes will result in a WPDS rule, though.
        val worklist = ArrayDeque<Pair<Node, Set<Node>>>()
        worklist.add(Pair<Node, Set<Node>>(fd, setOf(fd)))
        val skipTheseValsAtStmt = mutableMapOf<Node, Val>()
        val valsInScope = mutableSetOf<Val>()

        // Make sure we track all parameters inside this function
        val params = fd.parameters
        for (p in params) {
            valsInScope.add(Val(p.name, fd.name))
        }

        // Start creation of WPDS rules by traversing the EOG
        while (!worklist.isEmpty()) {
            val (currentNode, previousNodes) = worklist.pop()
            for (previousStmt in previousNodes) {
                // We consider only "Statements" and CallExpressions in the EOG
                if (isRelevantStmt(currentNode)) {
                    createRulesForStmt(
                        wpds,
                        fd,
                        previousStmt,
                        currentNode,
                        valsInScope,
                        skipTheseValsAtStmt,
                        tsNfa
                    )
                }
            }

            // Add successors to work list
            val successors = currentNode.getSuccessors(alreadySeen)
            for (succ in successors) {
                if (isRelevantStmt(currentNode)) {
                    worklist.add(Pair(succ, setOf(currentNode)))
                } else {
                    worklist.add(Pair(succ, previousNodes))
                }
            }
        }

        return wpds
    }

    /** Creates rules based on [stmtNode] in the WPDS [wpds]. */
    private fun createRulesForStmt(
        wpds: WPDS<Node, Val, TypestateWeight>,
        functionDeclaration: FunctionDeclaration,
        previousStmt: Node,
        stmtNode: Node,
        valsInScope: MutableSet<Val>,
        skipTheseValsAtStmt: MutableMap<Node, Val>,
        tsNfa: NFA
    ) {
        val currentFunctionName = functionDeclaration.name

        /* First we create normal rules from previous stmt to the current stmt, simply propagating existing values. */
        val normalRules = createNormalRules(previousStmt, stmtNode, valsInScope, tsNfa)
        for (normalRule in normalRules) {
            // If this is a call into a known method, we skip immediate propagation. In that case,
            // data flows *into* the method.
            val skipIt = shouldBeSkipped(normalRule, skipTheseValsAtStmt)
            if (!skipIt) {
                wpds.addRule(normalRule)
            }
        }

        when {
            stmtNode is CallExpression && !Utils.isPhantom(stmtNode) ->
                // Handle calls into known methods (not a "phantom" method) by creating push rule.
                handleCallExpression(stmtNode, currentFunctionName, wpds, skipTheseValsAtStmt)
            stmtNode is VariableDeclaration ->
                handleVariableDeclaration(stmtNode, currentFunctionName, valsInScope)
            stmtNode is DeclarationStatement ->
                handleDeclarationStatement(
                    stmtNode,
                    currentFunctionName,
                    previousStmt,
                    wpds,
                    valsInScope,
                    tsNfa
                )
            stmtNode is ReturnStatement ->
                handleReturnStatement(
                    stmtNode,
                    tsNfa,
                    wpds,
                    functionDeclaration,
                    valsInScope,
                    previousStmt,
                    skipTheseValsAtStmt
                )
        }
    }

    private fun handleReturnStatement(
        returnStatement: ReturnStatement,
        tsNfa: NFA,
        wpds: WPDS<Node, Val, TypestateWeight>,
        functionDeclaration: FunctionDeclaration,
        valsInScope: MutableSet<Val>,
        previousStmt: Node,
        skipTheseValsAtStmt: MutableMap<Node, Val>
    ) {
        val currentFunctionName = functionDeclaration.name

        // return statements result in pop rules
        if (!returnStatement.isInferred) {
            val returnedVals = findReturnedVals(returnStatement)
            for (returnedVal in returnedVals) {
                val relevantNFATransitions =
                    tsNfa.transitions.filter { it.target.op == returnedVal.variable }.toHashSet()
                val weight =
                    if (relevantNFATransitions.isEmpty()) TypestateWeight.one()
                    else TypestateWeight(relevantNFATransitions)

                // Pop Rule for actually returned value
                val returnPopRule =
                    PopRule<Node, Val, TypestateWeight>(
                        Val(returnStatement.returnValue.name, currentFunctionName),
                        returnStatement,
                        returnedVal,
                        weight
                    )
                wpds.addRule(returnPopRule)
                log.debug("Adding pop rule {}", returnPopRule)
            }

            // Pop Rules for side effects on parameters
            val paramToValueMap = findParamToValues(functionDeclaration)
            if (paramToValueMap.containsKey(currentFunctionName)) {
                for ((first, second) in paramToValueMap[currentFunctionName]!!) {
                    val popRule =
                        PopRule<Node, Val, TypestateWeight>(
                            first,
                            returnStatement,
                            second,
                            TypestateWeight.one()
                        )
                    wpds.addRule(popRule)
                    log.debug("Adding pop rule {}", popRule)
                }
            }
        }

        // Create normal rule. Flow remains where it is.
        for (valInScope in valsInScope) {
            val normalRule: Rule<Node, Val, TypestateWeight> =
                NormalRule(
                    valInScope,
                    previousStmt,
                    valInScope,
                    returnStatement,
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
                log.debug("Adding normal rule {}", normalRule)
                wpds.addRule(normalRule)
            }
        }
    }

    private fun handleDeclarationStatement(
        stmtNode: DeclarationStatement,
        currentFunctionName: String,
        previousStmt: Node,
        wpds: WPDS<Node, Val, TypestateWeight>,
        valsInScope: MutableSet<Val>,
        tsNfa: NFA
    ) {
        // Handle declaration of new variables. "DeclarationStatements" result in a normal rule,
        // assigning rhs to lhs.

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
                // Handle function/method calls whose return value is assigned to a declared
                // variable.
                // A new data flow for the declared variable (declVal) is introduced.
                /*val normalRuleDeclared: Rule<Node, Val, TypestateWeight> =
                    NormalRule(
                        Val(GraphWPDS.EPSILON_NAME, currentFunctionName),
                        previousStmt,
                        declVal,
                        stmtNode,
                        TypestateWeight.one()
                    )
                log.debug("Adding normal rule for declaration {}", normalRuleDeclared)
                wpds.addRule(normalRuleDeclared)*/

                val rules = createNormalRules(previousStmt, rhs, valsInScope, tsNfa)
                rules.forEach { wpds.addRule(it) }

                // Add declVal to set of currently tracked variables
                valsInScope.add(declVal)
            } else if (rhs != null) {
                // Handle assignment from right side (rhs) to left side (lhs).
                //
                // We simply take rhs.getName() as a data source. This might be imprecise and need
                // further differentiation. For instance, if rhs is an expression (other than
                //  CallExpression), we might want to recursively handle data flows within that
                //  expression. This is currently not implemented as it is not needed for our use
                //  case and would add unneeded complexity.
                val rhsVal = Val(rhs.name, currentFunctionName)

                // Add declVal to set of currently tracked variables
                valsInScope.add(declVal)
                val normalRulePropagate: Rule<Node, Val, TypestateWeight> =
                    NormalRule(rhsVal, previousStmt, declVal, stmtNode, TypestateWeight.one())
                log.debug("Adding normal rule for assignment {}", normalRulePropagate)
                wpds.addRule(normalRulePropagate)
            }
        }
    }

    private fun handleVariableDeclaration(
        stmtNode: Node,
        currentFunctionName: String,
        valsInScope: MutableSet<Val>
    ) {
        // Add declVal to set of currently tracked variables
        val declVal = Val(stmtNode.name, currentFunctionName)
        valsInScope.add(declVal)
    }

    private fun handleCallExpression(
        stmtNode: CallExpression,
        currentFunctionName: String,
        wpds: WPDS<Node, Val, TypestateWeight>,
        skipTheseValsAtStmt: MutableMap<Node, Val>
    ) {
        // For calls to functions whose body is known, we create push/pop rule pairs. All arguments
        // flow into the parameters of the function. The "return site" is the statement to which
        // flow returns after the function call.
        val pushRules = createPushRules(stmtNode, currentFunctionName, stmtNode)
        for (pushRule in pushRules) {
            log.debug("  Adding push rule: {}", pushRule)
            wpds.addRule(pushRule)

            // Remember that arguments flow only into callee and do not bypass it.
            skipTheseValsAtStmt[pushRule.callSite] = pushRule.s1
        }
    }

    private fun shouldBeSkipped(
        normalRule: NormalRule<Node, Val, TypestateWeight>,
        skipTheseValsAtStmt: Map<Node, Val>
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
     * Returns a set of nodes which are successors of this node in the EOG and are not contained in
     * [alreadySeen].
     *
     * @param alreadySeen
     * @return
     */
    private fun Node.getSuccessors(alreadySeen: HashSet<Node>): HashSet<Node> {
        val unseenSuccessors = HashSet<Node>()
        for (succ in this.nextEOG) {
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
        previousStmt: Node,
        node: Node,
        valsInScope: Set<Val>,
        tsNfa: NFA
    ): Set<NormalRule<Node, Val, TypestateWeight>> {
        val result: MutableSet<NormalRule<Node, Val, TypestateWeight>> = HashSet()
        val map = node.typestateTransitionTrigger(tsNfa)

        // Create normal rule. Flow remains where it is.
        for (valInScope in valsInScope) {
            // Determine weight
            val relevantNFATransitions =
                tsNfa.transitions
                    // .filter { node.triggersTypestateTransition(it.target.base, it.target.op) }
                    .filter { map[it.target.base]?.contains(it.target.op) == true }
                    .toHashSet()
            val weight =
                if (relevantNFATransitions.isEmpty()) TypestateWeight.one()
                else TypestateWeight(relevantNFATransitions)
            val normalRule = NormalRule(valInScope, previousStmt, valInScope, node, weight)
            log.debug("Adding normal rule {}", normalRule)
            result.add(normalRule)
        }
        return result
    }

    /**
     * Returns true if the given CPG node will result in a transition from any typestate into the
     * typestate denoted by [op].
     *
     * @param markInstance The current MARK instance.
     * @param op The target typestate, indicated by a MARK op.
     *
     * @return
     */
    @Deprecated("Replaced by typestateTransitionTrigger")
    private fun Node.triggersTypestateTransition(markInstance: String?, op: String): Boolean {
        /*
        TODO Future improvement: This method is repeatedly called for different "ops" and thus repeats quite some work.
        The "op" paramenter should be removed and the method should return a (possibly empty) set of target typestates (=ops)
        that would be reached, so this method needs to be called only once per CPG node.
        */
        if (markInstance == null || this.name == "") {
            return false
        }

        // Get the MARK entity of the markInstance
        val mEntity = rule.entityReferences[markInstance]
        if (mEntity?.second == null) {
            return false
        }

        // For non-OO languages, we need to check valInScope against function args and return value
        // (=assignee)
        var assigneeVar: String? = null
        var assignerFqn: String? = null
        if (this is VariableDeclaration) {
            assigneeVar = this.name
            if (this.initializer is CallExpression) {
                assignerFqn = (this.initializer as CallExpression).fqn
            }
        } else if (this is CallExpression) {
            assignerFqn = this.fqn
        }

        // For method calls we collect the "base", its type(s), and the method arguments.
        var types: Set<Type> = HashSet()
        val arguments: MutableList<Expression> = ArrayList()
        if (this is CallExpression) {
            val base: Expression? = this.base

            // even though base is annotated @NotNull, it sometimes is null
            if (base != null) {
                types = base.possibleSubTypes
            }
            arguments.addAll(this.arguments)
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
                    if (
                        assignerFqn != null &&
                            opStatement.call.name == assignerFqn &&
                            (assigneeVar != null // is return value assigned to valInScope?
                            ||
                                arguments.isEmpty() ||
                                argumentsMatchParameters(
                                    opStatement.call.params,
                                    (this as CallExpression).arguments
                                ))
                    ) {
                        return true
                    }
                } else {
                    for (type in types) {
                        if (
                            type.typeName.startsWith(
                                Utils.getScope(opStatement.call.name).replace("::", ".")
                            ) // Dirty: startsWith() to ignore modifiers (such as "*").
                            && opStatement.call.name.endsWith(this.name)
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
     * Returns the set of ops for the given CPG node that will result in a transition from any
     * typestate into another.
     *
     * @param markInstance The current MARK instance.
     *
     * @return
     */
    private fun Node.typestateTransitionTrigger(tsNfa: NFA): Map<String, Set<String>> {
        val opsMap = mutableMapOf<String, MutableSet<String>>()

        for (markInstance in tsNfa.transitions.map { it.target.base }) {
            if (markInstance == null) {
                continue
            }

            // Get the MARK entity of the markInstance
            val mEntity = rule.entityReferences[markInstance]
            if (mEntity?.second == null) {
                continue
            }

            // For non-OO languages, we need to check valInScope against function args and return
            // value
            // (=assignee)
            var assigneeVar: String? = null
            var assignerFqn: String? = null
            if (this is VariableDeclaration) {
                assigneeVar = this.name
                if (this.initializer is CallExpression) {
                    assignerFqn = (this.initializer as CallExpression).fqn
                }
            } else if (this is CallExpression) {
                assignerFqn = this.fqn
            }

            // For method calls we collect the "base", its type(s), and the method arguments.
            var types: Set<Type> = HashSet()
            val arguments: MutableList<Expression> = ArrayList()
            if (this is CallExpression) {
                val base: Expression? = this.base

                // even though base is annotated @NotNull, it sometimes is null
                if (base != null) {
                    types = base.possibleSubTypes
                }
                arguments.addAll(this.arguments)
            }

            val ops = opsMap.computeIfAbsent(markInstance) { mutableSetOf() }

            for (o in mEntity.second!!.ops) {
                for (opStatement in o.statements) {
                    if (types.isEmpty()) {
                        /* Failure to resolve type or a function call (e.g. EVP_EncryptInit), rather than a method call.
                          In case of function calls/non-OO languages, we ignore the type of the (non-existing) base
                          and simply check if the call stmt matches the one in the "op" spec.
                          "Matches" means that it matches the function and valInScope is either one of the arguments or is assigned the call's return value.
                        */
                        if (
                            assignerFqn != null &&
                                opStatement.call.name == assignerFqn &&
                                (assigneeVar != null // is return value assigned to valInScope?
                                ||
                                    arguments.isEmpty() ||
                                    argumentsMatchParameters(
                                        opStatement.call.params,
                                        (this as CallExpression).arguments
                                    ))
                        ) {
                            ops += o.name
                        }
                    } else {
                        for (type in types) {
                            if (
                                type.typeName.startsWith(
                                    Utils.getScope(opStatement.call.name).replace("::", ".")
                                ) // Dirty: startsWith() to ignore modifiers (such as "*").
                                && opStatement.call.name.endsWith(this.name)
                            ) {
                                // TODO should rather compare fully qualified names instead of
                                // "endsWith"
                                ops += o.name
                            }
                        }
                    }
                }
            }
        }

        return opsMap
    }

    /**
     * Finds the mapping from function parameters to arguments of calls to this method. This is
     * needed for later construction of pop rules.
     *
     * @param callee
     * @return
     */
    private fun findParamToValues(callee: FunctionDeclaration): Map<String, Set<Pair<Val, Val>>> {
        val result = mutableMapOf<String, Set<Pair<Val, Val>>>()
        val calleeName = callee.name

        // the function declaration is connected to their call expressions by a DFG edge
        val calls = callee.nextDFG.filterIsInstance<CallExpression>()
        for (ce in calls) {
            val caller = ce.containingFunction
            if (caller == null) {
                log.error("Unexpected: Null Node object for FunctionDeclaration")
                continue
            }
            val args = ce.arguments
            val params = callee.parameters
            val pToA = HashSet<Pair<Val, Val>>()

            for (i in 0 until params.size.coerceAtMost(args.size)) {
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
     * ```
     * void blubb() { int x = bla(); }
     *
     * int bla() { return 42; }
     * ```
     *
     * @param returnStatement
     * @return
     */
    private fun findReturnedVals(returnStatement: ReturnStatement): Set<Val> {
        val returnedVals = mutableSetOf<Val>()

        // Follow along "DFG" edges from the return statement to the CallExpression that initiated
        // the call. Then check if there is a "DFG" edge from that CallExpression to a
        // VariableDeclaration.
        val calls = returnStatement.nextDFG.filterIsInstance<CallExpression>()
        for (call in calls) {
            // We found the call site into our method. Now see if the return value is used.
            val nextDfgAfterCall = call.nextDFG.firstOrNull()
            nextDfgAfterCall?.let {
                var returnVar = ""

                if (it is VariableDeclaration || it is DeclaredReferenceExpression) {
                    // return value is used. Remember variable name.
                    returnVar = it.name
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
     * @param call
     * @param currentFunctionName
     * @param currentStmt
     * @return
     */
    private fun createPushRules(
        call: CallExpression,
        currentFunctionName: String,
        currentStmt: Node
    ): Set<PushRule<Node, Val, TypestateWeight>> {
        // Return site(s). Actually, multiple return sites will only occur in case of exception
        // handling.
        // TODO: support multiple return sites
        val returnSite = currentStmt.nextStatement
        val returnSites: List<Statement> = returnSite?.let { listOf(it) } ?: emptyList()

        // Arguments of function call
        val argVals = argumentsToVals(call, currentFunctionName)
        val pushRules: MutableSet<PushRule<Node, Val, TypestateWeight>> = HashSet()
        for (potentialCallee in call.invokes) {
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
                        val stmt = returnSiteVertex
                        val pushRule =
                            PushRule(
                                argVals[i],
                                currentStmt,
                                parmVals[i],
                                potentialCallee,
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
     * Returns a list of the function parameters of [func], each wrapped as a [Val].
     *
     * @param func the function declaration
     * @return
     */
    private fun parametersToVals(func: FunctionDeclaration): List<Val> {
        return func.parameters.map { Val(it.name, func.name) }
    }

    /**
     * Returns a list of the call expression's arguments of [call], each wrapped as a [Val].
     *
     * @param call the call expression
     * @return
     */
    private fun argumentsToVals(call: CallExpression, currentFunctionName: String): List<Val> {
        return call.arguments.map { Val(it.name, currentFunctionName) }
    }

    companion object {
        private val log = LoggerFactory.getLogger(TypestateAnalysis::class.java)
    }
}
