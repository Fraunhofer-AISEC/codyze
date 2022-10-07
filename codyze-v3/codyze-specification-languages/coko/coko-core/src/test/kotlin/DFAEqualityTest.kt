import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.dsl.*
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.ordering.*
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import org.junit.jupiter.api.Test

/**
 * Tests whether the [DFA.equals] method works as expected and correctly identifies whether two DFAs
 * accept the same language. If this test fails, the [NfaDfaConstructionTest] suite and
 * [DFAConstructionTest] suite will also fail.
 */
class DFAEqualityTest {
    @Test
    /** Tests an empty DFA */
    fun `test empty DFA`() {
        assertFailsWith<IllegalStateException>(
            message = "In order to compare to FSMs, both must have exactly one start state.",
            block = { DFA() == DFA() }
        )
    }

    @Test
    /** Tests a DFA with a single state */
    fun `test DFA with a single state`() {
        val dfa1 = DFA(setOf(DfaState(1, isStart = true)))
        val dfa2 = DFA(setOf(DfaState(2, isStart = true)))

        assertEquals(dfa1, dfa2)
    }

    @Test
    /**
     * Tests a DFA with an isomorphic states (exactly the same DFA but with different state names)
     */
    fun `test isomorphic DFA`() {
        val getStates = { offset: Int ->
            val state4 = DfaState(4 + offset, isAcceptingState = true)
            val state3 = DfaState(3 + offset).apply { addEdge(Edge("to4", nextState = state4)) }
            val state2 = DfaState(2 + offset).apply { addEdge(Edge("to4", nextState = state4)) }
            val state1 =
                DfaState(1 + offset, isStart = true).apply {
                    addEdge(Edge("to2", nextState = state2))
                    addEdge(Edge("to3", nextState = state3))
                }
            setOf(state1, state2, state3, state4)
        }
        val dfa1 = DFA(getStates(0))
        val dfa2 = DFA(getStates(5))

        assertEquals(dfa1, dfa2)
    }

    @Test
    /**
     * Tests a DFA and an equivalent minimal DFA. Uses the two DFAs depicted
     * [here](https://en.wikipedia.org/wiki/DFA_minimization)
     */
    fun `test bloated and minimal DFA`() {
        val state6 = DfaState(6) // f
        val state5 = DfaState(5, isAcceptingState = true) // e
        val state4 = DfaState(4, isAcceptingState = true) // d
        val state3 = DfaState(3, isAcceptingState = true) // c
        val state2 = DfaState(2) // b
        val state1 = DfaState(1, isStart = true) // a

        state1.apply {
            addEdge(Edge("0", nextState = state2))
            addEdge(Edge("1", nextState = state3))
        }
        state2.apply {
            addEdge(Edge("0", nextState = state1))
            addEdge(Edge("1", nextState = state4))
        }
        state3.apply {
            addEdge(Edge("0", nextState = state5))
            addEdge(Edge("1", nextState = state6))
        }
        state4.apply {
            addEdge(Edge("0", nextState = state5))
            addEdge(Edge("1", nextState = state6))
        }
        state5.apply {
            addEdge(Edge("0", nextState = this))
            addEdge(Edge("1", nextState = state6))
        }
        state6.apply {
            addEdge(Edge("0", nextState = this))
            addEdge(Edge("1", nextState = this))
        }
        val dfa1 = DFA(setOf(state1, state2, state3, state4, state5, state6))

        // construct the equivalent minimal DFA
        val state33 =
            DfaState(3).apply {
                addEdge(Edge("1", nextState = this))
                addEdge(Edge("0", nextState = this))
            } // a,b
        val state22 =
            DfaState(2, isAcceptingState = true).apply {
                addEdge(Edge("0", nextState = this))
                addEdge(Edge("1", nextState = state33))
            } // c,d,e
        val state11 =
            DfaState(1, isStart = true).apply {
                addEdge(Edge("0", nextState = this))
                addEdge(Edge("1", nextState = state22))
            } // f
        val dfa2 = DFA(setOf(state11, state22, state33))

        assertEquals(dfa1, dfa2)
    }

    @Test
    /** Tests whether we can correctly flag non-equivalent DFAs. */
    fun `test non equivalent DFA`() {
        val state6 = DfaState(6) // f
        val state5 = DfaState(5, isAcceptingState = true) // e
        val state4 = DfaState(4, isAcceptingState = true) // d
        val state3 = DfaState(3, isAcceptingState = true) // c
        val state2 = DfaState(2) // b
        val state1 = DfaState(1, isStart = true) // a

        state1.apply {
            addEdge(Edge("0", nextState = state2))
            addEdge(Edge("1", nextState = state3))
        }
        state2.apply {
            addEdge(Edge("0", nextState = state1))
            addEdge(Edge("1", nextState = state4))
        }
        state3.apply {
            addEdge(Edge("0", nextState = state5))
            addEdge(Edge("1", nextState = state6))
        }
        state4.apply {
            addEdge(Edge("0", nextState = state5))
            addEdge(Edge("1", nextState = state6))
        }
        state5.apply {
            addEdge(Edge("0", nextState = this))
            addEdge(Edge("1", nextState = state6))
        }
        state6.apply {
            addEdge(Edge("0", nextState = this))
            addEdge(Edge("1", nextState = this))
        }
        val dfa1 = DFA(setOf(state1, state2, state3, state4, state5, state6))

        // construct another DFA that does not accept the same language
        val state33 =
            DfaState(3).apply {
                addEdge(
                    Edge("1", nextState = this)
                ) // this is where an equivalent DFA would need an additional Edge("0",
                // nextState=this)
            } // a,b
        val state22 =
            DfaState(2, isAcceptingState = true).apply {
                addEdge(Edge("0", nextState = this))
                addEdge(Edge("1", nextState = state33))
            } // c,d,e
        val state11 =
            DfaState(1, isStart = true).apply {
                addEdge(Edge("0", nextState = this))
                addEdge(Edge("1", nextState = state22))
            } // f
        val dfa2 = DFA(setOf(state11, state22, state33))

        assertNotEquals(dfa1, dfa2)
    }

    @Test
    /** Tests whether we can correctly flag non-equivalent DFAs a second time. */
    fun `test DFA with a single difference`() {
        // construct another DFA that does not accept the same language
        val state3 = DfaState(3).apply { addEdge(Edge("1", nextState = this)) } // a,b
        val state2 =
            DfaState(2, isAcceptingState = true).apply {
                addEdge(Edge("0", nextState = this))
                addEdge(Edge("1", nextState = state3))
            } // c,d,e
        val state1 =
            DfaState(1, isStart = true).apply {
                addEdge(Edge("0", nextState = this))
                addEdge(Edge("1", nextState = state2))
            } // f
        val dfa1 = DFA(setOf(state1, state2, state3))

        // construct another DFA that does not accept the same language
        val state33 = DfaState(3).apply { addEdge(Edge("0", nextState = this)) } // a,b
        val state22 =
            DfaState(2, isAcceptingState = true).apply {
                addEdge(Edge("0", nextState = this))
                addEdge(Edge("1", nextState = state33))
            } // c,d,e
        val state11 =
            DfaState(1, isStart = true).apply {
                addEdge(Edge("0", nextState = this))
                addEdge(Edge("1", nextState = state22))
            } // f
        val dfa2 = DFA(setOf(state11, state22, state33))

        assertNotEquals(dfa1, dfa2)
    }
}
