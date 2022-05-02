package de.fraunhofer.aisec.codyze.legacy.crymlin

import de.fraunhofer.aisec.codyze.legacy.analysis.*
import de.fraunhofer.aisec.codyze.legacy.analysis.resolution.ConstantValue
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import org.junit.jupiter.api.*

internal class StructuresTest {
    @Test
    fun testConstantValueEquals() {
        val nullCV: ConstantValue = ErrorValue.newErrorValue("narf")
        val otherNullCV: ConstantValue = ErrorValue.newErrorValue("other")
        val oneCV = ConstantValue.of(1)
        val otheroneCV = ConstantValue.of(1)
        val twoCV = ConstantValue.of(2)
        assertNotEquals(nullCV, oneCV)
        assertNotEquals(nullCV, otherNullCV)
        assertNotEquals(oneCV, twoCV)
        assertEquals(oneCV, otheroneCV)
        assertNotEquals(oneCV, Any())
    }
}
