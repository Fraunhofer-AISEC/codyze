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
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class EqualityTest {

    @Test
    fun `test Definition equality`() {
        with(mockk<FunctionOp>(relaxed = true)) {
            val def1 = definition("Test.test") {
                signature {
                    - "string"
                    - listOf(1, 2)
                }
                signature(1, 3, 5)
                signature {
                    - "h"
                }
            }

            val def2 = definition("Test.test") {
                signature(1, 3, 5)
                signature {
                    - "h"
                }
                signature {
                    - "string"
                    - listOf(1, 2)
                }
            }

            assertEquals(def1, def2)
        }
    }

    @Test
    fun `test FunctionOp equality`() {
        val op1 = op {
            definition("Test.test1") {}
            definition("Test.test2") {}
            definition("Test.test3") {}
        }

        val op2 = op {
            definition("Test.test2") {}
            definition("Test.test1") {}
            definition("Test.test3") {}
        }

        assertEquals(op1, op2)
    }

    @Test
    fun `test ConstructorOp equality`() {
        val op1 = constructor("my.Test") {
            signature(1, 2, 3)
            signature {
                - setOf("h", "he", "hel")
                - mapOf(1 to "one", 2 to "two")
            }
            signature("h", "test")
        }

        val op2 = constructor("my.Test") {
            signature("h", "test")
            signature {
                - setOf("h", "he", "hel")
                - mapOf(1 to "one", 2 to "two")
            }
            signature(1, 2, 3)
        }

        assertEquals(op1, op2)
    }

    @Test
    fun `test ParameterGroup equality`() {
        with(mockk<Signature>(relaxed = true)) {
            val group1 = group {
                - "h"
                - "string"
                - (1..4)
            }

            val group2 = group {
                - (1..4)
                - "h"
                - "string"
            }

            assertEquals(group1, group2)
        }
    }

    @Test
    fun `test Signature equality`() {
        with(mockk<Definition>(relaxed = true)) {
            val sig1 = signature {
                - "one"
                - listOf(0.1, 3.2)
                unordered(1, setOf(2, 4, 6), "test")
            }

            val sig2 = signature {
                - "one"
                - listOf(0.1, 3.2)
                unordered("test", 1, setOf(2, 4, 6))
            }

            val differentSig = signature {
                - listOf(0.1, 3.2)
                - "one"
                unordered(1, setOf(2, 4, 6), "test")
            }

            assertEquals(sig1, sig2)
            assertNotEquals(sig1, differentSig)
        }
    }
}
