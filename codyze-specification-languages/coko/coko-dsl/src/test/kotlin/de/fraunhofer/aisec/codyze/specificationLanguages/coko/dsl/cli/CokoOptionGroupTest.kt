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

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.junit5.KoinTestExtension
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.div
import kotlin.io.path.isRegularFile
import kotlin.streams.asSequence
import kotlin.test.*

class CokoOptionGroupTest : KoinTest {

    // starting koin is necessary because some options (e.g., --executor)
    // dynamically look up available choices for the by options(...).choice() command
    @JvmField
    @RegisterExtension
    val koinTestExtension =
        KoinTestExtension.create { // Initialize the koin dependency injection
            // declare modules necessary for testing
            modules(module { })
        }

    /**
     * Test that all specs are combined correctly.
     *
     * This is not tested as thoroughly as with sources because it uses the same code internally.
     */
    @Test
    fun combineSpecTest() {
        val argv: Array<String> =
            arrayOf("--spec", testDir3Spec.div("same-file-extension").toString())
        val cli = CokoSubcommand()
        cli.parse(argv)

        val mappedSpec = cli.executorOptions.spec.map { it.toString() }.sorted()
        val expectedSpec =
            Files.walk(testDir3Spec.div("same-file-extension"))
                .asSequence()
                .filter { it.isRegularFile() }
                .toList()
                .map { it.toString() }
                .sorted()

        assertContentEquals(
            actual = mappedSpec.toTypedArray(),
            expected = expectedSpec.toTypedArray()
        )
    }

    /**
     * Test that the spec files must have the same file extensions and if not an exception is
     * thrown.
     */
    @Test
    fun mixedSpecTest() {
        val argv: Array<String> =
            arrayOf(
                "--spec",
                testDir3Spec.div("mixed-file-extension").toString()
            )
        val cli = CokoSubcommand()
        cli.parse(argv)

        val exception: Exception =
            Assertions.assertThrows(IllegalArgumentException::class.java) { cli.executorOptions.spec }

        val expectedMessage = "All given specification files must be coko specification files (*.codyze.kts)."
        val actualMessage = exception.message

        assertTrue(actualMessage!!.contains(expectedMessage))
    }

    companion object {
        lateinit var topTestDir: Path
        private lateinit var testDir3Spec: Path

        private lateinit var allFiles: List<Path>

        @BeforeAll
        @JvmStatic
        fun startup() {
            val topTestDirResource =
                CokoOptionGroupTest::class.java.classLoader.getResource("cli-test-directory")
            assertNotNull(topTestDirResource)
            topTestDir = Path(topTestDirResource.path)

            val testDir3SpecResource =
                CokoOptionGroupTest::class
                    .java
                    .classLoader
                    .getResource("cli-test-directory/dir3-spec")
            assertNotNull(testDir3SpecResource)
            testDir3Spec = Path(testDir3SpecResource.path)

            allFiles = Files.walk(topTestDir).asSequence().filter { it.isRegularFile() }.toList()
        }
    }
}
