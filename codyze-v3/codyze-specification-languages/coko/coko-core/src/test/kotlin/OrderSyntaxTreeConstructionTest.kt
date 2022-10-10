import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.dsl.*
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.ordering.*
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.Test

/**
 * Tests whether a coko order expression can be correctly converted into a syntax tree. This is the
 * first step in evaluating the order. The resulting syntax tree is then converted to a NFA, which
 * is converted to a DFA afterwards.
 */
class OrderSyntaxTreeConstructionTest {
    class TestClass {
        fun fun1() = {}
        fun fun2() = {}
    }
    val baseName = "class OrderSyntaxTreeConstructionTest\$TestClass"

    private inline fun orderExpressionToSyntaxTree(block: Order.() -> Unit): OrderNode {
        val order = Order().apply(block)
        return order.toNode()
    }

    @Test
    /** Tests an order with a no token at all -> this should error */
    fun `test zero node order`() {
        assertFailsWith<IllegalArgumentException>(
            message = "Groups and sets must have at least one element.",
            block = { orderExpressionToSyntaxTree {} }
        )
    }

    @Test
    /** Tests an order with an empty group -> this should error */
    fun `test order with empty set`() {
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

    @Test
    /** Tests a simple sequence order */
    fun `test simple sequence order`() {
        val syntaxTree = orderExpressionToSyntaxTree {
            +TestClass::fun1
            +TestClass::fun2
        }

        val expectedSyntaxTree =
            SequenceOrderNode(
                left = TerminalOrderNode(baseName, "fun1"),
                right = TerminalOrderNode(baseName, "fun2")
            )

        assertEquals(expected = expectedSyntaxTree, actual = syntaxTree)
    }

    @Test
    /** Tests a simple alternative order */
    fun `test simple alternative order`() {
        val syntaxTree = orderExpressionToSyntaxTree { +(TestClass::fun1 or TestClass::fun2) }

        val expectedSyntaxTree =
            AlternativeOrderNode(
                left = TerminalOrderNode(baseName, "fun1"),
                right = TerminalOrderNode(baseName, "fun2")
            )

        assertEquals(expected = expectedSyntaxTree, actual = syntaxTree)
    }

    @Test
    /** Tests an order with a [QuantifierOrderNode] */
    fun `test order with an QuantifierOrderNode`() {
        val syntaxTree = orderExpressionToSyntaxTree { +TestClass::fun1.between(1..4) }
        val expectedSyntaxTree =
            QuantifierOrderNode(
                child = TerminalOrderNode(baseName, "fun1"),
                type = OrderQuantifier.BETWEEN,
                value = 1..4
            )

        assertEquals(expected = expectedSyntaxTree, actual = syntaxTree)
    }

    @Test
    /** Tests an order with a group */
    fun `test order with group`() {
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
                    left = TerminalOrderNode(baseName, "fun1"),
                    right = TerminalOrderNode(baseName, "fun2")
                ),
                OrderQuantifier.MAYBE
            )

        assertEquals(expected = expectedSyntaxTree, actual = syntaxTree)
    }

    @Test
    /** Tests an order with a set */
    fun `test order with set`() {
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
                TerminalOrderNode(baseName, "fun1"),
                TerminalOrderNode(baseName, "fun2")
            )

        assertEquals(expected = expectedSyntaxTree, actual = syntaxTree)
    }

    @Test
    /** Tests a complex order */
    fun `test complex order`() {
        val syntaxTree = orderExpressionToSyntaxTree {
            +TestClass::fun1.between(1..4)
            +maybe(TestClass::fun1, TestClass::fun2)
            +set[TestClass::fun2, TestClass::fun1]
            +TestClass::fun1
        }

        val nodeFun1 = TerminalOrderNode(baseName, "fun1")
        val nodeFun2 = TerminalOrderNode(baseName, "fun2")

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
