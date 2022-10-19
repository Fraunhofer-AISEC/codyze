package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.dsl

import de.fraunhofer.aisec.cpg.graph.statements.expressions.CallExpression
import de.fraunhofer.aisec.cpg.graph.statements.expressions.Literal
import io.mockk.*
import org.junit.jupiter.api.AfterEach
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse

class SignatureTest {
    val node = mockk<CallExpression>()
    val stringArgument = mockk<Literal<String>>()
    val pairArgument = mockk<Literal<Pair<Int, String>>>()

    @AfterEach
    fun clearMockks() {
        clearAllMocks()
    }

    @Test
    fun `test signature with wrong number of params`() {
        every { node.arguments } returns listOf()

        assertFalse { with(node) { signature("test") } }
    }

    @Test
    fun `test signature with null`() {
        every { node.arguments } returns listOf(mockk<CallExpression>())

        assertFalse { with(node) { signature(null) } }
    }

    @Test
    fun `test signature with Type`() {
        every { node.arguments } returns listOf(stringArgument)
        every { stringArgument.type.typeName } returns "kotlin.String"

        assertTrue { with(node) { signature(Type("kotlin.String")) } }
    }

    @Test
    fun `test signature with wrong Type`() {
        every { node.arguments } returns listOf(stringArgument)
        every { stringArgument.type.typeName } returns "kotlin.String"

        assertFalse { with(node) { signature(Type("kotlin.Int")) } }
    }

    @Test
    fun `test signature with Value to Type Pair`() {
        every { node.arguments } returns listOf(stringArgument)
        every { stringArgument.type.typeName } returns "kotlin.String"
        every { stringArgument.value } returns "test"

        assertTrue { with(node) { signature("test" to Type("kotlin.String")) } }
    }

    @Test
    fun `test signature with Pair with wrong Type`() {
        every { node.arguments } returns listOf(stringArgument)
        every { stringArgument.type.typeName } returns "kotlin.String"
        every { stringArgument.value } returns "test"

        assertFalse { with(node) { signature("test" to Type("kotlin.Int")) } }
    }

    @Test
    fun `test signature with Pair with wrong value`() {
        every { node.arguments } returns listOf(stringArgument)
        every { stringArgument.type.typeName } returns "kotlin.String"
        every { stringArgument.value } returns "test"

        assertFalse { with(node) { signature(1 to Type("kotlin.String")) } }
    }

    @Test
    fun `test signature with normal Pair`() {
        val param = 1 to "one"

        every { node.arguments } returns listOf(pairArgument)
        every { pairArgument.value } returns (1 to "one")

        mockkStatic("de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.dsl.ImplementationDslKt")
        every { with(node) { param.flowsTo(pairArgument) } } returns true

        // tests that with normal pair only flowsTo is called
        with(node) { signature(param) }
        verify { with(node) { param.flowsTo(pairArgument) } }
    }

    @Test
    fun `test signature with String`() {
        every { node.arguments } returns listOf(stringArgument)
        every { stringArgument.value } returns "test"

        assertTrue { with(node) { signature("test") } }
    }

    @Test
    fun `test signature with Wildcard`() {
        every { node.arguments } returns listOf(stringArgument)

        assertTrue { with(node) { signature(Wildcard) } }
    }
    //TODO hasVarargs

}
