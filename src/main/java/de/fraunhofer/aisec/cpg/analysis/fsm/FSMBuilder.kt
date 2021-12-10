package de.fraunhofer.aisec.cpg.analysis.fsm

import de.fraunhofer.aisec.mark.markDsl.*

/**
 * Class to build an [FSM] based on some input.
 */
class FSMBuilder {
    /**
     * Constructs a [DFA] based on the given [expr].
     */
    fun sequenceToDFA(expr: OrderExpression): DFA {
        val dfa = DFA()
        val q1 = dfa.addState(isStart = true)
        val edgeAddResult = addEdgesToDFA(expr.exp, dfa, mutableSetOf(q1))
        // The last states are accepting states because this is where all
        // possible sequences of statements in the MARK rule can finish.
        edgeAddResult.endStates.forEach { s -> s.isAcceptingState = true }
        return dfa
    }

    /**
     * Constructs a [DFA] from the given [expression] by incrementally adding states and edges
     * to [dfa]. [previousStates] is the set of predecessor states for the next edge in the DFA.
     *
     * For each processed expression, it returns an [EdgeAddResult] which keeps:
     * - The last states. This is where the next edges have to be added.
     * - The first states of the expression and the label to reach it. These are the states
     *   where a potential loop (with * or +) would end in if the given label is followed.
     */
    private fun addEdgesToDFA(
        expression: Expression,
        dfa: DFA,
        previousStates: MutableSet<State>
    ): EdgeAddResult {
        if (expression is Terminal) {
            // This is a single transition p -l-> q, with l = (expr.entity).(expr.op).
            // We take the last state(s) and add the respective edge.
            // We return q as possible target for a loop (* or +) with label l of the edge.
            val newState = dfa.addState()
            previousStates.forEach { prev -> dfa.addEdge(prev, newState, expression.op, expression.entity) }
            return EdgeAddResult(
                mutableSetOf(newState),
                mutableSetOf(LoopTarget(newState, expression.op, expression.entity))
            )
        } else if (expression is SequenceExpression) {
            // We have a sequence of expressions p -l1-> q -l2-> r ...
            // First, we build the edge p --> q and use q as starting point for the next
            // edges. q is also the target of a potential loop (actually, q depends on what
            // the lhs of the SequenceExpression is and could also be a set of nodes).
            val lhsStartEndStates = addEdgesToDFA(expression.left, dfa, previousStates)
            val rhsStartEndStates = addEdgesToDFA(expression.right, dfa, lhsStartEndStates.endStates)
            return EdgeAddResult(rhsStartEndStates.endStates, lhsStartEndStates.possibleLoopTargets)
        } else if (expression is AlternativeExpression) {
            // We have two possible branches of execution, lhs and rhs. So, we build the
            // transitions p -l1-> l and p -l2-> r. Both, l and r can be used for the next
            // step for the next transition "merging" the paths together again. The two branches are
            // separated one from the other when evaluating lhs and rhs.
            // Both are possible loop targets (one loop ends in l with edge l1, one in r with l2).
            val lhsStartEndStates = addEdgesToDFA(expression.left, dfa, previousStates)
            val rhsStartEndStates = addEdgesToDFA(expression.right, dfa, previousStates)
            lhsStartEndStates.endStates.addAll(rhsStartEndStates.endStates)
            lhsStartEndStates.possibleLoopTargets.addAll(rhsStartEndStates.possibleLoopTargets)
            return lhsStartEndStates
        } else if (expression is RepetitionExpression) {
            if (expression.op.equals("?")) {
                // Repeat 0x or 1x. This means that we build the edge p -l-> q but
                // for the next node, we keep p as predecessor (which will result in
                // an edge p -l2-> r and q -l2-> r).
                val newStartEndNodes = addEdgesToDFA(expression.expr, dfa, previousStates)
                newStartEndNodes.endStates.addAll(previousStates)
                return newStartEndNodes
            } else if (expression.op.equals("*")) {
                // Repeat 0-infinity times. This means that we build the edge p -l-> q but
                // for the next node, we keep p as predecessor (which will result in
                // an edge p -l2-> r and q -l2-> r).
                // We take the target of the loop which was computed by previous expressions
                // And add the edges to the respective nodes (identified from previous
                // computations).
                val newStartEndNodes = addEdgesToDFA(expression.expr, dfa, previousStates)
                for (target in newStartEndNodes.possibleLoopTargets) {
                    newStartEndNodes.endStates.forEach { endState ->
                        dfa.addEdge(endState, target.state, target.loopOp, target.loopBase)
                    }
                }
                newStartEndNodes.endStates.addAll(previousStates)
                return newStartEndNodes
            } else if (expression.op.equals("+")) {
                // We take the target of the loop which was computed by previous expressions
                // And add the edges to the respective nodes (identified from previous
                // computations).
                val newStartEndNodes = addEdgesToDFA(expression.expr, dfa, previousStates)
                for (target in newStartEndNodes.possibleLoopTargets) {
                    newStartEndNodes.endStates.forEach { endState ->
                        dfa.addEdge(endState, target.state, target.loopOp, target.loopBase)
                    }
                }
                return newStartEndNodes
            }
        }
        return EdgeAddResult(mutableSetOf(), mutableSetOf())
    }

    private class EdgeAddResult(
        val endStates: MutableSet<State>,
        val possibleLoopTargets: MutableSet<LoopTarget>
    )

    private class LoopTarget(val state: State, val loopOp: String, val loopBase: String)
}
