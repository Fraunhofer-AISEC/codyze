package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.ordering

import de.fraunhofer.aisec.cpg.analysis.fsm.FSMBuilderException
import de.fraunhofer.aisec.cpg.graph.Node

data class Trace(val state: State, val cpgNode: Node, val edge: Edge)

/** A representation of a deterministic finite automaton (DFA). */
class DFA(states: Set<State> = setOf()) : FSM(states) {
    private val _executionTrace = mutableListOf<Trace>()
    val executionTrace: List<Trace>
        get() = _executionTrace
    val currentState: State?
        get() = _executionTrace.lastOrNull()?.edge?.nextState ?: _states.firstOrNull { it.isStart }

    /** True, if the DFA is currently in an accepting state. */
    val isAccepted: Boolean
        get() = currentState?.isAcceptingState == true

    /**
     * Creates an edge between two nodes with a given label (operator and optional base).
     *
     * It checks if [from] already has an outgoing edge with the same [base] and [op] but to another
     * target node. If so, it throws a [FSMBuilderException].
     */
    override fun addEdge(from: State, edge: Edge) {
        if (from.outgoingEdges.any { e -> e.matches(edge) && e.nextState != edge.nextState }) {
            throw FSMBuilderException(
                "State already has an outgoing edge with the same label but a different target!"
            )
        }
        super.addEdge(from, edge)
    }

    /**
     * Checks if the transition with operator [op] is possible from the current state of the FSM. If
     * so, it updates the state of the FSM and returns `true`. If no transition is possible, returns
     * `false`. Collects the old state, the edge and the cpg [cpgNode] in the [executionTrace]
     */
    fun makeTransitionWithOp(op: String, cpgNode: Node): Boolean {
        checkNotNull(currentState) { "Cannot make transition because the FSM does not have a starting state!" }

        val possibleEdges = currentState!!.outgoingEdges.filter { e -> e.op == op }
        check(possibleEdges.size <= 1) { "Transition $op is not deterministic for current state $currentState" }
        val edgeToFollow = possibleEdges.firstOrNull()
        return if (edgeToFollow != null) {
            _executionTrace.add(Trace(state=currentState!!, cpgNode=cpgNode, edge=edgeToFollow))
            true
        } else {
            false
        }
    }

    override fun equals(other: Any?) = super.equals(other) && other is DFA && other.currentState == currentState

//    /** Copies the FSM to enable multiple independent branches of execution. */
//    fun deepCopy(): DFA {
//        val newDFA = DFA()
//        val startingState = this.states.first { it.isStart }
//        newDFA.states.addAll(startingState.cloneRecursively())
//
//        val newStart = newDFA.states.first { it.isStart }
//        if (executionTrace.size > 0) {
//            newDFA.currentState = newStart
//            newDFA.executionTrace.add(
//                Triple(newStart, executionTrace[0].second, BaseOpEdge(de.fraunhofer.aisec.cpg.analysis.fsm.DFA.EPSILON, "", newStart))
//            )
//            for (t in this.executionTrace.subList(1, executionTrace.size)) {
//                val traceState = newDFA.states.first { it.name == t.first.name }
//                newDFA.executionTrace.add(
//                    Triple(
//                        traceState,
//                        t.second,
//                        traceState.nextNodeWithLabelOp(t.third.op)!!.second
//                    )
//                )
//            }
//        }
//        newDFA.currentState = newDFA.states.firstOrNull { it.name == this.currentState?.name }
//        return newDFA
//    }
}
