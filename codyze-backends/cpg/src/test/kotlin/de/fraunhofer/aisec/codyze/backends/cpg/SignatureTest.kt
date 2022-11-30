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
package de.fraunhofer.aisec.codyze.backends.cpg

import de.fraunhofer.aisec.codyze.backends.cpg.coko.dsl.*
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.CokoBackend
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.dsl.Type
import de.fraunhofer.aisec.cpg.graph.statements.expressions.CallExpression
import de.fraunhofer.aisec.cpg.graph.statements.expressions.Expression
import de.fraunhofer.aisec.cpg.graph.statements.expressions.Literal
import io.mockk.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SignatureTest {
    val node = mockk<CallExpression>()
    val backend = mockk<CokoBackend>()
    val stringArgument = mockk<Literal<String>>()
    val pairArgument = mockk<Literal<Pair<Int, String>>>()

    @AfterEach
    fun clearMockks() {
        clearAllMocks()
    }

    @Test
    fun `test signature with wrong number of params`() {
        every { node.arguments } returns listOf()

        assertFalse { with(backend) { with(node) { signature("test") } } }
    }

    @Test
    fun `test signature with null`() {
        every { node.arguments } returns listOf(mockk<CallExpression>())

        assertFalse { with(backend) { with(node) { signature(null) } } }
    }

    @Test
    fun `test signature with Type`() {
        every { node.arguments } returns listOf(stringArgument)
        every { stringArgument.type.typeName } returns "kotlin.String"

        assertTrue { with(backend) { with(node) { signature(Type("kotlin.String")) } } }
    }

    @Test
    fun `test signature with wrong Type`() {
        every { node.arguments } returns listOf(stringArgument)
        every { stringArgument.type.typeName } returns "kotlin.String"

        assertFalse { with(backend) { with(node) { signature(Type("kotlin.Int")) } } }
    }

    @Test
    fun `test signature with Value to Type Pair`() {
        every { node.arguments } returns listOf(stringArgument)
        every { stringArgument.type.typeName } returns "kotlin.String"
        every { stringArgument.value } returns "test"

        assertTrue { with(backend) { with(node) { signature("test" to Type("kotlin.String")) } } }
    }

    @Test
    fun `test signature with Pair with wrong Type`() {
        every { node.arguments } returns listOf(stringArgument)
        every { stringArgument.type.typeName } returns "kotlin.String"
        every { stringArgument.value } returns "test"

        assertFalse { with(backend) { with(node) { signature("test" to Type("kotlin.Int")) } } }
    }

    @Test
    fun `test signature with Pair with wrong value`() {
        every { node.arguments } returns listOf(stringArgument)
        every { stringArgument.type.typeName } returns "kotlin.String"
        every { stringArgument.value } returns "test"

        assertFalse { with(backend) { with(node) { signature(1 to Type("kotlin.String")) } } }
    }

    @Test
    fun `test signature with normal Pair`() {
        val param = 1 to "one"

        every { node.arguments } returns listOf(pairArgument)
        every { pairArgument.value } returns (1 to "one")

        mockkStatic("de.fraunhofer.aisec.codyze_backends.cpg.coko.dsl.ImplementationDslKt")
        every { with(node) { param.flowsTo(pairArgument) } } returns true

        // tests that with normal pair only flowsTo is called
        with(backend) { with(node) { signature(param) } }
        verify { with(node) { param.flowsTo(pairArgument) } }
    }

    @Test
    fun `test signature with single param`() {
        val param = "test"
        every { node.arguments } returns listOf(stringArgument)
        every { stringArgument.value } returns "test"

        mockkStatic("de.fraunhofer.aisec.codyze_backends.cpg.coko.dsl.ImplementationDslKt")
        every { with(node) { param.flowsTo(stringArgument) } } returns true

        // assert that signature checks the dataflow from the parameter to the argument
        with(backend) { with(node) { signature(param) } }
        verify { with(node) { param.flowsTo(stringArgument) } }
    }

    @Test
    fun `test signature with multiple params`() {
        val params = arrayOf("test", 1, mockk<CallExpression>(), listOf(1, 2, 5), Any())
        val args = arrayListOf<Expression>()
        mockkStatic("de.fraunhofer.aisec.codyze_backends.cpg.coko.dsl.ImplementationDslKt")
        for (p in params) {
            val a = mockk<Literal<String>>()
            args.add(a)
            every { a.value } returns "test"
            every { with(node) { p.flowsTo(a) } } returns true
        }
        every { node.arguments } returns args

        // assert that signature checks the dataflow from the parameter to the argument at the same
        // position
        with(backend) { with(node) { signature(*params) } }
        for (i in args.indices) verify { with(node) { params[i].flowsTo(args[i]) } }
    }
    // TODO hasVarargs
}
