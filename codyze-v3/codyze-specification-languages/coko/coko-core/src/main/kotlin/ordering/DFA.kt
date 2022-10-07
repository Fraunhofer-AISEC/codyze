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

    /** Create a new state and add it to this NFA. Returns the newly created state. */
    override fun addState(isStart: Boolean, isAcceptingState: Boolean): State {
        val newState =
            DfaState(name = states.size, isStart = isStart, isAcceptingState = isAcceptingState)
        addState(newState)
        return newState
    }

    /** Create a shallow copy */
    override fun copy() = DFA(states = states)

    /**
     * Checks if the transition with operator [op] is possible from the current state of the FSM. If
     * so, it updates the state of the FSM and returns `true`. If no transition is possible, returns
     * `false`. Collects the old state, the edge and the cpg [cpgNode] in the [executionTrace]
     */
    fun makeTransitionWithOp(op: String, cpgNode: Node): Boolean {
        checkNotNull(currentState) {
            "Cannot make transition because the FSM does not have a starting state!"
        }

        val possibleEdges = currentState!!.outgoingEdges.filter { e -> e.op == op }
        check(possibleEdges.size <= 1) {
            "Transition $op is not deterministic for current state $currentState"
        }
        val edgeToFollow = possibleEdges.firstOrNull()
        return if (edgeToFollow != null) {
            _executionTrace.add(
                Trace(state = currentState!!, cpgNode = cpgNode, edge = edgeToFollow)
            )
            true
        } else false
    }

    /**
     * Implements a DFA equivalence check as described [here](https://arxiv.org/abs/0907.5058) in
     * algorithm 2. This equivalence check is an improvement on the original algorithm developed by
     * Hopcroft and Karp described [here](https://ecommons.cornell.edu/handle/1813/5958) and checks
     * whether two DFAs accept the same language.
     *
     * The functions needed for the algorithm:
     * - MAKE-SET(i): creates a new set (singleton) for one element i.
     * - FIND-SET(i): returns the identifier Si of the set which contains i. If no set containing i
     * is found, MAKE-SET(i) is called and the set Si = {i} is created.
     * - UNION(i,j): combines the sets identified by i and j in a new set S = Si ∪ Sj; Si and Sj are
     * destroyed.
     *
     * The algorithm:
     * 1. On input (M1, M2), where M1 and M2 are DFAs with start states p0 and q0 respectively:
     * 2. Initialize a set for both start states p0 and q0: MAKE-SET(p0), MAKE-SET(q0).
     * 3. Begin with the start states. UNION(p0, q0) and push the pair {p0, q0} on a stack.
     * 4. Until the stack is empty:
     * ```
     *     - 4.1 Pop pair {p1, q1} from the stack.
     *     - 4.2 Check if p1 and q1 are accepting states of their respective DFA. If one of the two states is an accepting state and the other is a non-accepting state, then the two automata are not equivalent: return False
     *     - 4.3 For each symbol a in Σ (so for all transitions possible from either state):
     *          - i. Let p' and q' be the destination states when performing a on p1 and q1 respectively: p' = δ(p1, a) and q' = δ(q1, a)
     *          - ii. Let r1 and r2 be the names of the sets containing the successors to p1 and q1: r1 = FIND-SET(p') and r2 = FIND-SET(q')
     *          - iii. If r1 != r2, then UNION(r1, r2) and push the pair {p', q'} on the stack.
     * ```
     * 5. If 4. was completed without aborting in 4.2, the two DFAs accept the same language and are
     * thus equivalent: return True
     */
    override fun equals(other: Any?): Boolean {
        if (other is FSM) {
            val stateSets = mutableListOf<Set<State>>()

            /** MAKE(i): creates a new set (singleton) for one element i */
            fun makeSet(state: State) = stateSets.add(setOf(state))

            /**
             * FIND-SET(i): returns the identifier Si of the set which contains i. If no set
             * containing i is found, MAKE(i) is called and the set Si = {i} is created.
             */
            fun findSet(state: State) =
                stateSets.find { it.contains(state) }?.let { stateSets.indexOf(it) }
                    ?: run {
                        makeSet(state)
                        stateSets.lastIndex
                    }

            /**
             * UNION(i,j): combines the sets identified by i and j in a new set S = Si ∪ Sj; Si and
             * Sj are destroyed.
             */
            fun union(stateSet1: Set<State>, stateSet2: Set<State>): Set<State> {
                stateSets.remove(stateSet1)
                stateSets.remove(stateSet2)
                stateSets.add(stateSet1 + stateSet2)
                return stateSet1 + stateSet2
            }

            // for some reason, this when statement sometimes breaks the compilation...
            // it that is the case, a gradle build clean should help
            val otherDfa =
                when (other) {
                    is NFA -> other.toDfa()
                    is DFA ->
                        other.deepCopy() // make sure to create a deep copy to not alter the passed
                // object when changing the state names
                }
            // it's important to make sure that all states have unique names because
            // states are only differentiated by their name
            otherDfa.states.map { it.name += states.size }

            // first get the start state of both DFAs and make sure that both DFAs have exactly one
            val p0 = states.singleOrNull { it.isStart }
            val q0 = otherDfa.states.singleOrNull { it.isStart }
            check(p0 != null && q0 != null) {
                "In order to compare to FSMs, both must have exactly one start state."
            }

            //
            // this is where the actual algorithm as described in the paper starts
            makeSet(p0) // MAKE-SET(p0)
            makeSet(q0) // MAKE-SET(q0)
            val statesToExplore = ArrayDeque<Pair<State, State>>() // S = ∅

            union(setOf(p0), setOf(q0)) // UNION(p0, q0)
            statesToExplore.add(p0 to q0) // PUSH(p0, q0)

            while (statesToExplore.size > 0) {
                val (p1, q1) = statesToExplore.removeLast() // POP(S)
                if (p1.isAcceptingState != q1.isAcceptingState)
                    return false // if ε(p)!=ε(q): return False

                val allPossibleEdges =
                    setOf(p1, q1).flatMap {
                        it.outgoingEdges
                    } // get all possible transitions for both states
                for (edge in allPossibleEdges) { // for a∈Σ
                    val pPrime = p1.outgoingEdges.find { it.matches(edge) }?.nextState // δ(p1,a)
                    val qPrime = q1.outgoingEdges.find { it.matches(edge) }?.nextState // δ(q1,a)
                    if (pPrime == null || qPrime == null)
                        return false // this is because [findSet] cannot handle null as input
                    val r1 = findSet(pPrime) // FIND-SET(p')
                    val r2 = findSet(qPrime) // FIND-SET(q')
                    if (r1 != r2) {
                        union(stateSets[r1], stateSets[r2]) // UNION(r1, r2)
                        statesToExplore.add(pPrime to qPrime) // PUSH(p', q')
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
            newDFA.makeTransitionWithOp(op = trace.edge.op, cpgNode = trace.cpgNode)
        }
        return newDFA
    }
}
