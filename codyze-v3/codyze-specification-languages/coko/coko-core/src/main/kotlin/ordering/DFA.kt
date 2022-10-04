package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.ordering

import de.fraunhofer.aisec.cpg.graph.Node

data class Trace(val state: State, val cpgNode: Node, val edge: Edge)

/** A representation of a deterministic finite automaton (DFA). */
class DFA(states: Set<State> = setOf()) : FSM(states) {
    private val _executionTrace = mutableListOf<Trace>()
    val executionTrace: List<Trace>
        get() = _executionTrace
    val currentState: State?
        get() = executionTrace.lastOrNull()?.edge?.nextState ?: states.firstOrNull { it.isStart }

    /** True, if the DFA is currently in an accepting state. */
    val isAccepted: Boolean
        get() = currentState?.isAcceptingState == true

    /**
     * Create a new state and add it to this NFA. Returns the newly created state.
     */
    override fun addState(isStart: Boolean, isAcceptingState: Boolean): State {
        val newState = DfaState(name=states.size, isStart=isStart, isAcceptingState=isAcceptingState)
        addState(newState)
        return newState
    }

    /**
     * Create a shallow copy
     */
    override fun copy() = DFA(states=states)

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
        } else false
    }

    /**
     * Implements a DFA equivalence check as described [here](https://arxiv.org/abs/0907.5058) in algorithm 2.
     * This equivalence check is closely related to the check by Hopcroft and Karp described [here](https://ecommons.cornell.edu/handle/1813/5958)
     * and checks whether two DFAs accept the same language.
     */
    override fun equals(other: Any?): Boolean {
        if (other is FSM) {
            val stateSets = mutableListOf<Set<State>>()

            fun make(state: State) = stateSets.add(setOf(state))
            fun find(state: State) =
                stateSets.find { it.contains(state) }?.let { stateSets.indexOf(it) } ?:
                run {
                    stateSets.add(setOf(state))
                    stateSets.lastIndex
                }
            fun union(stateSet1: Set<State>, stateSet2: Set<State>): Set<State> {
                stateSets.remove(stateSet1)
                stateSets.remove(stateSet2)
                stateSets.add(stateSet1+stateSet2)
                return stateSet1 + stateSet2
            }

            val otherDfa: DFA = when(other) {
                is NFA -> other.toDfa()
                is DFA -> other.deepCopy()  // make sure to create a deep copy to not change the passed object
            }
            // it's important to make sure that all states have unique names because
            // states are only differentiated by their name
            otherDfa.states.map { it.name = (it.name+states.size) }

            // first get the start state of both DFAs and make sure that both DFAs have exactly one
            val startStateThis = states.singleOrNull { it.isStart }
            val startStateOther =
                otherDfa.states.singleOrNull { it.isStart }
            check(startStateThis != null && startStateOther != null){ "In order to compare to FSMs, both must have exactly one start state." }

            make(startStateThis)  // MAKE(p0)
            make(startStateOther) // MAKE(q0)
            val statesToExplore = ArrayDeque<Pair<State, State>>()  // S = ∅

            union(setOf(startStateThis), setOf(startStateOther))  // UNION(p0, q0, q0)
            statesToExplore.add(startStateThis to startStateOther)  // PUSH(S, p0, q0)
            while (statesToExplore.size > 0) {
                val (p, q) = statesToExplore.removeFirst()  // POP(S)
                if (p.isAcceptingState != q.isAcceptingState) return false  // if ε(p)=ε(q): return False

                val allPossibleEdges = setOf(p,q).flatMap { state -> state.outgoingEdges.filter { edge -> edge.op != NFA.EPSILON } }
                for (edge in allPossibleEdges) {  // for a∈Σ
                    val r = p.outgoingEdges.find { it.matches(edge) }?.nextState
                    val s = q.outgoingEdges.find { it.matches(edge) }?.nextState
                    if (r == null || s == null) return false
                    val rIndex = find(r)  // FIND(δ(p,a))
                    val sIndex = find(s)  // FIND(δ(q,a))
                    if (rIndex != sIndex) {
                        union(stateSets[rIndex], stateSets[sIndex])  // UNION(r,s,s)
                        statesToExplore.add(r to s)  // PUSH(S ,(r,s))
                    }
                }
            }
            return true
        } else return false
    }

    /** Creates a deep copy the DFA to enable multiple independent branches of execution. */
    override fun deepCopy(): DFA {
        val newDFA = super.deepCopy() as DFA

        for (trace in executionTrace) {
            newDFA.makeTransitionWithOp(op=trace.edge.op, cpgNode = trace.cpgNode)
        }
        return newDFA
    }
}
