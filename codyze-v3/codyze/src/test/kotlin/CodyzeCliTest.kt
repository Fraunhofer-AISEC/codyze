package de.fraunhofer.aisec.codyze

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import de.fraunhofer.aisec.codyze_core.config.Language
import de.fraunhofer.aisec.codyze_core.config.TypestateMode
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.div
import kotlin.io.path.exists
import kotlin.test.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.test.KoinTest
import org.koin.test.junit5.KoinTestExtension

class CodyzeCliTest : KoinTest {

    @JvmField
    @RegisterExtension
    // starting koin is necessary because some options (e.g., --executor)
    // dynamically look up available choices for the by options(...).choice() command
    val koinTestExtension =
        KoinTestExtension.create { // Initialize the koin dependency injection
            // declare modules necessary for testing
            modules(executorModule)
        }

    class TestSubcommand : CodyzeSubcommand() {
        override fun run() {}
    }

    fun initCli(configFile: Path, subcommands: Array<CliktCommand>, argv: Array<String>) {
        val command = CodyzeCli(configFile).subcommands(*subcommands)
        command.parse(argv)
    }

    // Test that relative paths are resolved relative to the config file
    @Test
    fun configRelativePathResolutionTest() {
        val testSubcommand = TestSubcommand()
        val codyzeCli =
            initCli(pathConfigFile, arrayOf(testSubcommand), arrayOf(testSubcommand.commandName))

        val expectedSource = listOf<Path>(fileYml)
        assertContentEquals(expectedSource, testSubcommand.codyzeOptions.source)

        val expectedSpecs = listOf(specMark)
        assertContentEquals(expectedSpecs, testSubcommand.codyzeOptions.spec)
    }

    // Test the behavior of command if both config file and command line options are present
    @Test
    fun configFileWithArgsTest() {
        val testSubcommand = TestSubcommand()
        val codyzeCli =
            initCli(
                correctConfigFile,
                arrayOf(testSubcommand),
                arrayOf(
                    testSubcommand.commandName,
                    "-s",
                    srcMainJava.div("dir1").toString(),
                    "--additional-languages",
                    "python",
                    "--no-unity",
                    "--spec-additions",
                    spec2Mark.toString(),
                )
            )

        // should be overwritten by args
        val overwrittenMessage = "CLI options should take precedence over config file"
        val expectedSource = listOf(srcMainJava.div("dir1").div("File3.java"))
        assertContentEquals(expectedSource, testSubcommand.codyzeOptions.source, overwrittenMessage)
        assertFalse(testSubcommand.cpgOptions.useUnityBuild, overwrittenMessage)
        assertContentEquals(
            listOf(Language.PYTHON),
            testSubcommand.cpgOptions.additionalLanguages,
            overwrittenMessage
        )

        // args values should be appended to config values
        val appendMessage = "Values from CLI option should be appended to config values"
        val expectedSpecs = listOf(specMark, spec2Mark)
        assertContentEquals(expectedSpecs, testSubcommand.codyzeOptions.spec, appendMessage)

        // should be config values
        val staySameMessage =
            "Config file options should stay the same if it was not matched on CLI"
        assertFalse(testSubcommand.codyzeOptions.goodFindings, staySameMessage)
        assertEquals(TypestateMode.DFA, testSubcommand.analysisOptions.typestate, staySameMessage)
        assertEquals(5, testSubcommand.codyzeOptions.timeout, staySameMessage)
        assertTrue(testSubcommand.translationOptions.loadIncludes, staySameMessage)
    }

    companion object {
        lateinit var pathConfigFile: Path
        lateinit var correctConfigFile: Path

        lateinit var fileYml: Path
        lateinit var specMark: Path
        lateinit var spec2Mark: Path
        lateinit var srcMainJava: Path

        @BeforeAll
        @JvmStatic
        fun startup() {

            val pathConfigFileResource =
                CodyzeCliTest::class.java.classLoader.getResource("config-files/path-config.json")
            assertNotNull(pathConfigFileResource)
            pathConfigFile = Path(pathConfigFileResource.path)
            assertTrue(pathConfigFile.exists())

            val correctConfigFileResource =
                CodyzeCliTest::class
                    .java
                    .classLoader
                    .getResource("config-files/correct-config.json")
            assertNotNull(correctConfigFileResource)
            correctConfigFile = Path(correctConfigFileResource.path)
            assertTrue(correctConfigFile.exists())

            val fileYmlResource =
                CodyzeCliTest::class.java.classLoader.getResource("config-files/file.yml")
            assertNotNull(fileYmlResource)
            fileYml = Path(fileYmlResource.path)
            assertTrue(fileYml.exists())

            val specMarkResource =
                CodyzeCliTest::class.java.classLoader.getResource("config-files/spec/spec.mark")
            assertNotNull(specMarkResource)
            specMark = Path(specMarkResource.path)
            assertTrue(specMark.exists())

            val specMark2Resource =
                CodyzeCliTest::class.java.classLoader.getResource("config-files/spec2.mark")
            assertNotNull(specMark2Resource)
            spec2Mark = Path(specMark2Resource.path)
            assertTrue(spec2Mark.exists())

            val srcMainJavaResource =
                CodyzeCliTest::class.java.classLoader.getResource("config-files/src/main/java")
            assertNotNull(srcMainJavaResource)
            srcMainJava = Path(srcMainJavaResource.path)
            assertTrue(srcMainJava.exists())
        }
    }
}
