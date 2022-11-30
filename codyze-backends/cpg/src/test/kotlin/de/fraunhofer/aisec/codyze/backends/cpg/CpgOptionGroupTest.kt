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

import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import de.fraunhofer.aisec.codyze_core.config.ConfigurationRegister
import de.fraunhofer.aisec.codyze_core.config.combineSources
import de.fraunhofer.aisec.cpg.passes.CallResolver
import de.fraunhofer.aisec.cpg.passes.EdgeCachePass
import de.fraunhofer.aisec.cpg.passes.FilenameMapper
import de.fraunhofer.aisec.cpg.passes.Pass
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Stream
import kotlin.io.path.Path
import kotlin.io.path.absolute
import kotlin.io.path.div
import kotlin.io.path.isRegularFile
import kotlin.streams.asSequence
import kotlin.test.*

class CpgOptionGroupTest {

    class CPGOptionsCommand : CliktCommand() {
        private val configurationRegister = ConfigurationRegister()

        val cpgOptions by CPGOptionGroup(configurationRegister)
        override fun run() {}
    }

    @ParameterizedTest
    @MethodSource("sourceParamHelper")
    fun resolveSourceTest(argv: Array<String>, expectedSource: List<Path>) {
        val cli = CPGOptionsCommand()
        cli.parse(argv)

        val mappedSource =
            cli.cpgOptions.source
                .map { p -> p.toString() }
                .sorted() // the source option should only contain already normalized absolute paths
        val mappedExpectedSource =
            expectedSource.map { p -> p.normalize().absolute().toString() }.sorted()
        assertContentEquals(
            expected = mappedExpectedSource.toTypedArray(),
            actual = mappedSource.toTypedArray()
        )
    }

    @ParameterizedTest
    @MethodSource("combineSourcesHelper")
    fun combineSourcesTest(expectedSource: List<Path>, vararg sources: List<Path>) {
        val combinedSource = combineSources(*sources)

        val mappedCombinedSource =
            combinedSource.map { p -> p.normalize().absolute().toString() }.sorted()
        val mappedExpectedSource =
            expectedSource.map { p -> p.normalize().absolute().toString() }.sorted()

        assertContentEquals(
            mappedExpectedSource.toTypedArray(),
            mappedCombinedSource.toTypedArray()
        )
    }

    @Test
    fun passesTest() {
        val edgeCachePassName = EdgeCachePass::class.qualifiedName
        val filenameMapperName = FilenameMapper::class.qualifiedName
        val callResolverName = CallResolver::class.qualifiedName
        assertNotNull(edgeCachePassName)
        assertNotNull(filenameMapperName)
        assertNotNull(callResolverName)

        val cli = CPGOptionsCommand()
        cli.parse(
            arrayOf(
                "--source",
                testDir1.toString(),
                "--passes",
                edgeCachePassName,
                "--passes",
                filenameMapperName,
                "--passes",
                callResolverName
            )
        )

        val expectedPassesNames =
            listOf(EdgeCachePass(), FilenameMapper(), CallResolver()).map { p ->
                p::class.qualifiedName
            }
        val actualPassesNames = cli.cpgOptions.passes.map { p -> p::class.qualifiedName }

        println(actualPassesNames.joinToString(","))

        assertContentEquals(expectedPassesNames, actualPassesNames)
    }

    @ParameterizedTest
    @MethodSource("incorrectPassesHelper")
    fun incorrectPassesTest(argv: Array<String>) {
        val cli = CPGOptionsCommand()
        assertThrows<BadParameterValue> { cli.parse(argv) }
    }

    companion object {
        lateinit var topTestDir: Path
        private lateinit var testDir1: Path
        private lateinit var testDir2: Path
        private lateinit var testFile1: Path

        private lateinit var allFiles: List<Path>
        private val workingDir: Path = Path(System.getProperty("user.dir"))

        @BeforeAll
        @JvmStatic
        fun startup() {
            val topTestDirResource =
                CpgOptionGroupTest::class.java.classLoader.getResource("cli-test-directory")
            assertNotNull(topTestDirResource)
            topTestDir = Path(topTestDirResource.path)
            assertNotNull(topTestDir) // TODO: why is this necessary

            val testDir1Resource =
                CpgOptionGroupTest::class.java.classLoader.getResource("cli-test-directory/dir1")
            assertNotNull(testDir1Resource)
            testDir1 = Path(testDir1Resource.path)
            assertNotNull(testDir1)

            val testDir2Resource =
                CpgOptionGroupTest::class.java.classLoader.getResource("cli-test-directory/dir2")
            assertNotNull(testDir2Resource)
            testDir2 = Path(testDir2Resource.path)
            assertNotNull(testDir2)

            val testFile1Resource =
                CpgOptionGroupTest::class
                    .java
                    .classLoader
                    .getResource("cli-test-directory/file1.java")
            assertNotNull(testFile1Resource)
            testFile1 = Path(testFile1Resource.path)
            assertNotNull(testFile1)

            allFiles = Files.walk(topTestDir).asSequence().filter { it.isRegularFile() }.toList()
        }

        @JvmStatic
        fun sourceParamHelper(): Stream<Arguments> {
            return Stream.of(
                // Tests if source and source-additions are combined correctly
                Arguments.of(
                    arrayOf(
                        "--source",
                        testDir1.toString(),
                        "-s",
                        testFile1.toString(),
                        "--source-additions",
                        testDir2.toString(),
                    ),
                    listOf(
                        testFile1,
                        testDir1.div("dir1file1.java"),
                        testDir2.div("dir2dir1").div("dir2dir1file1.java"),
                        testDir2.div("dir2dir1").div("dir2dir1file2.java"),
                        testDir2.div("dir2dir2").div("dir2dir2file1.java"),
                        testDir2.div("dir2dir3").div("dir2dir3file1.java"),
                        testDir2.div("dir2file1.java"),
                        testDir2.div("dir2file2.java")
                    )
                ),
                // Tests if disabled-source files are correctly removed from source
                Arguments.of(
                    arrayOf(
                        "--source",
                        testDir1.toString(),
                        "--source-additions",
                        testDir2.toString(),
                        "--disabled-source",
                        testDir2.div("dir2dir1").div("dir2dir1file2.java").toString(),
                        "--disabled-source-additions",
                        testDir2.div("dir2dir2").toString(),
                    ),
                    listOf(
                        testDir1.div("dir1file1.java"),
                        testDir2.div("dir2dir1").div("dir2dir1file1.java"),
                        testDir2.div("dir2dir3").div("dir2dir3file1.java"),
                        testDir2.div("dir2file1.java"),
                        testDir2.div("dir2file2.java")
                    )
                )
            )
        }

        @JvmStatic
        fun combineSourcesHelper(): Stream<Arguments> {
            return Stream.of(
                // Tests if duplicates are filtered out
                Arguments.of(
                    allFiles,
                    arrayOf(
                        listOf(testFile1, testDir1.div("dir1file1.java")),
                        listOf(topTestDir, testFile1),
                        listOf(testDir1),
                        listOf(testDir2, topTestDir, testDir2.div("dir2dir1"))
                    )
                ),
                // Tests if normalization works to filter out duplicates
                Arguments.of(
                    listOf(testFile1),
                    arrayOf(
                        listOf(
                            Path(
                                testDir1.toString(),
                                testDir1.relativize(workingDir).toString(),
                                workingDir.relativize(testDir1).toString(),
                                testDir1.relativize(topTestDir).toString(),
                                topTestDir.relativize(testFile1).toString()
                            ),
                            testFile1
                        )
                    )
                ),
                // test is relative paths are resolved correctly
                Arguments.of(
                    listOf(testDir1.div("dir1file1.java")),
                    arrayOf(listOf(workingDir.relativize(testDir1), testDir1))
                )
            )
        }

        @JvmStatic
        fun incorrectPassesHelper(): Stream<Arguments> {
            val passName = Pass::class.qualifiedName
            assertNotNull(passName)

            val translationOptionName = CPGOptionGroup::class.qualifiedName
            assertNotNull(translationOptionName)

            return Stream.of(
                Arguments.of(arrayOf("--passes", passName)),
                Arguments.of(arrayOf("--passes", "my.passes.MyPass")),
                Arguments.of(arrayOf("--passes", translationOptionName))
            )
        }
    }
}
