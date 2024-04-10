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
package de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.order

import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.CokoBackend
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.*
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.ordering.*
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Tests whether a coko order expression can be correctly converted into a syntax tree. This is the
 * first step in evaluating the order. The resulting syntax tree is then converted to a NFA, which
 * is converted to a DFA afterwards.
 */
class OrderSyntaxTreeConstructionTest {
    context(CokoBackend)
    class TestClass {
        fun fun1(testParam: Int?) = op {
            "testOp" {
                signature {
                    group {
                        -testParam
                    }
                }
            }
        }

        fun fun2() = op {
            "empty OP" {}
        }
    }

    context(CokoBackend)
    private fun orderExpressionToSyntaxTree(block: Order.() -> Unit): OrderNode {
        val order = Order().apply(block)
        return order.toNode()
    }

    /** Tests an order with a no token at all -> this should error */
    @Test
    fun `test zero node order`() {
        with(mockk<CokoBackend>()) {
            assertFailsWith<IllegalArgumentException>(
                message = "Groups and sets must have at least one element.",
                block = { orderExpressionToSyntaxTree {} }
            )
        }
    }

    /** Tests an order with an empty group -> this should error */
    @Test
    fun `test order with empty group`() {
        with(mockk<CokoBackend>()) {
            val testObj = TestClass()
            assertFailsWith<IllegalArgumentException>(
                message = "Groups and sets must have at least one element.",
                block = {
                    orderExpressionToSyntaxTree {
                        -testObj::fun1
                        group {}
                    }
                }
            )
        }
    }

    /** Tests an order with an empty set -> this should error */
    @Test
    fun `test order with empty set`() {
        with(mockk<CokoBackend>()) {
            val testObj = TestClass()
            assertFailsWith<IllegalArgumentException>(
                message = "Groups and sets must have at least one element.",
                block = {
                    orderExpressionToSyntaxTree {
                        -testObj::fun1
                        set {}
                    }
                }
            )
        }
    }

    /** Tests an order with a user defined OP */
    @Test
    fun `test order with user defined op`() {
        with(mockk<CokoBackend>()) {
            val testObj = TestClass()
            val syntaxTree = orderExpressionToSyntaxTree {
                - testObj.fun1(1)
                - testObj.fun1(1)
                - testObj::fun2
            }

            val fun1 = testObj.fun1(1).toNode()
            val expectedSyntaxTree =
                SequenceOrderNode(
                    left = SequenceOrderNode(
                        left = TerminalOrderNode(
                            fun1.baseName,
                            fun1.opName
                        ),
                        right = TerminalOrderNode(
                            fun1.baseName,
                            fun1.opName
                        )
                    ),
                    right = testObj::fun2.toNode()
                )

            assertEquals(expected = expectedSyntaxTree, actual = syntaxTree)
        }
    }

    /** Tests a simple sequence order */
    @Test
    fun `test simple sequence order`() {
        with(mockk<CokoBackend>()) {
            val testObj = TestClass()
            val syntaxTree = orderExpressionToSyntaxTree {
                - testObj::fun1
                - testObj::fun2
            }

            val expectedSyntaxTree =
                SequenceOrderNode(
                    left = testObj::fun1.toNode(),
                    right = testObj::fun2.toNode()
                )

            assertEquals(expected = expectedSyntaxTree, actual = syntaxTree)
        }
    }

    /** Tests a simple alternative order */
    @Test
    fun `test simple alternative order`() {
        with(mockk<CokoBackend>()) {
            val testObj = TestClass()
            val syntaxTree = orderExpressionToSyntaxTree { testObj::fun1 or testObj::fun2 }

            val expectedSyntaxTree =
                AlternativeOrderNode(
                    left = testObj::fun1.toNode(),
                    right = testObj::fun2.toNode()
                )

            assertEquals(expected = expectedSyntaxTree, actual = syntaxTree)
        }
    }

    /** Tests an alternative order with a group */
    @Test
    fun `test alternative order with group`() {
        with(mockk<CokoBackend>()) {
            val testObj = TestClass()
            val syntaxTree = orderExpressionToSyntaxTree {
                group(testObj::fun1, testObj::fun2) or testObj::fun2
            }

            val expectedSyntaxTree =
                AlternativeOrderNode(
                    left = SequenceOrderNode(
                        left = testObj::fun1.toNode(),
                        right = testObj::fun2.toNode()
                    ),
                    right = testObj::fun2.toNode()
                )

            assertEquals(expected = expectedSyntaxTree, actual = syntaxTree)
        }
    }

    /** Tests an alternative order with a quantified group */
    @Test
    fun `test alternative order with quantifier`() {
        with(mockk<CokoBackend>()) {
            val testObj = TestClass()
            val syntaxTree = orderExpressionToSyntaxTree {
                maybe(testObj::fun1, testObj::fun2) or testObj::fun2
            }

            val expectedSyntaxTree =
                AlternativeOrderNode(
                    left = QuantifierOrderNode(
                        SequenceOrderNode(
                            left = testObj::fun1.toNode(),
                            right = testObj::fun2.toNode()
                        ),
                        OrderQuantifier.MAYBE
                    ),
                    right = testObj::fun2.toNode()
                )

            assertEquals(expected = expectedSyntaxTree, actual = syntaxTree)
        }
    }

    /** Tests an order with a [QuantifierOrderNode] */
    @Test
    fun `test order with an QuantifierOrderNode`() {
        with(mockk<CokoBackend>()) {
            val testObj = TestClass()
            val syntaxTree = orderExpressionToSyntaxTree { between(1..4, testObj::fun1) }
            val expectedSyntaxTree =
                QuantifierOrderNode(
                    child = testObj::fun1.toNode(),
                    type = OrderQuantifier.BETWEEN,
                    value = 1..4
                )

            assertEquals(expected = expectedSyntaxTree, actual = syntaxTree)
        }
    }

    /** Tests an order with a group */
    @Test
    fun `test order with group`() {
        with(mockk<CokoBackend>()) {
            val testObj = TestClass()
            val syntaxTree = orderExpressionToSyntaxTree {
                maybe {
                    - testObj::fun1
                    - testObj::fun2
                }
            }

            val expectedSyntaxTree =
                QuantifierOrderNode(
                    SequenceOrderNode(
                        left = testObj::fun1.toNode(),
                        right = testObj::fun2.toNode()
                    ),
                    OrderQuantifier.MAYBE
                )

            assertEquals(expected = expectedSyntaxTree, actual = syntaxTree)
        }
    }

    /** Tests an order with a set */
    @Test
    fun `test order with set with one element`() {
        with(mockk<CokoBackend>()) {
            val testObj = TestClass()
            val syntaxTree = orderExpressionToSyntaxTree {
                set {
                    - testObj::fun1
                }
            }

            val syntaxTreeShortcut = orderExpressionToSyntaxTree {
                set[testObj::fun1]
            }

            assertEquals(syntaxTree, syntaxTreeShortcut)

            val expectedSyntaxTree = testObj::fun1.toNode()

            assertEquals(expected = expectedSyntaxTree, actual = syntaxTree)
        }
    }

    /** Tests an order with a set */
    @Test
    fun `test order with set with multiple elements`() {
        with(mockk<CokoBackend>()) {
            val testObj = TestClass()
            val syntaxTree = orderExpressionToSyntaxTree {
                set {
                    - testObj::fun1
                    - testObj::fun2
                }
            }

            val syntaxTreeShortcut = orderExpressionToSyntaxTree {
                set[testObj::fun1, testObj::fun2]
            }

            assertEquals(syntaxTree, syntaxTreeShortcut)

            val expectedSyntaxTree =
                AlternativeOrderNode(
                    testObj::fun1.toNode(),
                    testObj::fun2.toNode()
                )

            assertEquals(expected = expectedSyntaxTree, actual = syntaxTree)
        }
    }

    /** Tests an order where the same object is added twice */
    @Test
    fun `test order with multiple usages of same object`() {
        with(mockk<CokoBackend>()) {
            val testObj = TestClass()
            val nodeFun1 = testObj::fun1.toNode()
            val nodeFun2 = testObj::fun2.toNode()

            // regex:  fun1 (fun1 fun2)* fun2

            val syntaxTree = orderExpressionToSyntaxTree {
                // `maybe` is added to list
                val maybe = maybe {
                    - testObj::fun1
                    - testObj::fun2
                }
                - testObj::fun1
                // `maybe` is removed from list and added again
                add(maybe)
                - testObj::fun2
            }

            val expectedSyntaxTree =
                SequenceOrderNode(
                    SequenceOrderNode(
                        nodeFun1,
                        QuantifierOrderNode(
                            child = SequenceOrderNode(
                                nodeFun1,
                                nodeFun2
                            ),
                            type = OrderQuantifier.MAYBE
                        )
                    ),
                    nodeFun2
                )

            assertEquals(expected = expectedSyntaxTree, actual = syntaxTree)
        }
    }

    /** Tests an order where the same object is added twice */
    @Test
    fun `test order with usages of same groups`() {
        with(mockk<CokoBackend>()) {
            val testObj = TestClass()
            val nodeFun1 = testObj::fun1.toNode()
            val nodeFun2 = testObj::fun2.toNode()

            // regex: (fun1 fun2)* fun1 ((fun1 fun2)* | fun2) fun2

            val syntaxTree = orderExpressionToSyntaxTree {
                // `maybe` is added to list
                maybe {
                    - testObj::fun1
                    - testObj::fun2
                }
                - testObj::fun1
                // `maybe` is removed from list and added within the Node from `or`
                maybe {
                    - testObj::fun1
                    - testObj::fun2
                } or testObj::fun2
                - testObj::fun2
            }

            val expectedSyntaxTree =
                SequenceOrderNode(
                    SequenceOrderNode(
                        SequenceOrderNode(
                            QuantifierOrderNode(
                                child = SequenceOrderNode(
                                    nodeFun1,
                                    nodeFun2
                                ),
                                type = OrderQuantifier.MAYBE
                            ),
                            nodeFun1
                        ),
                        AlternativeOrderNode(
                            QuantifierOrderNode(
                                child = SequenceOrderNode(
                                    nodeFun1,
                                    nodeFun2
                                ),
                                type = OrderQuantifier.MAYBE
                            ),
                            nodeFun2
                        )
                    ),
                    nodeFun2
                )

            assertEquals(expected = expectedSyntaxTree, actual = syntaxTree)
        }
    }

    /** Tests an order where the same object is added twice and used within an `or` */
    @Test
    fun `test order with multiple usages of same object in or`() {
        with(mockk<CokoBackend>()) {
            val testObj = TestClass()
            val nodeFun1 = testObj::fun1.toNode()
            val nodeFun2 = testObj::fun2.toNode()

            // regex: fun1 fun1 ((fun1 fun2)* | fun2) fun2

            val syntaxTree = orderExpressionToSyntaxTree {
                // `maybe` is added to list
                val maybe = maybe {
                    - testObj::fun1
                    - testObj::fun2
                }
                - testObj::fun1
                // `maybe` is removed from list and added again
                add(maybe)
                - testObj::fun1
                // `maybe` is removed from list and added within the Node from `or`
                maybe or testObj::fun2
                - testObj::fun2
            }

            val expectedSyntaxTree =
                SequenceOrderNode(
                    SequenceOrderNode(
                        SequenceOrderNode(
                            nodeFun1,
                            nodeFun1
                        ),
                        AlternativeOrderNode(
                            QuantifierOrderNode(
                                child = SequenceOrderNode(
                                    nodeFun1,
                                    nodeFun2
                                ),
                                type = OrderQuantifier.MAYBE
                            ),
                            nodeFun2
                        )
                    ),
                    nodeFun2
                )

            assertEquals(expected = expectedSyntaxTree, actual = syntaxTree)
        }
    }

    /** Tests a complex order */
    @Test
    fun `test complex order`() {
        with(mockk<CokoBackend>()) {
            val testObj = TestClass()
            val syntaxTree = orderExpressionToSyntaxTree {
                between(1..4, testObj::fun1)
                maybe(testObj::fun1, testObj::fun2)
                set[testObj::fun2, testObj::fun1]
                - testObj::fun1
            }

            val nodeFun1 = testObj::fun1.toNode()
            val nodeFun2 = testObj::fun2.toNode()

            val expectedSyntaxTree =
                SequenceOrderNode(
                    SequenceOrderNode(
                        SequenceOrderNode(
                            QuantifierOrderNode(nodeFun1, OrderQuantifier.BETWEEN, 1..4),
                            QuantifierOrderNode(
                                SequenceOrderNode(nodeFun1, nodeFun2),
                                OrderQuantifier.MAYBE
                            )
                        ),
                        AlternativeOrderNode(nodeFun2, nodeFun1)
                    ),
                    nodeFun1
                )

            assertEquals(expected = expectedSyntaxTree, actual = syntaxTree)
        }
    }
}
