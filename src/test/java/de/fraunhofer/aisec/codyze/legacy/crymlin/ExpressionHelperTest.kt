package de.fraunhofer.aisec.codyze.legacy.crymlin

import de.fraunhofer.aisec.codyze.legacy.analysis.markevaluation.ExpressionHelper
import de.fraunhofer.aisec.mark.markDsl.impl.*
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import org.junit.jupiter.api.*

internal class ExpressionHelperTest {
    @Test
    fun testComparableInt() {
        assertEquals("0.0", ExpressionHelper.toComparableString(0))
        assertEquals("0.0", ExpressionHelper.toComparableString(0.0))
        assertEquals("1.0", ExpressionHelper.toComparableString(1))
        assertEquals("-1.0", ExpressionHelper.toComparableString(-1))
        assertEquals("-2147483648.0", ExpressionHelper.toComparableString(Int.MIN_VALUE))
        assertEquals("2147483647.0", ExpressionHelper.toComparableString(Int.MAX_VALUE))
    }

    @Test
    fun testComparableDouble() {
        assertEquals("0.0", ExpressionHelper.toComparableString(0.0))
        assertEquals("0.0", ExpressionHelper.toComparableString(0.0))
        assertEquals("1.0", ExpressionHelper.toComparableString(1.0))
        assertEquals("-1.0", ExpressionHelper.toComparableString(-1.0))
        assertEquals("4.9E-324", ExpressionHelper.toComparableString(Double.MIN_VALUE))
        assertEquals(
            "1.7976931348623157E308",
            ExpressionHelper.toComparableString(Double.MAX_VALUE)
        )
    }

    @Test
    fun testComparableFloat() {
        assertEquals("0.0", ExpressionHelper.toComparableString(0.toFloat()))
        assertEquals("0.0", ExpressionHelper.toComparableString(0.0f))
        assertEquals("1.2", ExpressionHelper.toComparableString(1.2f))
        assertEquals("-1.2", ExpressionHelper.toComparableString(-1.2f))
        assertEquals("1.4E-45", ExpressionHelper.toComparableString(Float.MIN_VALUE))
        assertEquals("3.4028235E38", ExpressionHelper.toComparableString(Float.MAX_VALUE))
    }

    @Test
    fun testComparableString() {
        assertEquals("", ExpressionHelper.toComparableString(""))
        assertEquals("a", ExpressionHelper.toComparableString("a"))
        assertEquals("1.0", ExpressionHelper.toComparableString("1"))
        assertEquals("-1.0", ExpressionHelper.toComparableString("-1"))
        assertEquals("a", ExpressionHelper.toComparableString("\"a"))

        // doesn't work as expected
        // assertEquals("ab", ExpressionHelper.toComparableString("ab\""))
        // assertEquals("abc", ExpressionHelper.toComparableString("\"abc\""))

        assertEquals("4.0", ExpressionHelper.toComparableString(4.toByte()))
        assertEquals("a", ExpressionHelper.toComparableString('a'))
    }

    @Test
    fun testComparableStringObject() {
        // uses String implementation -> NullPointerException
        // assertEquals("", ExpressionHelper.toComparableString(null))

        val nullObject: kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType.Object? = null
        assertEquals("", ExpressionHelper.toComparableString(nullObject))
    }

    @Test
    fun testCollectVars() {
        val vars = mutableSetOf<String>()
        val expr =
            funcCall(
                "function",
                logicalAnd(
                    left =
                        comparison(
                            left = mul(left = operand("b"), op = "*", right = lit(1)),
                            op = "==",
                            right = operand("a")
                        ),
                    op = "&&",
                    right =
                        logicalOr(
                            left =
                                comparison(
                                    left = operand("a"),
                                    op = "==",
                                    right = unary(exp = operand("c"), op = "-")
                                ),
                            op = "||",
                            right = comparison(left = lit(1), op = "==", right = lit(1))
                        )
                ),
                litList(lit(1), lit("s"), lit("1"))
            )

        val expectedVars = setOf("a", "b", "c")

        assertNotNull(expr)
        ExpressionHelper.collectVars(expr, vars)
        assertEquals(expectedVars.size, vars.size, "Size of vars set was not equal")
        for (variable in expectedVars) {
            assertContains(vars, variable, "\"$variable\" was not in vars set")
        }
    }
}
