package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.ordering

/** A representation of a non-deterministic finite automaton (NFA). */
class NFA(states: Set<State> = setOf()): FSM(states) {
    override fun equals(other: Any?) = super.equals(other) && other is NFA

//    /** Copies the FSM to enable multiple independent branches of execution. */
//    fun deepCopy(): NFA {
//        val nfa = NFA()
//        val startingState = states.firstOrNull { it.isStart }
//        nfa.states.addAll(startingState.cloneRecursively())
//        return nfa
//    }

//    /**
//     * Compute the ε-closure for this ε-NFA and then use the [powerset construction](https://en.wikipedia.org/wiki/Powerset_construction)
//     * algorithm to convert it to a [DFA]
//     */
//    fun toDfa(): DFA {
//        states
//    }
}
