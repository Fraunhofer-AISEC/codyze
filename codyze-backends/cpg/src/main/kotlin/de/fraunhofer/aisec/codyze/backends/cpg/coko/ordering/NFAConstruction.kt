/*
 * Copyright (c) 2022, Fraunhofer AISEC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fraunhofer.aisec.codyze.backends.cpg.coko.ordering

import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.ordering.*
import de.fraunhofer.aisec.cpg.analysis.fsm.Edge
import de.fraunhofer.aisec.cpg.analysis.fsm.NFA

/**
 * Constructs a NFA using Thompson's construction algorithm ([YouTube](https://youtu.be/HLOAwCCYVxE))
 */
fun OrderNode.toNfa(): NFA =
    when (this) {
        is TerminalOrderNode -> nfaForTerminalOrderNode(this)
        is SequenceOrderNode -> concatenateMultipleNfa(left.toNfa(), right.toNfa())
        is AlternativeOrderNode -> alternateTwoNfa(left.toNfa(), right.toNfa())
        is QuantifierOrderNode -> nfaForQuantifierOrderNode(this)
    }

/**
 * Construct a NFA for a single node using Thompson's construction algorithm
 * ([YouTube](https://youtu.be/HLOAwCCYVxE?t=237))
 */
internal fun nfaForTerminalOrderNode(node: TerminalOrderNode): NFA {
    val nfa = NFA()

    // create a start state
    val startState = nfa.addState(isStart = true)
    // create an accepting state
    val endState = nfa.addState(isAcceptingState = true)
    // create an edge connecting the two states
    val edge =
        Edge(
            op = node.opName,
            base = node.baseName,
            nextState = endState,
        )
    // add the edge to the NFA
    nfa.addEdge(startState, edge)
    return nfa
}

/**
 * Constructs a NFA using Thompson's construction algorithm ([YouTube](https://youtu.be/HLOAwCCYVxE))
 */
internal fun nfaForQuantifierOrderNode(node: QuantifierOrderNode): NFA =
    when (node.type) {
        OrderQuantifier.MAYBE -> addMaybeQuantifierToNFA(node.child.toNfa()) // '*'
        OrderQuantifier.OPTION -> addOptionQuantifierToNFA(node.child.toNfa()) // '?'
        OrderQuantifier.COUNT ->
            concatenateMultipleNfa(List(node.value as Int) { node.child.toNfa() })
        OrderQuantifier.ATLEAST ->
            concatenateMultipleNfa(
                concatenateMultipleNfa(List(node.value as Int) { node.child.toNfa() }),
                addMaybeQuantifierToNFA(node.child.toNfa())
            ) // '{..,}'
        OrderQuantifier.BETWEEN ->
            concatenateMultipleNfa(
                List((node.value as IntRange).first) { node.child.toNfa() } +
                    List((node.value as IntRange).last - (node.value as IntRange).first) {
                        addOptionQuantifierToNFA(node.child.toNfa())
                    }
            )
    }

/**
 * Add a maybe ('*') qualifier to an existing NFA ([YouTube](https://youtu.be/HLOAwCCYVxE?t=478))
 */
internal fun addMaybeQuantifierToNFA(nfa: NFA): NFA {
    // create a new start state (make is starting state and accepting state later to only ever have
    // one starting state in the graph)
    val startState = nfa.addState()
    // create epsilon edges from the accepting states to the new start state and add them to the NFA
    nfa.states
        .filter { it.isAcceptingState }
        .map {
            val edge = Edge(op = NFA.EPSILON, nextState = startState)
            nfa.addEdge(it, edge)
        }
    // create epsilon edges from the new start state to all previous start states and add them to
    // the NFA
    nfa.states
        .filter { it.isStart }
        .map {
            val edge = Edge(op = NFA.EPSILON, nextState = it)
            nfa.addEdge(startState, edge)
        }
    // convert the old start states to non-start states
    nfa.states.filter { it.isStart }.forEach { nfa.checkedChangeStateProperty(it, isStart = false) }

    // convert [startState] to a start and accepting state (this is done now to only ever have one
    // start state in the nfa)
    nfa.checkedChangeStateProperty(startState, isStart = true, isAcceptingState = true)

    return nfa
}

/**
 * Add an option ('?') qualifier to an existing NFA. This is very similar to @see
 * [addMaybeQuantifierToNFA]
 */
internal fun addOptionQuantifierToNFA(nfa: NFA): NFA {
    // create a new start state (make is starting state and accepting state later to only ever have
    // one starting state in the graph)
    val startState = nfa.addState()

    // create epsilon edges from the new start state to all previous start states and add them to
    // the NFA
    nfa.states
        .filter { it.isStart }
        .map {
            val edge = Edge(op = NFA.EPSILON, nextState = it)
            nfa.addEdge(startState, edge)
        }
    // convert the old start states to non-start states
    nfa.states.filter { it.isStart }.forEach { nfa.checkedChangeStateProperty(it, isStart = false) }

    // convert [startState] to a start and accepting state (this is done now to only ever have one
    // start state in the nfa)
    nfa.checkedChangeStateProperty(startState, isStart = true, isAcceptingState = true)

    return nfa
}

/**
 * Combine multiple NFAs into one big NFA, where the first NFA must be followed by the second NFA,
 * and the second NFA must be followed by the third NFA,... Constructs the combined NFA using
 * Thompson's construction algorithm ([YouTube](https://youtu.be/HLOAwCCYVxE?t=380))
 */
internal fun concatenateMultipleNfa(multipleNFA: List<NFA>): NFA {
    fun concatenateTwoNfa(firstNfa: NFA, secondNfa: NFA): NFA {
        // first, it's important to make sure that all states have unique names because
        // states are only differentiated by their name
        firstNfa.renameStatesToBeDifferentFrom(secondNfa)

        // First create edges from all accepting states of the first NFA to all start states of the
        // second NFA
        // add the epsilon edges to all accepting states of the first NFA
        secondNfa.states
            .filter { it.isStart }
            .map {
                val edge = Edge(op = NFA.EPSILON, nextState = it)
                firstNfa.states.filter { state -> state.isAcceptingState }.map { state -> state.addEdge(edge) }
            }
        // then make the accepting states in the first NFA non-accepting
        firstNfa.states
            .filter { it.isAcceptingState }
            .forEach { firstNfa.checkedChangeStateProperty(it, isAcceptingState = false) }
        // then make the starting states in the second NFA non-starting
        secondNfa.states
            .filter { it.isStart }
            .forEach { secondNfa.checkedChangeStateProperty(it, isStart = false) }

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
 * Combine multiple NFAs into one big NFA, where the first NFA must be followed by the second NFA,
 * and the second NFA must be followed by the third NFA,... Constructs the combined NFA using
 * Thompson's construction algorithm ([YouTube](https://youtu.be/HLOAwCCYVxE?t=380))
 */
internal fun concatenateMultipleNfa(vararg multipleNFA: NFA) = concatenateMultipleNfa(multipleNFA.toList())

/**
 * Combine two NFAs into one big NFA, where either the first or second NFA is OK Constructs the
 * combined NFA using Thompson's construction algorithm (
 * [YouTube](https://youtu.be/HLOAwCCYVxE?t=306))
 */
internal fun alternateTwoNfa(firstNfa: NFA, secondNfa: NFA): NFA {
    // first, it's important to make sure that all states have unique names because
    // states are only differentiated by their name
    firstNfa.renameStatesToBeDifferentFrom(secondNfa)

    // first create new epsilon edges to the start states of both NFAs
    val epsilonEdgesFirst =
        firstNfa.states.filter { it.isStart }.map { Edge(op = NFA.EPSILON, nextState = it) }
    val epsilonEdgesSecond =
        secondNfa.states.filter { it.isStart }.map { Edge(op = NFA.EPSILON, nextState = it) }

    // then make the old start states non-start states
    firstNfa.states
        .filter { it.isStart }
        .forEach { firstNfa.checkedChangeStateProperty(it, isStart = false) }
    secondNfa.states
        .filter { it.isStart }
        .forEach { secondNfa.checkedChangeStateProperty(it, isStart = false) }

    // merge both NFAs into a single one
    val combinedNfa = NFA(firstNfa.states + secondNfa.states)

    // add a new start state
    val newStartState = combinedNfa.addState(isStart = true)

    // and lastly add the epsilonEdges to the new start state
    epsilonEdgesFirst.forEach { newStartState.addEdge(it) }
    epsilonEdgesSecond.forEach { newStartState.addEdge(it) }

    return combinedNfa
}
