package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.ordering

abstract class FSM(states: Set<State>) {
    companion object {
        @JvmStatic val EPSILON: String = "ε"
    }

    protected val _states: MutableSet<State> = mutableSetOf()
    val states: Set<State>
        get() = _states

    init {
        check(states.count { it.isStart } <= 1) { "Cannot create a FSM with multiple start states" }
        _states.addAll(states)
    }

    /** Generates a new state and adds it to this FSM. */
    fun addState(isStart: Boolean = false, isAcceptingState: Boolean = false): State {
        val newState = State(name=_states.size, isStart=isStart, isAcceptingState=isAcceptingState)
        if (isStart) {
            check( _states.firstOrNull{ it.isStart } == null) { "This FSM already has a start state." }
        }
        _states.add(newState)
        return newState
    }

    /**
     * Creates an edge between two nodes with a given label (operator and optional base).
     */
    open fun addEdge(from: State, edge: Edge) {
        _states.add(from)
        _states.add(edge.nextState)
        from.outgoingEdges.add(edge)
    }

    override fun equals(other: Any?): Boolean {
        val res = other is FSM && other.states == states

        if (res) {
            for (s in states) {
                val otherState = (other as DFA).states.first { otherS -> s.name == otherS.name }
                if (s.outgoingEdges != otherState.outgoingEdges) {
                    return false
                }
            }
        }
        return res
    }

    /**
     * Generates the string representing this FSM in DOT format. This allows a simple visualization
     * of the resulting automaton.
     */
    fun toDotString(): String {
        var str = "digraph fsm {\n\t\"\" [shape=point];\n"
        var edges = ""
        for (s in states) {
            str +=
                if (s.isAcceptingState) {
                    "\t${s.name} [shape=doublecircle];\n"
                } else {
                    "\t${s.name} [shape=circle];\n"
                }
            if (s.isStart) {
                edges += "\t\"\" -> ${s.name};\n"
            }

            for (e in s.outgoingEdges) {
                edges += "\t${s.name} -> ${e.nextState.name} [label=\"${e.toDotLabel()}\"];\n"
            }
        }
        return "$str$edges}"
    }
}