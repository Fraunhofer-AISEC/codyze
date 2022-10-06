package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.ordering

sealed class FSM(states: Set<State>) {

    private val _states: MutableSet<State> = mutableSetOf()
    val states: Set<State>
        get() = _states

    init {
        check(states.count { it.isStart } <= 1) { "Cannot create a FSM with multiple start states" }
        for (state in states) addState(state)
    }

    /** Generates a new state and adds it to this FSM. */
    abstract fun addState(isStart: Boolean = false, isAcceptingState: Boolean = false): State

    protected fun addState(state: State) {
        if (!_states.contains(state)) {
            if (state.isStart) {
                check(states.firstOrNull { it.isStart } == null) {
                    "This FSM already has a start state."
                }
            }
            _states.add(state)
        }
    }

    /** Creates an edge between two nodes with a given label (operator and optional base). */
    open fun addEdge(from: State, edge: Edge) {
        addState(from)
        addState(edge.nextState)
        from.addEdge(edge)
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

    protected abstract fun copy(): FSM

    /** Creates a deep copy of this FSM to enable multiple independent branches of execution. */
    open fun deepCopy(): FSM {
        val newFSM = copy()
        if (newFSM.states.isEmpty())
            return newFSM // this is needed for an empty FSM because empty ones cannot be deepCopied
        // currently

        val startingState = this.states.singleOrNull { it.isStart }
        check(startingState != null) { "Only FSMs with a single starting state can be deep copied" }
        startingState.deepCopy().forEach { newFSM.addState(it) }

        return newFSM
    }
}
