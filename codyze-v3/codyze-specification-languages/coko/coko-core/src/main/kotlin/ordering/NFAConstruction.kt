package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.ordering

/**
 * Combine multiple NFAs into one big NFA, where the first NFA must be followed by the second NFA, and the second NFA must be followed by the third NFA,...
 * Constructs the combined NFA using Thompson's construction algorithm
 * @see [YouTube](https://youtu.be/HLOAwCCYVxE?t=380)
 */
internal fun concatenateMultipleNfa(vararg multipleNFA: NFA): NFA {
    fun concatenateTwoNfa(firstNfa: NFA, secondNfa: NFA): NFA {
        // first, it's important to make sure that all states have unique names because
        // states are only differentiated by their name
        secondNfa.states.map { it.name = (it.name+firstNfa.states.size) }

        // First create edges from all accepting states of the first NFA to all start states of the second NFA
        // add the epsilon edges to all accepting states of the first NFA
        secondNfa.states.filter { it.isStart }.map {
            val edge = Edge(op=FSM.EPSILON, nextState = it)
            firstNfa.states.filter { it.isAcceptingState }.map { it.outgoingEdges.add(edge) }
        }
        // then make the accepting states in the first NFA non-accepting
        firstNfa.states.filter { it.isAcceptingState }.map { it.isAcceptingState = false }
        // then make the starting states in the second NFA non-starting
        secondNfa.states.filter { it.isStart }.map { it.isStart = false }

        // lastly combine both NFAs into a single one
        return NFA(firstNfa.states + secondNfa.states)
    }

    var currentNfa = multipleNFA.first()
    for (nfa in multipleNFA.drop(1)) {
        currentNfa = concatenateTwoNfa(currentNfa, nfa)
    }
    return currentNfa
}

/**
 * Combine two NFAs into one big NFA, where either the first or second NFA is OK
 * Constructs the combined NFA using Thompson's construction algorithm
 * @see [YouTube](https://youtu.be/HLOAwCCYVxE?t=306)
 */
internal fun alternateTwoNfa(firstNfa: NFA, secondNfa: NFA): NFA {
    // first, it's important to make sure that all states have unique names because
    // states are only differentiated by their name
    secondNfa.states.map { it.name = (it.name+firstNfa.states.size) }

    // first create new epsilon edges to the start states of both NFAs
    val epsilonEdgesFirst = firstNfa.states.filter { it.isStart }.map { Edge(op=FSM.EPSILON, nextState = it) }
    val epsilonEdgesSecond = secondNfa.states.filter { it.isStart }.map { Edge(op=FSM.EPSILON, nextState = it) }

    // then make the old start states non-start states
    firstNfa.states.filter { it.isStart }.map { it.isStart = false }
    secondNfa.states.filter { it.isStart }.map { it.isStart = false }

    // merge both NFAs into a single one
    val combinedNfa = NFA(firstNfa.states + secondNfa.states)

    // add a new start state
    val newStartState = combinedNfa.addState(isStart = true)

    // and lastly add the epsilonEdges to the new start state
    epsilonEdgesFirst.forEach { newStartState.outgoingEdges.add(it) }
    epsilonEdgesSecond.forEach { newStartState.outgoingEdges.add(it) }

    return combinedNfa
}

/**
 * Add a maybe ('*') qualifier to an existing NFA
 * @see [YouTube](https://youtu.be/HLOAwCCYVxE?t=478)
 */
internal fun addMaybeQuantifierToNFA(nfa: NFA): NFA {
    // create a new start state (make is starting state and accepting state later to only ever have one starting state in the graph)
    val startState = nfa.addState()
    // create epsilon edges from the accepting states to the new start state and add them to the NFA
    nfa.states.filter { it.isAcceptingState }.map {
        val edge = Edge(op=FSM.EPSILON, nextState = startState)
        nfa.addEdge(it, edge)
    }
    // create epsilon edges from the new start state to all previous start states and add them to the NFA
    nfa.states.filter { it.isStart }.map {
        val edge = Edge(op=FSM.EPSILON, nextState = it)
        nfa.addEdge(startState, edge)
    }
    // convert the old start states to non-start states
    nfa.states.filter { it.isStart }.map { it.isStart = false }

    // convert [startState] to a start and accepting state (this is done now to only ever have one start state in the nfa)
    startState.apply {
        isStart = true
        isAcceptingState = true
    }

    return nfa
}

/**
 * Add an option ('?') qualifier to an existing NFA.
 * This is very similar to @see [addMaybeQuantifierToNFA]
 */
internal fun addOptionQuantifierToNFA(nfa: NFA): NFA {
    // create a new start state (make is starting state and accepting state later to only ever have one starting state in the graph)
    val startState = nfa.addState()

    // create epsilon edges from the new start state to all previous start states and add them to the NFA
    nfa.states.filter { it.isStart }.map {
        val edge = Edge(op=FSM.EPSILON, nextState = it)
        nfa.addEdge(startState, edge)
    }
    // convert the old start states to non-start states
    nfa.states.filter { it.isStart }.map { it.isStart = false }

    // convert [startState] to a start and accepting state (this is done now to only ever have one start state in the nfa)
    startState.apply {
        isStart = true
        isAcceptingState = true
    }

    return nfa
}
