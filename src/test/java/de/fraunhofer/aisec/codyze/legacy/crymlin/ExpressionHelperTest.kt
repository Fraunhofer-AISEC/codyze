package de.fraunhofer.aisec.codyze.legacy.crymlin

import de.fraunhofer.aisec.codyze.legacy.analysis.markevaluation.ExpressionHelper
import kotlin.test.assertEquals
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
    fun testComparableString() {
        assertEquals("", ExpressionHelper.toComparableString(""))
        assertEquals("a", ExpressionHelper.toComparableString("a"))
        assertEquals("1.0", ExpressionHelper.toComparableString("1"))
        assertEquals("-1.0", ExpressionHelper.toComparableString("-1"))
    }
}
