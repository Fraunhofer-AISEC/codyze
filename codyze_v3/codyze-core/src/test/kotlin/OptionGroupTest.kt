package de.fraunhofer.aisec.codyze_core

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import de.fraunhofer.aisec.codyze_core.config.options.CodyzeOptions
import de.fraunhofer.aisec.codyze_core.config.options.combineSources
import kotlin.test.*
import org.junit.jupiter.api.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.nio.file.Path
import java.util.stream.Stream
import kotlin.io.path.Path
import kotlin.io.path.absolute

class OptionGroupTest {
    class TestCommand : CliktCommand() {
        val codyzeOptions by CodyzeOptions()
        override fun run() { }
    }

    @ParameterizedTest
    @MethodSource("sourceParamHelper")
    fun resolveSourceTest(argv: Array<String>, expectedSource: List<Path>) {
        val cli = TestCommand()
        cli.parse(argv)

        val mappedSource = cli.codyzeOptions.source.map { p -> p.normalize().absolute().toString() }
        val mappedExpectedSource = expectedSource.map {  p -> p.normalize().absolute().toString() }
        assertUnsortedListEquality(mappedExpectedSource, mappedSource)
    }


    @ParameterizedTest
    @MethodSource("combineSourcesHelper")
    fun combineSourcesTest(expectedSource: List<Path>, vararg sources: List<Path>) {
        val combinedSource = combineSources(*sources)

        val mapppedCombinedSource = combinedSource.map { p -> p.normalize().absolute().toString() }
        val mappedExpectedSource = expectedSource.map { p -> p.normalize().absolute().toString() }

        assertUnsortedListEquality(mappedExpectedSource, mapppedCombinedSource)
    }

    private fun assertUnsortedListEquality(expected: List<Any>, actual: List<Any>){
        assertEquals(expected.size, actual.size, "Size was not equal")
        for(es in expected) {
            assertContains(actual, es)
        }
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
                OptionGroupTest::class
                    .java.classLoader.getResource("cli-test-directory")
            assertNotNull(topTestDirResource)
            topTestDir = Path(topTestDirResource.path)
            assertNotNull(topTestDir)

            val testDir1Resource =
                OptionGroupTest::class
                    .java.classLoader.getResource("cli-test-directory/dir1")
            assertNotNull(testDir1Resource)
            testDir1 = Path(testDir1Resource.path)
            assertNotNull(testDir1)

            val testDir2Resource =
                OptionGroupTest::class
                    .java.classLoader.getResource("cli-test-directory/dir2")
            assertNotNull(testDir2Resource)
            testDir2 = Path(testDir2Resource.path)
            assertNotNull(testDir2)

            val testFile1Resource =
                OptionGroupTest::class
                    .java.classLoader.getResource("cli-test-directory/file1.java")
            assertNotNull(testFile1Resource)
            testFile1 = Path(testFile1Resource.path)
            assertNotNull(testFile1)

            allFiles =  listOf(
                testFile1,
                Path(topTestDir.toString(), "file2.java"),
                Path(testDir1.toString(),"dir1file1.java"),
                Path(testDir2.toString(),"dir2dir1","dir2dir1file1.java"),
                Path(testDir2.toString(),"dir2dir1","dir2dir1file2.java"),
                Path(testDir2.toString(),"dir2dir2","dir2dir2file1.java"),
                Path(testDir2.toString(),"dir2dir3","dir2dir3file1.java"),
                Path(testDir2.toString(),"dir2file1.java"),
                Path(testDir2.toString(),"dir2file2.java")
            )
        }

        @JvmStatic
        fun sourceParamHelper(): Stream<Arguments> {
            return Stream.of(
                // Tests if source and source-additions are combined correctly
                Arguments.of(
                    arrayOf(
                        "--source", testDir1.toString(),
                        "-s", testFile1.toString(),
                        "--source-additions", testDir2.toString(),
                        "--spec", testDir1.toString()
                    ),
                    listOf(
                        testFile1,
                        Path(testDir1.toString(),"dir1file1.java"),
                        Path(testDir2.toString(),"dir2dir1","dir2dir1file1.java"),
                        Path(testDir2.toString(),"dir2dir1","dir2dir1file2.java"),
                        Path(testDir2.toString(),"dir2dir2","dir2dir2file1.java"),
                        Path(testDir2.toString(),"dir2dir3","dir2dir3file1.java"),
                        Path(testDir2.toString(),"dir2file1.java"),
                        Path(testDir2.toString(),"dir2file2.java")
                    )
                ),
                // Tests if disabled-source files are correctly removed from source
                Arguments.of(
                    arrayOf(
                        "--source", testDir1.toString(),
                        "--source-additions", testDir2.toString(),
                        "--disabled-source", Path(testDir2.toString(),"dir2dir1","dir2dir1file2.java").toString(),
                        "--disabled-source-additions", Path(testDir2.toString(),"dir2dir2").toString(),
                        "--spec", testDir1.toString()
                    ),
                    listOf(
                        Path(testDir1.toString(),"dir1file1.java"),
                        Path(testDir2.toString(),"dir2dir1","dir2dir1file1.java"),
                        Path(testDir2.toString(),"dir2dir3","dir2dir3file1.java"),
                        Path(testDir2.toString(),"dir2file1.java"),
                        Path(testDir2.toString(),"dir2file2.java")
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
                        listOf(
                            testFile1,
                            Path(testDir1.toString(),"dir1file1.java")),
                        listOf(
                            topTestDir,
                            testFile1
                        ),
                        listOf(
                            testDir1
                        ),
                        listOf(
                            testDir2,
                            topTestDir,
                            Path(testDir2.toString(), "dir2dir1")
                        )
                    )
                ),
                // Tests if normalization works to filter out duplicates
                Arguments.of(
                    listOf(
                        testFile1
                    ),
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
                    listOf(
                        Path(testDir1.toString(), "dir1file1.java")
                    ),
                    arrayOf(
                        listOf(
                            workingDir.relativize(testDir1),
                            testDir1
                        )
                    )

                )

            )
        }
    }
}