package de.fraunhofer.aisec.codyze.crymlin

import de.fraunhofer.aisec.codyze.analysis.*
import de.fraunhofer.aisec.codyze.analysis.resolution.ConstantValue
import org.junit.jupiter.api.*

internal class StructuresTest {
    @Test
    fun testConstantValueEquals() {
        val nullCV: ConstantValue = ErrorValue.newErrorValue("narf")
        val otherNullCV: ConstantValue = ErrorValue.newErrorValue("other")
        val oneCV = ConstantValue.of(1)
        val otheroneCV = ConstantValue.of(1)
        val twoCV = ConstantValue.of(2)
        Assertions.assertNotEquals(nullCV, oneCV)
        Assertions.assertNotEquals(nullCV, otherNullCV)
        Assertions.assertNotEquals(oneCV, twoCV)
        Assertions.assertEquals(oneCV, otheroneCV)
        Assertions.assertNotEquals(oneCV, Any())
    }
}
