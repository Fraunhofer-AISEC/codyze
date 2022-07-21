package de.fraunhofer.aisec.codyze

import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import de.fraunhofer.aisec.codyze.options.CPGOptions
import de.fraunhofer.aisec.codyze.options.CodyzeOptions
import de.fraunhofer.aisec.codyze.options.TranslationOptions
import de.fraunhofer.aisec.codyze.options.combineSources
import de.fraunhofer.aisec.codyze_core.Executor
import de.fraunhofer.aisec.cpg.passes.CallResolver
import de.fraunhofer.aisec.cpg.passes.EdgeCachePass
import de.fraunhofer.aisec.cpg.passes.FilenameMapper
import de.fraunhofer.aisec.cpg.passes.Pass
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Stream
import kotlin.io.path.*
import kotlin.streams.asSequence
import kotlin.test.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.koin.core.context.startKoin

class OptionGroupTest {
    class CodyzeOptionsCommand : CliktCommand() {
        val codyzeOptions by CodyzeOptions()
        override fun run() {}
    }

    class CPGOptionsCommand : CliktCommand() {
        val cpgOptions by CPGOptions()
        override fun run() {}
    }

    @ParameterizedTest
    @MethodSource("sourceParamHelper")
    fun resolveSourceTest(argv: Array<String>, expectedSource: List<Path>) {
        val cli = CodyzeOptionsCommand()
        cli.parse(argv)

        val mappedSource =
            cli.codyzeOptions.source
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

        val mapppedCombinedSource =
            combinedSource.map { p -> p.normalize().absolute().toString() }.sorted()
        val mappedExpectedSource =
            expectedSource.map { p -> p.normalize().absolute().toString() }.sorted()

        assertContentEquals(
            mappedExpectedSource.toTypedArray(),
            mapppedCombinedSource.toTypedArray()
        )
    }

    /** Test that all available executors are available as choices */
    @Test
    fun executorOptionTest() {
        val argv: Array<String> =
            arrayOf(
                "--source", // source is a required option
                testDir1.toString(),
                "--spec",
                testDir1.toString(),
                "--executor",
                "testExecutor" // invalid choice
            )
        val cli = CodyzeOptionsCommand()

        val exception: Exception =
            Assertions.assertThrows(BadParameterValue::class.java) { cli.parse(argv) }

        val expectedMessage =
            "Invalid value for \"--executor\": invalid choice: testExecutor. (choose from MarkExecutor)"
        val actualMessage = exception.message

        assertTrue(actualMessage!!.contains(expectedMessage))
    }

    /** Test that executor choices are cast correctly */
    @Test
    fun executorOptionCastTest() {
        val argv: Array<String> =
            arrayOf(
                "--source", // source is a required option
                testDir1.toString(),
                "--spec",
                testDir1.toString(),
                "--executor",
                "MarkExecutor" // valid choice
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
            arrayOf(
                "--source", // source is a required option
                testDir1.toString(),
                "--spec",
                testDir3Spec.div("same-file-extension").toString()
            )
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

    /**
     * Test that the spec files must have the same file extensions and if not an exception is
     * thrown.
     */
    @Test
    fun mixedSpecTest() {
        val argv: Array<String> =
            arrayOf(
                "--source",
                testDir1.toString(),
                "--spec",
                testDir3Spec.div("mixed-file-extension").toString()
            )
        val cli = CodyzeOptionsCommand()

        val exception: Exception =
            Assertions.assertThrows(BadParameterValue::class.java) { cli.parse(argv) }

        val expectedMessage =
            "Invalid value for \"--spec\": All given specification files must be of the same file type"
        val actualMessage = exception.message

        assertTrue(actualMessage!!.contains(expectedMessage))
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
        private lateinit var testDir3Spec: Path
        private lateinit var testFile1: Path

        private lateinit var allFiles: List<Path>
        private val workingDir: Path = Path(System.getProperty("user.dir"))

        @BeforeAll
        @JvmStatic
        fun startup() {
            // starting koin is necessary because some options (e.g., --executor)
            // dynamically look up available choices for the by options(...).choice() command
            startKoin { // Initialize the koin dependency injection
                // declare modules necessary for testing
                modules(executorModule)
            }

            val topTestDirResource =
                OptionGroupTest::class.java.classLoader.getResource("cli-test-directory")
            assertNotNull(topTestDirResource)
            topTestDir = Path(topTestDirResource.path)
            assertTrue(topTestDir.exists())

            val testDir1Resource =
                OptionGroupTest::class.java.classLoader.getResource("cli-test-directory/dir1")
            assertNotNull(testDir1Resource)
            testDir1 = Path(testDir1Resource.path)
            assertTrue(testDir1.exists())

            val testDir2Resource =
                OptionGroupTest::class.java.classLoader.getResource("cli-test-directory/dir2")
            assertNotNull(testDir2Resource)
            testDir2 = Path(testDir2Resource.path)
            assertTrue(testDir2.exists())

            val testDir3SpecResource =
                OptionGroupTest::class.java.classLoader.getResource("cli-test-directory/dir3-spec")
            assertNotNull(testDir3SpecResource)
            testDir3Spec = Path(testDir3SpecResource.path)
            assertTrue(testDir3Spec.exists())

            val testFile1Resource =
                OptionGroupTest::class.java.classLoader.getResource("cli-test-directory/file1.java")
            assertNotNull(testFile1Resource)
            testFile1 = Path(testFile1Resource.path)
            assertTrue(testFile1.exists())

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
                        "--spec",
                        testDir1.toString()
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
                        "--spec",
                        testDir1.toString()
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

            val translationOptionName = TranslationOptions::class.qualifiedName
            assertNotNull(translationOptionName)

            return Stream.of(
                Arguments.of(arrayOf("--passes", passName)),
                Arguments.of(arrayOf("--passes", "my.passes.MyPass")),
                Arguments.of(arrayOf("--passes", translationOptionName))
            )
        }
    }
}
