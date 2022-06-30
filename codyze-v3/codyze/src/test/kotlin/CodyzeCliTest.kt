package de.fraunhofer.aisec.codyze

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.MissingOption
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import de.fraunhofer.aisec.codyze.options.*
import de.fraunhofer.aisec.codyze_core.config.Language
import de.fraunhofer.aisec.codyze_core.config.TypestateMode
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.div
import kotlin.test.*

class CodyzeCliTest {

    class TestSubcommand : CliktCommand(name = "analyze") {
        val codyzeOptions by CodyzeOptions()
        val analysisOptions by AnalysisOptions()
        val cpgOptions by CPGOptions()
        val translationOptions by TranslationOptions()

        override fun run() {}
    }

    fun initCli(configFile: Path, subcommands: Array<CliktCommand>, argv: Array<String>): CodyzeCli {
        val command =  CodyzeCli(configFile).subcommands(*subcommands)
        command.parse(argv)
        return command
    }

    // Test that relative paths are resolved relative to the config file
    @Test
    fun configRelativePathResolutionTest() {
        val analyze = TestSubcommand()
        val codyzeCli = initCli(pathConfigFile, arrayOf(analyze), arrayOf("analyze"))

        val expectedSource = listOf<Path>(fileYml)
        assertContentEquals(expectedSource, analyze.codyzeOptions.source)

        val expectedSpecs = listOf(specMark)
        assertContentEquals(expectedSpecs, analyze.codyzeOptions.spec)

    }

    // Test the behavior of command if both config file and command line options are present
    @Test
    fun configFileWithArgsTest() {
        val analyze = TestSubcommand()
        val codyzeCli = initCli(
            correctConfigFile,
            arrayOf(analyze),
            arrayOf(
                "analyze",
                "-s", srcMainJava.div("dir1").toString(),
                "--additional-languages", "python",
                "--no-unity",
                "--spec-additions", spec2Mark.toString(),
            )
        )

        // should be overwritten by args
        val overwrittenMessage = "CLI options should take precedence over config file"
        val expectedSource = listOf(srcMainJava.div("dir1").div("File3.java"))
        assertContentEquals(expectedSource, analyze.codyzeOptions.source, overwrittenMessage)
        assertFalse(analyze.cpgOptions.useUnityBuild, overwrittenMessage)
        assertContentEquals(listOf(Language.PYTHON), analyze.cpgOptions.additionalLanguages, overwrittenMessage)

        // args values should be appended to config values
        val appendMessage = "Values from CLI option should be appended to config values"
        val expectedSpecs = listOf(
            specMark,
            spec2Mark
        )
        assertContentEquals(expectedSpecs, analyze.codyzeOptions.spec, appendMessage)

        // should be config values
        val staySameMessage = "Config file options should stay the same if it was not matched on CLI"
        assertFalse(analyze.codyzeOptions.goodFindings, staySameMessage)
        assertEquals(TypestateMode.DFA, analyze.analysisOptions.typestate, staySameMessage)
        assertEquals(5, analyze.codyzeOptions.timeout, staySameMessage)
        assertTrue(analyze.translationOptions.loadIncludes, staySameMessage)
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
            startKoin { // Initialize the koin dependency injection
                // use Koin logger
                printLogger()
                // declare modules
                modules(executorModule, subcommandModule)
            }

            val pathConfigFileResource =
                CodyzeCliTest::class.java.classLoader.getResource("config-files/path-config.json")
            assertNotNull(pathConfigFileResource)
            pathConfigFile = Path(pathConfigFileResource.path)
            assertNotNull(pathConfigFile)

            val correctConfigFileResource =
                CodyzeCliTest::class.java.classLoader.getResource("config-files/correct-config.json")
            assertNotNull(correctConfigFileResource)
            correctConfigFile = Path(correctConfigFileResource.path)
            assertNotNull(correctConfigFile)

            val fileYmlResource =
                CodyzeCliTest::class.java.classLoader.getResource("config-files/file.yml")
            assertNotNull(fileYmlResource)
            fileYml = Path(fileYmlResource.path)
            assertNotNull(fileYml)

            val specMarkResource =
                CodyzeCliTest::class.java.classLoader.getResource("config-files/spec/spec.mark")
            assertNotNull(specMarkResource)
            specMark = Path(specMarkResource.path)
            assertNotNull(specMark)

            val specMark2Resource =
                CodyzeCliTest::class.java.classLoader.getResource("config-files/spec2.mark")
            assertNotNull(specMark2Resource)
            spec2Mark = Path(specMark2Resource.path)
            assertNotNull(spec2Mark)

            val srcMainJavaResource =
                CodyzeCliTest::class.java.classLoader.getResource("config-files/src/main/java")
            assertNotNull(srcMainJavaResource)
            srcMainJava = Path(srcMainJavaResource.path)
            assertNotNull(srcMainJava)

        }

    }
}