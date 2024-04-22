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
package de.fraunhofer.aisec.codyze.specificationLanguages.coko.dsl

import de.fraunhofer.aisec.codyze.specificationLanguages.coko.dsl.host.CokoExecutor
import io.mockk.mockk
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.writeText
import kotlin.script.experimental.api.valueOrThrow
import kotlin.test.Test

/** Tests whether the basic functionality of [CokoScript] works. */
class CokoScriptHostTest {

    @Test
    fun `test basic type creation`() {
        assertDoesNotThrow {
            CokoExecutor.eval(
                """
                interface TestInterface {
                    fun log(message: String)
                }
                """.trimIndent(),
                mockk()
            )
                .valueOrThrow()
        }
    }

    @Test
    fun `test default imports`() {
        assertDoesNotThrow {
            CokoExecutor.eval(
                """
                // Wildcard is a default import
                interface TestInterface {
                    fun log(message: String) = Wildcard
                }
                """.trimIndent(),
                mockk()
            )
                .valueOrThrow()
        }
    }

    @Test
    fun `test implicit receivers`() {
        assertDoesNotThrow {
            CokoExecutor.eval(
                """
                // op & definition is a method of an implicit receiver
                class TestImpl {
                    fun log(message: String) = 
                        op {
                            definition("") {}
                        }
                }
                """.trimIndent(),
                mockk()
            )
                .valueOrThrow()
        }
    }

    @Test
    fun `test import annotation`(@TempDir tempDir: Path) {
        val modelDefinitionFile = tempDir.resolveAbsoluteInvariant("model.codyze.kts")
        modelDefinitionFile.writeText(
            """
                interface TestConcept {
                    fun log(message: String)
                }
            """.trimIndent()
        )

        assertDoesNotThrow {
            CokoExecutor.eval(
                """
                    @file:Import("$modelDefinitionFile")

                    class TestImpl: TestConcept {
                        override fun log(message: String) { }
                    }
                """.trimIndent(),
                mockk(),
            )
                .valueOrThrow()
        }
    }
}
