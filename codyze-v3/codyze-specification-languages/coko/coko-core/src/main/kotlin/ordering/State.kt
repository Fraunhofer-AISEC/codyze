package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.ordering

/**
 * A simple class representing a state in an FSM.
 * - [name] is the name of the State and must be unique for the FSM.
 * - [isStart] indicates if it is the starting state.
 * - [isAcceptingState] indicates if this State accepts the FSM (in our case, this means that the
 * order of statements was correct).
 */
data class State(var name: Int, var isStart: Boolean = false, var isAcceptingState: Boolean = false){
    val outgoingEdges = mutableSetOf<Edge>()

    // override the default equals method to only use the name property
    override fun equals(other: Any?): Boolean {
        return (other as? State)?.name?.equals(name) == true
    }

    // override the default hashCode method to only use the name property
    override fun hashCode() = name.hashCode()

    override fun toString(): String {
        return (if (isStart) "(S) q$name" else "q$name") + (if (isAcceptingState) " (A)" else "")
    }

//    fun cloneRecursively(currentStates: MutableSet<State> = mutableSetOf()): MutableSet<State> {
//        if (currentStates.any { it.name == name }) {
//            return currentStates
//        }
//
//        val newState = State(name, isStart, isAcceptingState)
//        currentStates.add(newState)
//
//        for (outE in outgoingEdges) {
//            outE.nextState.cloneRecursively(currentStates)
//            newState.outgoingEdges.add(
//                Edge(
//                    outE.op,
//                    outE.base,
//                    currentStates.first { it.name == outE.nextState.name }
//                )
//            )
//        }
//        return currentStates
//    }
}
