package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.ordering

import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.dsl.Nodes

/** Represents an [OrderToken] */
data class TerminalOrderNode(val base: String, val op: String, val block: (() -> Nodes)? = null): OrderNode {
    /**
     * Constructs a NFA using Thompson's construction algorithm
     * @see [YouTube](https://youtu.be/HLOAwCCYVxE?t=237)
     */
    override fun toNfa(): NFA {
        val nfa = NFA()

        // create a start state
        val startState = nfa.addState(isStart = true)
        // create an accepting state
        val endState = nfa.addState(isAcceptingState = true)
        // create an edge connecting the two states
        val edge = Edge(op=op, base=base, nextState = endState)
        // add the edge to the NFA
        nfa.addEdge(startState, edge)
        return nfa
    }
}

/**
 * Represents a regex sequence, where one [OrderNode] must be followed by another
 * [OrderNode]
 */
data class SequenceOrderNode(val left: OrderNode, val right: OrderNode) : OrderNode {
    /**
     * Constructs a NFA using Thompson's construction algorithm
     */
    override fun toNfa() = concatenateMultipleNfa(left.toNfa(), right.toNfa())
}

/** Represents a regex OR ('|') */
data class AlternativeOrderNode(val left: OrderNode, val right: OrderNode) : OrderNode {
    /**
     * Constructs a NFA using Thompson's construction algorithm
     */
    override fun toNfa() = alternateTwoNfa(left.toNfa(), right.toNfa())
}

/** Represents a regex quantifier like: '*', '?', etc. */
data class QuantifierOrderNode(
    val child: OrderNode,
    val type: OrderQuantifier,
    val value: Any? = null
) : OrderNode {
    init {
        if (
            type in listOf(OrderQuantifier.ATLEAST, OrderQuantifier.BETWEEN, OrderQuantifier.COUNT)
        ) {
            checkNotNull(value) { "You must provide a value for this kind of quantifier." }
        }
    }

    /**
     * Constructs a NFA using Thompson's construction algorithm
     * @see [YouTube](https://youtu.be/HLOAwCCYVxE)
     */
    override fun toNfa(): NFA = when(type) {
        OrderQuantifier.MAYBE -> addMaybeQuantifierToNFA(child.toNfa())  // '*'
        OrderQuantifier.OPTION -> addOptionQuantifierToNFA(child.toNfa())  // '?'
        OrderQuantifier.COUNT -> concatenateMultipleNfa(*Array(value as Int) {child.toNfa()})
        OrderQuantifier.ATLEAST -> concatenateMultipleNfa(concatenateMultipleNfa(*Array(value as Int) {child.toNfa()}), addMaybeQuantifierToNFA(child.toNfa()))  // '{..,}'
        OrderQuantifier.BETWEEN -> concatenateMultipleNfa(*Array((value as IntRange).first) {child.toNfa()}, *Array(value.last-value.first) { addMaybeQuantifierToNFA(child.toNfa())})
    }
}

/** All the available quantifiers for this simple regex like DSL. */
enum class OrderQuantifier {
    COUNT,
    BETWEEN,
    ATLEAST,
    MAYBE,
    OPTION
}
