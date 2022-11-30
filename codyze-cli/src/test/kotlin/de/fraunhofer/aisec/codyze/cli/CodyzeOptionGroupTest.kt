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
package de.fraunhofer.aisec.codyze.cli

import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import de.fraunhofer.aisec.codyze.cli.options.CodyzeOptionGroup
import de.fraunhofer.aisec.codyze.core.Executor
import de.fraunhofer.aisec.codyze.core.config.ConfigurationRegister
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.test.KoinTest
import org.koin.test.junit5.KoinTestExtension
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.div
import kotlin.io.path.isRegularFile
import kotlin.streams.asSequence
import kotlin.test.*

class CodyzeOptionGroupTest : KoinTest {

    // starting koin is necessary because some options (e.g., --executor)
    // dynamically look up available choices for the by options(...).choice() command
    @JvmField
    @RegisterExtension
    val koinTestExtension =
        KoinTestExtension.create { // Initialize the koin dependency injection
            // declare modules necessary for testing
            modules(executorModule)
        }

    class CodyzeOptionsCommand : CliktCommand() {
        val configurationRegister = ConfigurationRegister()
        val codyzeOptions by CodyzeOptionGroup(configurationRegister)
        override fun run() {}
    }

    /** Test that all available executors are available as choices */
    @Test
    fun executorOptionTest() {
        val argv: Array<String> =
            arrayOf(
                "--spec",
                testDir1.toString(),
                "--executor",
                "testExecutor" // invalid choice
            )
        val cli = CodyzeOptionsCommand()

        val exception: Exception =
            Assertions.assertThrows(BadParameterValue::class.java) { cli.parse(argv) }

        val expectedMessage =
            "Invalid value for \"--executor\": invalid choice: testExecutor. (choose from "
        val actualMessage = exception.message

        assertTrue(actualMessage!!.contains(expectedMessage))
    }

    /** Test that executor choices are cast correctly */
    @Test
    fun executorOptionCastTest() {
        val argv: Array<String> =
            arrayOf(
                "--spec",
                testDir1.toString(),
                "--executor",
                "CokoExecutor" // valid choice
            )
        val cli = CodyzeOptionsCommand()
        cli.parse(argv)

        assertTrue(cli.codyzeOptions.executor is Executor)
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
        val cli = CodyzeOptionsCommand()
        cli.parse(argv)

        val mappedSpec = cli.codyzeOptions.spec.map { it.toString() }.sorted()
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

    // Disabled for now  because spec file type is not validated by Codyze anymore. The Executors
    // must filter
    // the received files by file type
    //    /**
    //     * Test that the spec files must have the same file extensions and if not an exception is
    //     * thrown.
    //     */
    //    @Test
    //    fun mixedSpecTest() {
    //        val argv: Array<String> =
    //            arrayOf(
    //                "--source",
    //                testDir1.toString(),
    //                "--spec",
    //                testDir3Spec.div("mixed-file-extension").toString()
    //            )
    //        val cli = CodyzeOptionsCommand()
    //
    //        val exception: Exception =
    //            Assertions.assertThrows(BadParameterValue::class.java) { cli.parse(argv) }
    //
    //        val expectedMessage = "Invalid value for \"--spec\":
    // ${localization.invalidSpecFileType()}"
    //        val actualMessage = exception.message
    //
    //        assertTrue(actualMessage!!.contains(expectedMessage))
    //    }

    companion object {
        lateinit var topTestDir: Path
        private lateinit var testDir1: Path
        private lateinit var testDir3Spec: Path

        private lateinit var allFiles: List<Path>

        @BeforeAll
        @JvmStatic
        fun startup() {
            val topTestDirResource =
                CodyzeOptionGroupTest::class.java.classLoader.getResource("cli-test-directory")
            assertNotNull(topTestDirResource)
            topTestDir = Path(topTestDirResource.path)
            assertNotNull(topTestDir) // TODO: why is this necessary

            val testDir1Resource =
                CodyzeOptionGroupTest::class.java.classLoader.getResource("cli-test-directory/dir1")
            assertNotNull(testDir1Resource)
            testDir1 = Path(testDir1Resource.path)
            assertNotNull(testDir1)

            val testDir3SpecResource =
                CodyzeOptionGroupTest::class
                    .java
                    .classLoader
                    .getResource("cli-test-directory/dir3-spec")
            assertNotNull(testDir3SpecResource)
            testDir3Spec = Path(testDir3SpecResource.path)

            allFiles = Files.walk(topTestDir).asSequence().filter { it.isRegularFile() }.toList()
        }
    }
}
