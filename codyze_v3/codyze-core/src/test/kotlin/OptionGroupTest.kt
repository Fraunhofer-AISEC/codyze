package de.fraunhofer.aisec.codyze_core

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import de.fraunhofer.aisec.codyze_core.config.options.CodyzeOptions
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

        val source = cli.codyzeOptions.source.map { p -> p.normalize().absolute().toString() }

        assertEquals(expectedSource.size, source.size, "Size was not equal")
        for(es in expectedSource.map { p -> p.normalize().absolute().toString() }) {
            assertContains(source, es)
        }

    }


    companion object {
        lateinit var testDir1: Path
        lateinit var testDir2: Path
        lateinit var testFile1: Path

        @BeforeAll
        @JvmStatic
        fun startup() {
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
        }

        @JvmStatic
        fun sourceParamHelper(): Stream<Arguments> {
            return Stream.of(
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
                ),
                Arguments.of(
                    arrayOf(
                        "-s", testDir1.toString(),
                        "-s", Path(
                            testDir1.toString(),
                            testDir1.relativize(Path(System.getProperty("user.dir"))).toString(),
                            Path(System.getProperty("user.dir")).relativize(testDir1).toString(),
                            "dir1file1.java").toString(),
                        "--spec", testDir1.toString()
                    ),
                    listOf(
                        Path(testDir1.toString(),"dir1file1.java")
                    )
                )
            )
        }
    }
}