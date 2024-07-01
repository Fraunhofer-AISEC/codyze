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
package de.fraunhofer.aisec.codyze.backends.cpg.coko.dsl

import de.fraunhofer.aisec.codyze.backends.cpg.coko.CokoCpgBackend
import de.fraunhofer.aisec.codyze.backends.cpg.createCpgConfiguration
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import kotlin.io.path.toPath
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ImplementationDslTest {

    @Test
    fun `test cpgGetAllNodes`() {
        val op = op {
            "Foo.fun" {
                signature(2)
            }
        }
        with(simpleBackend) {
            val allNodes = op.cpgGetAllNodes()
            assertEquals(
                5,
                allNodes.size,
                "cpgGetAllNodes returned ${allNodes.size} node(s) instead of 5 nodes fo the Op: $op."
            )
        }
    }

    @Test
    fun `test cpgGetNodes`() {
        val op = op {
            "Foo.fun" {
                signature(1..10)
            }
        }
        with(simpleBackend) {
            val nodes = op.cpgGetNodes()
            assertEquals(
                2,
                nodes.size,
                "cpgGetNodes returned ${nodes.size} node(s) instead of 2 nodes for the Op: $op."
            )
        }
    }

    @Test
    fun `test cpgGetNodes with unordered parameters`() {
        val op = op {
            "Foo.bar" {
                signature(arrayOf(".*Test.*")) {
                    - Wildcard
                    - Wildcard
                }
            }
        }
        with(simpleBackend) {
            val nodes = op.cpgGetNodes()
            assertEquals(
                3,
                nodes.size,
                "cpgGetNodes returned ${nodes.size} node(s) instead of 3 nodes for the Op: $op."
            )
        }
    }

    @Test
    fun `test Array Length`() {
        val opX: MutableList<Op> = mutableListOf()
        for (i in 0..3) {
            opX += op {
                "Foo.fun" {
                    signature(
                        Length(i..i)
                    )
                }
            }
        }

        val results = arrayOf(1, 0, 1, 2)
        for (i in 0..3) {
            with(lengthBackend) {
                val nodes = opX[i].cpgGetNodes()
                val validNodes = nodes.filter { it.value == Result.VALID }
                assertEquals(
                    results[i],
                    validNodes.size,
                    "cpgGetNodes returned ${validNodes.size} node(s) instead of ${results[i]} nodes for the Op: ${opX[i]}."
                )
                assertEquals(
                    1,
                    nodes.filter { it.value == Result.OPEN }.size,
                    "cpgGetNodes did not return exactly one OPEN result as expected."
                )
            }
        }
    }

    @Test
    fun `test List Length`() {
        val opX: MutableList<Op> = mutableListOf()
        for (i in 0..3) {
            opX += op {
                "Foo.bar" {
                    signature(
                        Length(i..i)
                    )
                }
            }
        }

        val results = arrayOf(1, 0, 1, 2)
        for (i in 0..3) {
            with(lengthBackend) {
                val nodes = opX[i].cpgGetNodes()
                val validNodes = nodes.filter { it.value == Result.VALID }
                assertEquals(
                    results[i],
                    validNodes.size,
                    "cpgGetNodes returned ${validNodes.size} node(s) instead of ${results[i]} nodes for the Op: ${opX[i]}."
                )
                assertEquals(
                    1,
                    nodes.filter { it.value == Result.OPEN }.size,
                    "cpgGetNodes did not return exactly one OPEN result as expected."
                )
            }
        }
    }

    companion object {

        lateinit var simpleBackend: CokoCpgBackend
        lateinit var lengthBackend: CokoCpgBackend

        @BeforeAll
        @JvmStatic
        fun startup() {
            val classLoader = ImplementationDslTest::class.java.classLoader

            val simpleFileResource = classLoader.getResource("ImplementationDslTest/SimpleJavaFile.java")
            val lengthFileResource = classLoader.getResource("ImplementationDslTest/LengthJavaFile.java")

            assertNotNull(simpleFileResource)
            assertNotNull(lengthFileResource)

            val simpleFile = simpleFileResource.toURI().toPath()
            val lengthFile = lengthFileResource.toURI().toPath()

            simpleBackend = CokoCpgBackend(config = createCpgConfiguration(simpleFile))
            lengthBackend = CokoCpgBackend(config = createCpgConfiguration(lengthFile))
        }
    }
}
