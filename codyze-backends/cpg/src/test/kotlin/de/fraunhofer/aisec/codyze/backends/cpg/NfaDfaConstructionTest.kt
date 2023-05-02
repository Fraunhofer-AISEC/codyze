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
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.*
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.ordering.*
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
        fun create() = op { "create" {} }
        fun init() = op { "init" {} }
        fun start() = op { "start" {} }
        fun process() = op { "process" {} }
        fun finish() = op { "finish" {} }
        fun reset() = op { "reset" {} }
    }

    private val baseName =
        "de.fraunhofer.aisec.codyze.backends.cpg.NfaDfaConstructionTest\$TestClass"

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
            val testObj = TestClass()
            val nfa = orderExpressionToNfa {
                - testObj::create
                - testObj::init
            }

            val expectedNfa = NFA()
            val q0 = expectedNfa.addState(isStart = true)
            val q1 = expectedNfa.addState()
            val q2 = expectedNfa.addState()
            val q3 = expectedNfa.addState(isAcceptingState = true)
            expectedNfa.addEdge(q0, Edge(op = testObj.create().hashCode().toString(), base = baseName, nextState = q1))
            expectedNfa.addEdge(q1, Edge(op = NFA.EPSILON, nextState = q2))
            expectedNfa.addEdge(q2, Edge(op = testObj.init().hashCode().toString(), base = baseName, nextState = q3))

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
            val testObj = TestClass()
            val nfa = orderExpressionToNfa { testObj::create or testObj::init }

            val expectedNfa = NFA()
            val q0 = expectedNfa.addState(isStart = true)
            val q1 = expectedNfa.addState()
            val q2 = expectedNfa.addState()
            val q3 = expectedNfa.addState(isAcceptingState = true)
            val q4 = expectedNfa.addState(isAcceptingState = true)
            expectedNfa.addEdge(q0, Edge(op = NFA.EPSILON, nextState = q1))
            expectedNfa.addEdge(q0, Edge(op = NFA.EPSILON, nextState = q2))
            expectedNfa.addEdge(q1, Edge(op = testObj.create().hashCode().toString(), base = baseName, nextState = q3))
            expectedNfa.addEdge(q2, Edge(op = testObj.init().hashCode().toString(), base = baseName, nextState = q4))

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
            val testObj = TestClass()
            val nfa = orderExpressionToNfa { maybe(testObj::create) }
            val nfa2 = orderExpressionToNfa {
                maybe {
                    - testObj::create
                }
            }

            val expectedNfa = NFA()
            val q0 = expectedNfa.addState(isStart = true, isAcceptingState = true)
            val q1 = expectedNfa.addState()
            val q2 = expectedNfa.addState(isAcceptingState = true)
            expectedNfa.addEdge(q0, Edge(op = NFA.EPSILON, nextState = q1))
            expectedNfa.addEdge(q1, Edge(op = testObj.create().hashCode().toString(), base = baseName, nextState = q2))
            expectedNfa.addEdge(q2, Edge(op = NFA.EPSILON, nextState = q0))

            assertEquals(expected = expectedNfa, actual = nfa)
            assertEquals(expected = expectedNfa, actual = nfa2)
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
            val testObj = TestClass()
            val nfa = orderExpressionToNfa { option(testObj::create) }
            val nfa2 = orderExpressionToNfa {
                option {
                    - testObj::create
                }
            }

            val expectedNfa = NFA()
            val q0 = expectedNfa.addState(isStart = true, isAcceptingState = true)
            val q1 = expectedNfa.addState()
            val q2 = expectedNfa.addState(isAcceptingState = true)
            expectedNfa.addEdge(q0, Edge(op = NFA.EPSILON, nextState = q1))
            expectedNfa.addEdge(q1, Edge(op = testObj.create().hashCode().toString(), base = baseName, nextState = q2))

            assertEquals(expected = expectedNfa, actual = nfa)
            assertEquals(expected = expectedNfa, actual = nfa2)
        }
    }

    /**
     * Tests a simple order with a count qualifier.
     *
     * The NFA can be converted to .dot format using: [NFA.toDotString].
     */
    @Test
    fun `test simple order with count qualifier`() {
        with(mockk<CokoCpgBackend>()) {
            val testObj = TestClass()
            val nfa = orderExpressionToNfa { count(2, testObj::create) }
            val nfa2 = orderExpressionToNfa {
                count(2) {
                    - testObj::create
                }
            }

            val expectedNfa = NFA()
            val q0 = expectedNfa.addState(isStart = true)
            val q1 = expectedNfa.addState()
            val q2 = expectedNfa.addState(isAcceptingState = true)
            expectedNfa.addEdge(q0, Edge(op = testObj.create().hashCode().toString(), base = baseName, nextState = q1))
            expectedNfa.addEdge(q1, Edge(op = testObj.create().hashCode().toString(), base = baseName, nextState = q2))

            assertEquals(expected = expectedNfa, actual = nfa)
            assertEquals(expected = expectedNfa, actual = nfa2)
        }
    }

    /**
     * Tests a simple order with a some qualifier.
     *
     * The NFA can be converted to .dot format using: [NFA.toDotString].
     */
    @Test
    fun `test simple order with some qualifier`() {
        with(mockk<CokoCpgBackend>()) {
            val testObj = TestClass()
            val nfa = orderExpressionToNfa { some(testObj::create) }
            val nfa2 = orderExpressionToNfa {
                some {
                    - testObj::create
                }
            }

            val expectedNfa = NFA()
            val q0 = expectedNfa.addState(isStart = true)
            val q1 = expectedNfa.addState(isAcceptingState = true)
            expectedNfa.addEdge(q0, Edge(op = testObj.create().hashCode().toString(), base = baseName, nextState = q1))
            expectedNfa.addEdge(q1, Edge(op = testObj.create().hashCode().toString(), base = baseName, nextState = q1))

            assertEquals(expected = expectedNfa, actual = nfa)
            assertEquals(expected = expectedNfa, actual = nfa2)
        }
    }

    /**
     * Tests a simple order with a atLeast qualifier.
     *
     * The NFA can be converted to .dot format using: [NFA.toDotString].
     */
    @Test
    fun `test simple order with atLeast qualifier`() {
        with(mockk<CokoCpgBackend>()) {
            val testObj = TestClass()
            val nfa = orderExpressionToNfa { atLeast(2, testObj::create) }
            val nfa2 = orderExpressionToNfa {
                atLeast(2) {
                    - testObj::create
                }
            }

            val expectedNfa = NFA()
            val q0 = expectedNfa.addState(isStart = true)
            val q1 = expectedNfa.addState()
            val q2 = expectedNfa.addState(isAcceptingState = true)
            expectedNfa.addEdge(q0, Edge(op = testObj.create().hashCode().toString(), base = baseName, nextState = q1))
            expectedNfa.addEdge(q1, Edge(op = testObj.create().hashCode().toString(), base = baseName, nextState = q2))
            expectedNfa.addEdge(q2, Edge(op = testObj.create().hashCode().toString(), base = baseName, nextState = q2))

            assertEquals(expected = expectedNfa, actual = nfa)
            assertEquals(expected = expectedNfa, actual = nfa2)
        }
    }

    /**
     * Tests a simple order with a set.
     *
     * The NFA can be converted to .dot format using: [NFA.toDotString].
     */
    @Test
    fun `test simple order with set`() {
        with(mockk<CokoCpgBackend>()) {
            val testObj = TestClass()
            val nfa = orderExpressionToNfa {
                set [testObj::create, testObj::init, testObj::start]
            }

            val expectedNfa = NFA()
            val q0 = expectedNfa.addState(isStart = true)
            val q1 = expectedNfa.addState(isAcceptingState = true)
            expectedNfa.addEdge(q0, Edge(op = testObj.create().hashCode().toString(), base = baseName, nextState = q1))
            expectedNfa.addEdge(q0, Edge(op = testObj.start().hashCode().toString(), base = baseName, nextState = q1))
            expectedNfa.addEdge(q0, Edge(op = testObj.init().hashCode().toString(), base = baseName, nextState = q1))

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
            val testObj = TestClass()
            val dfa = orderExpressionToDfa {
                - testObj::create
                - testObj::init
                - some {
                    - testObj::start
                    - maybe(testObj::process)
                    - testObj::finish
                }
                - option(testObj::reset)
            }

            val expected = DFA()
            val q1 = expected.addState(isStart = true)
            val q2 = expected.addState()
            val q3 = expected.addState()
            val q4 = expected.addState()
            val q5 = expected.addState()
            val q6 = expected.addState(isAcceptingState = true)
            val q7 = expected.addState(isAcceptingState = true)
            expected.addEdge(q1, Edge(testObj.create().hashCode().toString(), baseName, q2))
            expected.addEdge(q2, Edge(testObj.init().hashCode().toString(), baseName, q3))
            expected.addEdge(q3, Edge(testObj.start().hashCode().toString(), baseName, q4))
            expected.addEdge(q4, Edge(testObj.process().hashCode().toString(), baseName, q5))
            expected.addEdge(q4, Edge(testObj.finish().hashCode().toString(), baseName, q6))
            expected.addEdge(q5, Edge(testObj.process().hashCode().toString(), baseName, q5))
            expected.addEdge(q5, Edge(testObj.finish().hashCode().toString(), baseName, q6))
            expected.addEdge(q6, Edge(testObj.start().hashCode().toString(), baseName, q4))
            expected.addEdge(q6, Edge(testObj.reset().hashCode().toString(), baseName, q7))

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
            val testObj = TestClass()
            val dfa = orderExpressionToDfa {
                - testObj::init
                some {
                    testObj::create or
                        group {
                            - testObj::start
                            maybe(testObj::process)
                            - testObj::finish
                        }
                }
                option(testObj::reset)
            }

            val expected = DFA()
            val q1 = expected.addState(isStart = true)
            val q2 = expected.addState()
            val q3 = expected.addState(isAcceptingState = true)
            val q4 = expected.addState()
            val q5 = expected.addState()
            val q6 = expected.addState(isAcceptingState = true)
            val q7 = expected.addState(isAcceptingState = true)
            expected.addEdge(q1, Edge(testObj.init().hashCode().toString(), baseName, q2))
            expected.addEdge(q2, Edge(testObj.create().hashCode().toString(), baseName, q3))
            expected.addEdge(q2, Edge(testObj.start().hashCode().toString(), baseName, q4))
            expected.addEdge(q3, Edge(testObj.create().hashCode().toString(), baseName, q3))
            expected.addEdge(q3, Edge(testObj.start().hashCode().toString(), baseName, q4))
            expected.addEdge(q3, Edge(testObj.reset().hashCode().toString(), baseName, q7))
            expected.addEdge(q4, Edge(testObj.process().hashCode().toString(), baseName, q5))
            expected.addEdge(q4, Edge(testObj.finish().hashCode().toString(), baseName, q6))
            expected.addEdge(q5, Edge(testObj.process().hashCode().toString(), baseName, q5))
            expected.addEdge(q5, Edge(testObj.finish().hashCode().toString(), baseName, q6))
            expected.addEdge(q6, Edge(testObj.create().hashCode().toString(), baseName, q3))
            expected.addEdge(q6, Edge(testObj.start().hashCode().toString(), baseName, q4))
            expected.addEdge(q6, Edge(testObj.reset().hashCode().toString(), baseName, q7))

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
            val testObj = TestClass()
            val dfa = orderExpressionToNfa {
                - testObj::init
                - some {
                    testObj::create or
                        group {
                            maybe(testObj::start)
                            maybe(testObj::process)
                            maybe(testObj::start)
                            - testObj::finish
                        }
                }
                option(testObj::reset)
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
            expected.addEdge(q1, Edge(testObj.init().hashCode().toString(), baseName, q2))
            expected.addEdge(q2, Edge(testObj.create().hashCode().toString(), baseName, q3))
            expected.addEdge(q2, Edge(testObj.finish().hashCode().toString(), baseName, q3))
            expected.addEdge(q2, Edge(testObj.start().hashCode().toString(), baseName, q4))
            expected.addEdge(q2, Edge(testObj.process().hashCode().toString(), baseName, q5))
            expected.addEdge(q3, Edge(testObj.create().hashCode().toString(), baseName, q3))
            expected.addEdge(q3, Edge(testObj.finish().hashCode().toString(), baseName, q3))
            expected.addEdge(q3, Edge(testObj.start().hashCode().toString(), baseName, q4))
            expected.addEdge(q3, Edge(testObj.process().hashCode().toString(), baseName, q5))
            expected.addEdge(q3, Edge(testObj.reset().hashCode().toString(), baseName, q6))
            expected.addEdge(q4, Edge(testObj.start().hashCode().toString(), baseName, q4))
            expected.addEdge(q4, Edge(testObj.finish().hashCode().toString(), baseName, q3))
            expected.addEdge(q4, Edge(testObj.process().hashCode().toString(), baseName, q5))
            expected.addEdge(q5, Edge(testObj.process().hashCode().toString(), baseName, q5))
            expected.addEdge(q5, Edge(testObj.finish().hashCode().toString(), baseName, q3))
            expected.addEdge(q5, Edge(testObj.start().hashCode().toString(), baseName, q7))
            expected.addEdge(q7, Edge(testObj.start().hashCode().toString(), baseName, q7))
            expected.addEdge(q7, Edge(testObj.finish().hashCode().toString(), baseName, q3))

            assertEquals(expected, dfa)
        }
    }
}
