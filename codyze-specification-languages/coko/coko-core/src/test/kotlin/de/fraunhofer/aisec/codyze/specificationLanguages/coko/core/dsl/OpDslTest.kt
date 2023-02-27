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
package de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl

import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.modelling.Definition
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.modelling.Parameter
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.modelling.ParameterGroup
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.modelling.Signature
import io.mockk.mockk
import org.junit.jupiter.api.Test
import kotlin.reflect.KFunction
import kotlin.test.assertEquals

class OpDslTest {
    @Test
    fun `test simple op`() {
        val mockDefinition = mockk<Definition>()

        val actualOp = op { add(mockDefinition) }
        val expectedOp = FunctionOp().apply { definitions.add(mockDefinition) }

        assertEquals(expectedOp, actualOp)
    }

    @Test
    fun `test add same definition twice in FunctionOp`() {
        val mockDefinition = mockk<Definition>()

        val actualOp = op {
            add(mockDefinition)
            add(mockDefinition)
        }
        val expectedOp = FunctionOp().apply { definitions.add(mockDefinition) }

        assertEquals(expectedOp, actualOp)
    }

    @Test
    @Suppress("LongMethod")
    fun `test complete op`() {
        val stringParam: Parameter = "test string"
        val numberParam: Parameter = 123
        val typeParam: Parameter = Type("fqn")
        val arrayParam = arrayOf<Any>()
        val callTestParam = mockk<KFunction<*>>()
        val collectionParam = mockk<Collection<Any>>()

        val actualOp = op {
            "fqn1" {
                signature { +stringParam }
                signature(stringParam, callTestParam)
                signature {
                    +stringParam
                    +callTestParam
                }.unordered(numberParam)
                signature(numberParam, collectionParam, stringParam)
            }
            definition("fqn2") {
                signature {
                    +typeParam
                    group {
                        +stringParam
                        +arrayParam
                    }
                }
            }
        }

        val def1 = Definition("fqn1")
        def1.signatures.add(Signature().apply { parameters.add(stringParam) })
        def1.signatures.add(
            Signature().apply {
                parameters.add(stringParam)
                parameters.add(callTestParam)
            }
        )
        def1.signatures.add(
            Signature().apply {
                parameters.add(stringParam)
                parameters.add(callTestParam)
                unorderedParameters.add(numberParam)
            }
        )
        def1.signatures.add(
            Signature().apply {
                parameters.add(numberParam)
                parameters.add(collectionParam)
                parameters.add(stringParam)
            }
        )

        val def2 = Definition("fqn2")
        def2.signatures.add(
            Signature().apply {
                parameters.add(typeParam)
                parameters.add(
                    ParameterGroup().apply {
                        parameters.add(stringParam)
                        parameters.add(arrayParam)
                    }
                )
            }
        )

        val expectedOp = FunctionOp()
        expectedOp.definitions.add(def1)
        expectedOp.definitions.add(def2)

        assertEquals(expectedOp, actualOp)
    }

    @Test
    fun `test simple ConstructorOp`() {
        val mockSignature = mockk<Signature>()

        val actualConstructorOp = constructor("fqn") { add(mockSignature) }
        val expectedConstructorOp = ConstructorOp("fqn").apply { signatures.add(mockSignature) }

        assertEquals(expectedConstructorOp, actualConstructorOp)
    }

    @Test
    fun `test add same signature twice in ConstructorOp`() {
        val mockSignature = mockk<Signature>()

        val actualConstructorOp = constructor("fqn") {
            add(mockSignature)
            add(mockSignature)
        }
        val expectedConstructorOp = ConstructorOp("fqn").apply { signatures.add(mockSignature) }

        assertEquals(expectedConstructorOp, actualConstructorOp)
    }
}
