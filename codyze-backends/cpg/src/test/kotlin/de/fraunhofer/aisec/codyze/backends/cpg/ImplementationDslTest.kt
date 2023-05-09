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

import de.fraunhofer.aisec.codyze.backends.cpg.coko.CokoCpgBackend
import de.fraunhofer.aisec.codyze.backends.cpg.coko.dsl.cpgGetAllNodes
import de.fraunhofer.aisec.codyze.backends.cpg.coko.dsl.cpgGetNodes
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.Wildcard
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.op
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.signature
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import kotlin.io.path.Path
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
        with(backend) {
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
        with(backend) {
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
        with(backend) {
            val nodes = op.cpgGetNodes()
            assertEquals(
                3,
                nodes.size,
                "cpgGetNodes returned ${nodes.size} node(s) instead of 3 nodes for the Op: $op."
            )
        }
    }

    companion object {

        lateinit var backend: CokoCpgBackend

        @BeforeAll
        @JvmStatic
        fun startup() {
            val classLoader = ImplementationDslTest::class.java.classLoader

            val testFileResource = classLoader.getResource("ImplementationDslTest/SimpleJavaFile.java")
            assertNotNull(testFileResource)
            val testFile = Path(testFileResource.path)
            backend = CokoCpgBackend(config = createCpgConfiguration(testFile))
        }
    }
}
