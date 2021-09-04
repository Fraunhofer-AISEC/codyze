package de.fraunhofer.aisec.codyze.crymlin

import de.fraunhofer.aisec.codyze.analysis.markevaluation.ExpressionHelper
import org.junit.jupiter.api.*

internal class ExpressionHelperTest {
    @Test
    fun testComparableInt() {
        Assertions.assertEquals("0.0", ExpressionHelper.toComparableString(0))
        Assertions.assertEquals("0.0", ExpressionHelper.toComparableString(0.0))
        Assertions.assertEquals("1.0", ExpressionHelper.toComparableString(1))
        Assertions.assertEquals("-1.0", ExpressionHelper.toComparableString(-1))
        Assertions.assertEquals("-2147483648.0", ExpressionHelper.toComparableString(Int.MIN_VALUE))
        Assertions.assertEquals("2147483647.0", ExpressionHelper.toComparableString(Int.MAX_VALUE))
    }

    @Test
    fun testComparableDouble() {
        Assertions.assertEquals("0.0", ExpressionHelper.toComparableString(0.0))
        Assertions.assertEquals("0.0", ExpressionHelper.toComparableString(0.0))
        Assertions.assertEquals("1.0", ExpressionHelper.toComparableString(1.0))
        Assertions.assertEquals("-1.0", ExpressionHelper.toComparableString(-1.0))
        Assertions.assertEquals("4.9E-324", ExpressionHelper.toComparableString(Double.MIN_VALUE))
        Assertions.assertEquals(
            "1.7976931348623157E308",
            ExpressionHelper.toComparableString(Double.MAX_VALUE)
        )
    }

    @Test
    fun testComparableString() {
        Assertions.assertEquals("", ExpressionHelper.toComparableString(""))
        Assertions.assertEquals("a", ExpressionHelper.toComparableString("a"))
        Assertions.assertEquals("1.0", ExpressionHelper.toComparableString("1"))
        Assertions.assertEquals("-1.0", ExpressionHelper.toComparableString("-1"))
    }
}
