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
package de.fraunhofer.aisec.codyze.backends.cpg

import de.fraunhofer.aisec.codyze.backends.cpg.coko.CokoCpgBackend
import de.fraunhofer.aisec.codyze.backends.cpg.coko.ordering.toNfa
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.dsl.*
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.ordering.*
import de.fraunhofer.aisec.cpg.analysis.fsm.DFA
import de.fraunhofer.aisec.cpg.analysis.fsm.Edge
import de.fraunhofer.aisec.cpg.analysis.fsm.NFA
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals

// TODO: add tests for the other [OrderQuantifier] types
/**
 * Tests whether a coko order expression can be correctly converted to a NFA. This also tests
 * whether the NFAs are correctly converted to DFAs because the [NFA.equals] method converts the NFA
 * to a DFA for the comparison.
 *
 * If this test fails, make sure that the following tests work first as the functionality these test
 * is needed for this test:
 * - [OrderSyntaxTreeConstructionTest]
 * - [cpg-analysis.NfaToDfaConversionTest]
 */
class NfaDfaConstructionTest {
    class TestClass {
        fun create() = op {}
        fun init() = op {}
        fun start() = op {}
        fun process() = op {}
        fun finish() = op {}
        fun reset() = op {}
    }

    private val baseName =
        "class de.fraunhofer.aisec.codyze_backends.cpg.NfaDfaConstructionTest\$TestClass"

    private fun orderExpressionToNfa(block: Order.() -> Unit): NFA {
        val order = Order().apply(block)
        return order.toNode().toNfa()
    }

    private fun orderExpressionToDfa(block: Order.() -> Unit) =
        orderExpressionToNfa { block() }.toDfa()

    /**
     * Tests a simple sequence order.
     *
     * The NFA can be converted to .dot format using: [NFA.toDotString].
     */
    @Test
    fun `test simple sequence order`() {
        with(mockk<CokoCpgBackend>()) {
            val nfa = orderExpressionToNfa {
                +TestClass::create
                +TestClass::init
            }

            val expectedNfa = NFA()
            val q0 = expectedNfa.addState(isStart = true)
            val q1 = expectedNfa.addState()
            val q2 = expectedNfa.addState()
            val q3 = expectedNfa.addState(isAcceptingState = true)
            expectedNfa.addEdge(q0, Edge(op = "create", base = baseName, nextState = q1))
            expectedNfa.addEdge(q1, Edge(op = NFA.EPSILON, nextState = q2))
            expectedNfa.addEdge(q2, Edge(op = "init", base = baseName, nextState = q3))

            assertEquals(expected = expectedNfa, actual = nfa)
        }
    }

    /**
     * Tests a simple order with a branch.
     *
     * The NFA can be converted to .dot format using: [NFA.toDotString].
     */
    @Test
    fun `test simple order with branch`() {
        with(mockk<CokoCpgBackend>()) {
            val nfa = orderExpressionToNfa { +(TestClass::create or TestClass::init) }

            val expectedNfa = NFA()
            val q0 = expectedNfa.addState(isStart = true)
            val q1 = expectedNfa.addState()
            val q2 = expectedNfa.addState()
            val q3 = expectedNfa.addState(isAcceptingState = true)
            val q4 = expectedNfa.addState(isAcceptingState = true)
            expectedNfa.addEdge(q0, Edge(op = NFA.EPSILON, nextState = q1))
            expectedNfa.addEdge(q0, Edge(op = NFA.EPSILON, nextState = q2))
            expectedNfa.addEdge(q1, Edge(op = "create", base = baseName, nextState = q3))
            expectedNfa.addEdge(q2, Edge(op = "init", base = baseName, nextState = q4))

            assertEquals(expected = expectedNfa, actual = nfa)
        }
    }

    /**
     * Tests a simple order with a maybe qualifier.
     *
     * The NFA can be converted to .dot format using: [NFA.toDotString].
     */
    @Test
    fun `test simple order with maybe qualifier`() {
        with(mockk<CokoCpgBackend>()) {
            val nfa = orderExpressionToNfa { +TestClass::create.maybe() }

            val expectedNfa = NFA()
            val q0 = expectedNfa.addState(isStart = true, isAcceptingState = true)
            val q1 = expectedNfa.addState()
            val q2 = expectedNfa.addState(isAcceptingState = true)
            expectedNfa.addEdge(q0, Edge(op = NFA.EPSILON, nextState = q1))
            expectedNfa.addEdge(q1, Edge(op = "create", base = baseName, nextState = q2))
            expectedNfa.addEdge(q2, Edge(op = NFA.EPSILON, nextState = q0))

            assertEquals(expected = expectedNfa, actual = nfa)
        }
    }

    /**
     * Tests a simple order with an option qualifier.
     *
     * The NFA can be converted to .dot format using: [NFA.toDotString].
     */
    @Test
    fun `test simple order with option qualifier`() {
        with(mockk<CokoCpgBackend>()) {
            val nfa = orderExpressionToNfa { +TestClass::create.option() }

            val expectedNfa = NFA()
            val q0 = expectedNfa.addState(isStart = true, isAcceptingState = true)
            val q1 = expectedNfa.addState()
            val q2 = expectedNfa.addState(isAcceptingState = true)
            expectedNfa.addEdge(q0, Edge(op = NFA.EPSILON, nextState = q1))
            expectedNfa.addEdge(q1, Edge(op = "create", base = baseName, nextState = q2))

            assertEquals(expected = expectedNfa, actual = nfa)
        }
    }

    /**
     * Tests a more complex order with a loop.
     *
     * The NFA can be converted to .dot format using: [NFA.toDotString].
     */
    @Test
    fun `test complex order with loop`() {
        with(mockk<CokoCpgBackend>()) {
            // equivalent MARK:
            // order cm.create(), cm.init(), (cm.start(), cm.process()*, cm.finish())+, cm.reset()?
            val dfa = orderExpressionToDfa {
                +TestClass::create
                +TestClass::init
                +some {
                    +TestClass::start
                    +TestClass::process.maybe()
                    +TestClass::finish
                }
                +TestClass::reset.option()
            }

            val expected = DFA()
            val q1 = expected.addState(isStart = true)
            val q2 = expected.addState()
            val q3 = expected.addState()
            val q4 = expected.addState()
            val q5 = expected.addState()
            val q6 = expected.addState(isAcceptingState = true)
            val q7 = expected.addState(isAcceptingState = true)
            expected.addEdge(q1, Edge("create", baseName, q2))
            expected.addEdge(q2, Edge("init", baseName, q3))
            expected.addEdge(q3, Edge("start", baseName, q4))
            expected.addEdge(q4, Edge("process", baseName, q5))
            expected.addEdge(q4, Edge("finish", baseName, q6))
            expected.addEdge(q5, Edge("process", baseName, q5))
            expected.addEdge(q5, Edge("finish", baseName, q6))
            expected.addEdge(q6, Edge("start", baseName, q4))
            expected.addEdge(q6, Edge("reset", baseName, q7))

            assertEquals(expected, dfa)
        }
    }

    /**
     * Tests a more complex order with a branch and a loop.
     *
     * The NFA can be converted to .dot format using: [NFA.toDotString].
     */
    @Test
    fun `test complex order with branch and loop`() {
        with(mockk<CokoCpgBackend>()) {
            // equivalent MARK:
            // order cm.init(), (cm.bigStep() | (cm.start(), cm.process()*, cm.finish()))+,
            // cm.reset()?
            val dfa = orderExpressionToDfa {
                +TestClass::init
                +some {
                    +(
                        TestClass::create or
                            group {
                                +TestClass::start
                                +TestClass::process.maybe()
                                +TestClass::finish
                            }
                        )
                }
                +TestClass::reset.option()
            }

            val expected = DFA()
            val q1 = expected.addState(isStart = true)
            val q2 = expected.addState()
            val q3 = expected.addState(isAcceptingState = true)
            val q4 = expected.addState()
            val q5 = expected.addState()
            val q6 = expected.addState(isAcceptingState = true)
            val q7 = expected.addState(isAcceptingState = true)
            expected.addEdge(q1, Edge("init", baseName, q2))
            expected.addEdge(q2, Edge("create", baseName, q3))
            expected.addEdge(q2, Edge("start", baseName, q4))
            expected.addEdge(q3, Edge("create", baseName, q3))
            expected.addEdge(q3, Edge("start", baseName, q4))
            expected.addEdge(q3, Edge("reset", baseName, q7))
            expected.addEdge(q4, Edge("process", baseName, q5))
            expected.addEdge(q4, Edge("finish", baseName, q6))
            expected.addEdge(q5, Edge("process", baseName, q5))
            expected.addEdge(q5, Edge("finish", baseName, q6))
            expected.addEdge(q6, Edge("create", baseName, q3))
            expected.addEdge(q6, Edge("start", baseName, q4))
            expected.addEdge(q6, Edge("reset", baseName, q7))

            assertEquals(expected, dfa)
        }
    }

    /**
     * Tests an even more complex order branches and loops.
     *
     * The NFA can be converted to .dot format using: [NFA.toDotString].
     */
    @Test
    fun `test even more complex order with branch and loop`() {
        with(mockk<CokoCpgBackend>()) {
            // equivalent MARK:
            // order cm.init(), (cm.create() | (cm.start()*, cm.process()*, cm.start()*,
            // cm.finish()))+,
            // cm.reset()?
            val dfa = orderExpressionToNfa {
                +TestClass::init
                +some {
                    +(
                        TestClass::create or
                            group {
                                +TestClass::start.maybe()
                                +TestClass::process.maybe()
                                +TestClass::start.maybe()
                                +TestClass::finish
                            }
                        )
                }
                +TestClass::reset.option()
            }

            // the equivalent min-DFA was obtained from:
            // https://cyberzhg.github.io/toolbox/min_dfa?regex=YihhfChjKmQqYyplKSkrZj8=
            val expected = NFA()
            val q1 = expected.addState(isStart = true)
            val q2 = expected.addState()
            val q3 = expected.addState(isAcceptingState = true)
            val q4 = expected.addState()
            val q5 = expected.addState()
            val q6 = expected.addState(isAcceptingState = true)
            val q7 = expected.addState()
            expected.addEdge(q1, Edge("init", baseName, q2))
            expected.addEdge(q2, Edge("create", baseName, q3))
            expected.addEdge(q2, Edge("finish", baseName, q3))
            expected.addEdge(q2, Edge("start", baseName, q4))
            expected.addEdge(q2, Edge("process", baseName, q5))
            expected.addEdge(q3, Edge("create", baseName, q3))
            expected.addEdge(q3, Edge("finish", baseName, q3))
            expected.addEdge(q3, Edge("start", baseName, q4))
            expected.addEdge(q3, Edge("process", baseName, q5))
            expected.addEdge(q3, Edge("reset", baseName, q6))
            expected.addEdge(q4, Edge("start", baseName, q4))
            expected.addEdge(q4, Edge("finish", baseName, q3))
            expected.addEdge(q4, Edge("process", baseName, q5))
            expected.addEdge(q5, Edge("process", baseName, q5))
            expected.addEdge(q5, Edge("finish", baseName, q3))
            expected.addEdge(q5, Edge("start", baseName, q7))
            expected.addEdge(q7, Edge("start", baseName, q7))
            expected.addEdge(q7, Edge("finish", baseName, q3))

            assertEquals(expected, dfa)
        }
    }
}
