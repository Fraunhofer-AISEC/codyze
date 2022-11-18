package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.order

import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.CokoBackend
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.dsl.*
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.ordering.*
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
        fun fun1() = op {}
        fun fun2() = op {}
    }

    context(CokoBackend)
    private fun orderExpressionToSyntaxTree(block: Order.() -> Unit): OrderNode {
        val order = Order().apply(block)
        return order.toNode()
    }

    @Test
    /** Tests an order with a no token at all -> this should error */
    fun `test zero node order`() {
        with(mockk<CokoBackend>()) {
            assertFailsWith<IllegalArgumentException>(
                message = "Groups and sets must have at least one element.",
                block = { orderExpressionToSyntaxTree {} }
            )
        }
    }

    @Test
    /** Tests an order with an empty group -> this should error */
    fun `test order with empty set`() {
        with(mockk<CokoBackend>()) {
            assertFailsWith<IllegalArgumentException>(
                message = "Groups and sets must have at least one element.",
                block = {
                    orderExpressionToSyntaxTree {
                        +TestClass::fun1
                        +group {}
                    }
                }
            )
        }
    }

    @Test
    /** Tests a simple sequence order */
    fun `test simple sequence order`() {
        with(mockk<CokoBackend>()) {
            val syntaxTree = orderExpressionToSyntaxTree {
                +TestClass::fun1
                +TestClass::fun2
            }

            val expectedSyntaxTree =
                SequenceOrderNode(
                    left = TestClass::fun1.toTerminalOrderNode(),
                    right = TestClass::fun2.toTerminalOrderNode()
                )

            assertEquals(expected = expectedSyntaxTree, actual = syntaxTree)
        }
    }

    @Test
    /** Tests a simple alternative order */
    fun `test simple alternative order`() {
        with(mockk<CokoBackend>()) {
            val syntaxTree = orderExpressionToSyntaxTree { +(TestClass::fun1 or TestClass::fun2) }

            val expectedSyntaxTree =
                AlternativeOrderNode(
                    left = TestClass::fun1.toTerminalOrderNode(),
                    right = TestClass::fun2.toTerminalOrderNode()
                )

            assertEquals(expected = expectedSyntaxTree, actual = syntaxTree)
        }
    }

    @Test
    /** Tests an order with a [QuantifierOrderNode] */
    fun `test order with an QuantifierOrderNode`() {
        with(mockk<CokoBackend>()) {
            val syntaxTree = orderExpressionToSyntaxTree { +TestClass::fun1.between(1..4) }
            val expectedSyntaxTree =
                QuantifierOrderNode(
                    child = TestClass::fun1.toTerminalOrderNode(),
                    type = OrderQuantifier.BETWEEN,
                    value = 1..4
                )

            assertEquals(expected = expectedSyntaxTree, actual = syntaxTree)
        }
    }

    @Test
    /** Tests an order with a group */
    fun `test order with group`() {
        with(mockk<CokoBackend>()) {
            val syntaxTree = orderExpressionToSyntaxTree {
                +group {
                        +TestClass::fun1
                        +TestClass::fun2
                    }
                    .maybe()
            }

            val syntaxTreeShortcut = orderExpressionToSyntaxTree {
                +maybe {
                    +TestClass::fun1
                    +TestClass::fun2
                }
            }
            assertEquals(syntaxTree, syntaxTreeShortcut)

            val expectedSyntaxTree =
                QuantifierOrderNode(
                    SequenceOrderNode(
                        left = TestClass::fun1.toTerminalOrderNode(),
                        right = TestClass::fun2.toTerminalOrderNode()
                    ),
                    OrderQuantifier.MAYBE
                )

            assertEquals(expected = expectedSyntaxTree, actual = syntaxTree)
        }
    }

    @Test
    /** Tests an order with a set */
    fun `test order with set`() {
        with(mockk<CokoBackend>()) {
            val syntaxTree = orderExpressionToSyntaxTree {
                +set {
                    +TestClass::fun1
                    +TestClass::fun2
                }
            }

            val syntaxTreeShortcut = orderExpressionToSyntaxTree {
                +set[TestClass::fun1, TestClass::fun2]
            }

            assertEquals(syntaxTree, syntaxTreeShortcut)

            val expectedSyntaxTree =
                AlternativeOrderNode(
                    TestClass::fun1.toTerminalOrderNode(),
                    TestClass::fun2.toTerminalOrderNode()
                )

            assertEquals(expected = expectedSyntaxTree, actual = syntaxTree)
        }
    }

    @Test
    /** Tests a complex order */
    fun `test complex order`() {
        with(mockk<CokoBackend>()) {
            val syntaxTree = orderExpressionToSyntaxTree {
                +TestClass::fun1.between(1..4)
                +maybe(TestClass::fun1, TestClass::fun2)
                +set[TestClass::fun2, TestClass::fun1]
                +TestClass::fun1
            }

            val nodeFun1 = TestClass::fun1.toTerminalOrderNode()
            val nodeFun2 = TestClass::fun2.toTerminalOrderNode()

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
