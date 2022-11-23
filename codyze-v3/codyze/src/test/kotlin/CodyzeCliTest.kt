package de.fraunhofer.aisec.codyze

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.groups.OptionGroup
import de.fraunhofer.aisec.codyze_core.Executor
import io.mockk.clearAllMocks
import io.mockk.mockk
import java.nio.file.Path
import kotlin.test.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.junit5.KoinTestExtension
import kotlin.io.path.*

class CodyzeCliTest: KoinTest {

    val mockkExecutor = mockk<Executor>(relaxed = true)
    val mockkOptionGroup = mockk<OptionGroup>(relaxed = true)

    @JvmField
    @RegisterExtension
    // starting koin is necessary because some options (e.g., --executor)
    // dynamically look up available choices for the by options(...).choice() command
    val koinTestExtension =
        KoinTestExtension.create { // Initialize the koin dependency injection
            // declare modules necessary for testing
            modules(
                module {
                    factory { mockkOptionGroup } bind OptionGroup::class
                    factory { mockkExecutor } bind Executor::class
                }
            )
        }

    @AfterEach
    fun clearMockks() {
        clearAllMocks()
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
        initCli(pathConfigFile, arrayOf(testSubcommand), arrayOf(testSubcommand.commandName))

        val expectedSpecs = listOf(specMark)
        assertContentEquals(expectedSpecs, testSubcommand.codyzeOptions.spec)
    }

    // Test the behavior of command if both config file and command line options are present
    @Test
    fun configFileWithArgsTest() {
        val tempDir = createTempDirectory()

        val testSubcommand = TestSubcommand()
        initCli(
            correctConfigFile,
            arrayOf(testSubcommand),
            arrayOf(
                testSubcommand.commandName,
                "--output",
                tempDir.absolutePathString(),
                "--spec-additions",
                spec2Mark.absolutePathString(),
                "--timeout",
                "10"
            )
        )

        // should be overwritten by args
        val overwrittenMessage = "CLI options should take precedence over config file"
        assertEquals(tempDir.absolute(), testSubcommand.codyzeOptions.output.absolute(), overwrittenMessage)
        assertEquals(10, testSubcommand.codyzeOptions.timeout, overwrittenMessage)

        // args values should be appended to config values
        val appendMessage = "Values from CLI option should be appended to config values"
        val expectedSpecs = listOf(specMark, spec2Mark)
        assertContentEquals(expectedSpecs.map { it.absolute() }, testSubcommand.codyzeOptions.spec.map { it.absolute() }, appendMessage)

        // should be config values
        val staySameMessage =
            "Config file options should stay the same if it was not matched on CLI"
        assertFalse(testSubcommand.codyzeOptions.goodFindings, staySameMessage)
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
