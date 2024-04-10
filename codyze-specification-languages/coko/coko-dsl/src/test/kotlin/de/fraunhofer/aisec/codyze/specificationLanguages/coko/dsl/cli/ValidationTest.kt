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
package de.fraunhofer.aisec.codyze.specificationLanguages.coko.dsl.cli

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.toPath
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ValidationTest {

    @Test
    fun `test fileNameString`() {
        val fileName = "test.txt"
        val path = Path(fileName)
        assertEquals(fileName, path.fileNameString)
    }

    @Test
    fun `test validateSpec with directory`() {
        val exception: Exception =
            Assertions.assertThrows(IllegalArgumentException::class.java) {
                validateSpec(listOf(testDir3Spec))
            }

        val expectedMessage = "All given spec paths must be files."
        val actualMessage = exception.message.orEmpty()

        assertContains(actualMessage, expectedMessage)
    }

    companion object {
        private lateinit var testDir3Spec: Path

        @BeforeAll
        @JvmStatic
        fun startup() {
            val testDir3SpecResource =
                CokoOptionGroupTest::class
                    .java
                    .classLoader
                    .getResource("cli-test-directory/dir3-spec")
            assertNotNull(testDir3SpecResource)
            testDir3Spec = testDir3SpecResource.toURI().toPath()
        }
    }
}
