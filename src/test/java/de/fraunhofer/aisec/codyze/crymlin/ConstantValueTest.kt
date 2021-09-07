package de.fraunhofer.aisec.codyze.crymlin

import de.fraunhofer.aisec.codyze.analysis.resolution.ConstantValue
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test

internal class ConstantValueTest {
    @Test
    fun testTryOfInteger() {
        val test = 42
        val res = ConstantValue.tryOf(test)
        assertEquals(res.get().value, test)
    }

    @Test
    fun testTryOfFloat() {
        val test = 42f
        val res = ConstantValue.tryOf(test)
        assertEquals(res.get().value, test)
    }

    @Test
    fun testTryOfBoolean() {
        val test = true
        val res = ConstantValue.tryOf(test)
        assertEquals(res.get().value, test)
    }

    @Test
    fun testTryOfString() {
        val test = "test"
        val res = ConstantValue.tryOf(test)
        assertEquals(res.get().value, test)
    }
}
