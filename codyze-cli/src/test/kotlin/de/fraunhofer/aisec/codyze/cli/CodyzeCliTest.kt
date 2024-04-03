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

import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.path
import de.fraunhofer.aisec.codyze.core.backend.Backend
import de.fraunhofer.aisec.codyze.core.config.combineSources
import de.fraunhofer.aisec.codyze.core.executor.Executor
import de.fraunhofer.aisec.codyze.core.executor.ExecutorCommand
import io.mockk.mockk
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.test.KoinTest
import org.koin.test.junit5.KoinTestExtension
import java.nio.file.Path
import kotlin.io.path.*
import kotlin.test.*

class CodyzeCliTest : KoinTest {

    // starting koin is necessary because some options (e.g., --executor)
    // dynamically look up available choices for the by options(...).choice() command
    @JvmField
    @RegisterExtension
    val koinTestExtension =
        KoinTestExtension.create { // Initialize the koin dependency injection
            // declare modules necessary for testing
            modules(outputBuilders)
        }

    class TestExecutorSubcommand : ExecutorCommand<Executor>() {
        private val rawSpec: List<Path> by option("--spec")
            .path(mustExist = true, mustBeReadable = true, canBeDir = true)
            .multiple(required = true)

        val spec by lazy { combineSources(rawSpec) }

        override fun getExecutor(
            goodFindings: Boolean,
            pedantic: Boolean,
            backend: Backend?
        ) = mockk<Executor>(relaxed = true)
    }

    // Test that relative paths are resolved relative to the config file
    @Test
    fun configRelativePathResolutionTest() {
        val testSubcommand = TestExecutorSubcommand()
        CodyzeCli(pathConfigFile).subcommands(testSubcommand).parse(arrayOf(testSubcommand.commandName))

        val expectedSpecs = listOf(specMark)
        assertContentEquals(expectedSpecs, testSubcommand.spec)
    }

    // Test the behavior of command if both config file and command line options are present
    @Test
    fun configFileWithArgsTest() {
        val tempDir = createTempDirectory()

        val testSubcommand = TestExecutorSubcommand()
        val codyzeCli = CodyzeCli(correctConfigFile).subcommands(testSubcommand)
        codyzeCli.parse(
            arrayOf(
                "--output",
                tempDir.absolutePathString(),
                "--pedantic",
                testSubcommand.commandName,
                "--spec",
                spec2Mark.absolutePathString(),
            )
        )

        // should be overwritten by args
        val overwrittenMessage = "CLI options should take precedence over config file"
        assertEquals(tempDir.absolute(), codyzeCli.codyzeOptions.output.absolute(), overwrittenMessage)
        assertEquals(true, codyzeCli.codyzeOptions.pedantic, overwrittenMessage)
        assertContentEquals(listOf(spec2Mark), testSubcommand.spec.map { it.absolute() }, overwrittenMessage)

        // should be config values
        val staySameMessage =
            "Config file options should stay the same if it was not matched on CLI"
        assertFalse(codyzeCli.codyzeOptions.goodFindings, staySameMessage)
    }

    companion object {
        lateinit var pathConfigFile: Path
        lateinit var correctConfigFile: Path

        lateinit var specMark: Path
        lateinit var spec2Mark: Path

        @BeforeAll
        @JvmStatic
        fun startup() {
            val pathConfigFileResource =
                CodyzeCliTest::class.java.classLoader.getResource("config-files/path-config.json")
            assertNotNull(pathConfigFileResource)
            pathConfigFile = pathConfigFileResource.toURI().toPath()
            assertTrue(pathConfigFile.exists())

            val correctConfigFileResource =
                CodyzeCliTest::class
                    .java
                    .classLoader
                    .getResource("config-files/correct-config.json")
            assertNotNull(correctConfigFileResource)
            correctConfigFile = correctConfigFileResource.toURI().toPath()
            assertTrue(correctConfigFile.exists())

            val specMarkResource =
                CodyzeCliTest::class.java.classLoader.getResource("config-files/spec/spec.mark")
            assertNotNull(specMarkResource)
            specMark = specMarkResource.toURI().toPath()
            assertTrue(specMark.exists())

            val specMark2Resource =
                CodyzeCliTest::class.java.classLoader.getResource("config-files/spec2.mark")
            assertNotNull(specMark2Resource)
            spec2Mark = specMark2Resource.toURI().toPath()
            assertTrue(spec2Mark.exists())
        }
    }
}
