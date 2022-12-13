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
package de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.modelling

import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.*
import io.mockk.mockk
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class OpComponentsTest {

    @ParameterizedTest(name = "[{index}] test with {0} parameters")
    @MethodSource("unaryPlusParamHelper")
    fun `test group`(expectedParams: ArrayList<Parameter>) {
        with(mockk<Signature>(relaxed = true)) {
            val paramGroup = group { expectedParams.forEach { +it } }

            assertContentEquals(expectedParams, paramGroup.parameters)
        }
    }

    @ParameterizedTest(name = "[{index}] {1}")
    @MethodSource("unaryPlusParamHelper")
    fun `test unaryPlus in Signature`(
        expectedParams: ArrayList<Parameter>
    ) {
        val sig = Signature()
        with(sig) { expectedParams.forEach { +it } }
        assertContentEquals(
            expectedParams,
            sig.parameters,
            "parameters in Signature were not as expected"
        )
    }

    @Test
    fun `test simple signature`() {
        with(mockk<Definition>(relaxed = true)) {
            val sig = signature { +singleParam }
            val sigShortcut = signature(singleParam)

            val expectedSig = Signature().apply { parameters.add(singleParam) }

            assertEquals(expectedSig, sig)
            assertEquals(expectedSig, sigShortcut)
        }
    }

    @Test
    fun `test signature with unordered`() {
        with(mockk<Definition>(relaxed = true)) {
            val unordered = signature(multipleParams.toTypedArray()) {}
            val unorderedShortcut = signature().unordered(*multipleParams.toTypedArray())

            val expectedSig = Signature().apply { unorderedParameters.addAll(multipleParams) }
            assertEquals(expectedSig, unorderedShortcut)
            assertEquals(expectedSig, unordered)
        }
    }

    @Test
    fun `test signature with group`() {
        with(mockk<Definition>(relaxed = true)) {
            val sig = signature { group { multipleParams.forEach { +it } } }

            val groupShortcut = signature { group(*multipleParams.toTypedArray()) }

            val expectedSig =
                Signature().apply {
                    parameters.add(ParameterGroup().apply { parameters.addAll(multipleParams) })
                }

            assertEquals(expectedSig, sig)
            assertEquals(expectedSig, groupShortcut)
        }
    }

    @Test
    fun `test complex signature`() {
        val (allOrdered, unordered) = multipleParams.partition { Random.nextBoolean() }
        val (ordered, grouped) = allOrdered.partition { Random.nextBoolean() }

        with(mockk<Definition>(relaxed = true)) {
            val sig =
                signature {
                    +singleParam
                    group { grouped.forEach { +it } }
                    ordered.forEach { +it }
                }
                    .unordered(*unordered.toTypedArray())

            val expectedSig =
                Signature().apply {
                    parameters.add(singleParam)
                    parameters.add(ParameterGroup().apply { parameters.addAll(grouped) })
                    parameters.addAll(ordered)

                    unorderedParameters.addAll(unordered)
                }

            assertEquals(expectedSig, sig)
        }
    }

    @Test
    fun `test definition`() {
        val sig1 = mockk<Signature>()
        val sig2 = mockk<Signature>()

        with(mockk<FunctionOp>(relaxed = true)) {
            val def = definition("fqn") {
                add(sig1)
                add(sig2)
            }

            val expected = Definition("fqn")
            expected.signatures.add(sig1)
            expected.signatures.add(sig2)

            assertEquals(expected, def)
        }
    }

    @Test
    fun `test add component twice`() {
        val sig = mockk<Signature>()

        with(mockk<FunctionOp>(relaxed = true)) {
            val def = definition("fqn") {
                add(sig)
                add(sig)
            }

            val expected = Definition("fqn")
            expected.signatures.add(sig)

            assertEquals(expected, def)
        }
    }

    companion object {
        val singleParam = arrayListOf<Parameter>("test")
        val multipleParams =
            arrayListOf<Parameter>("test", emptyList<Parameter>(), Type("fqn"), intArrayOf(1, 2))

        @JvmStatic
        @Suppress("UnusedPrivateMember")
        private fun unaryPlusParamHelper(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(singleParam, "Test with single parameter"),
                Arguments.of(multipleParams, "Test with multiple parameters")
            )
        }
    }
}
