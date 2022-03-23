package de.fraunhofer.aisec.codyze.crymlin

import de.fraunhofer.aisec.codyze.analysis.TypestateMode
import de.fraunhofer.aisec.codyze.config.Configuration
import java.io.File
import java.lang.Exception
import kotlin.Throws
import kotlin.test.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Test
import picocli.CommandLine

internal class CLILoadTest {

    @Test
    @Throws(Exception::class)
    fun noArgsTest() {
        Assertions.assertThrows(CommandLine.MissingParameterException::class.java) {
            Configuration.initConfig(null, *emptyArray())
        }
    }

    @Test
    @Throws(Exception::class)
    fun exclusiveOptionTest() {
        Assertions.assertThrows(CommandLine.MutuallyExclusiveArgsException::class.java) {
            Configuration.initConfig(null, "-t", "-c")
        }
    }

    @Test
    @Throws(Exception::class)
    fun unknownOptionTest() {
        val config = Configuration.initConfig(null, "-c", "-z")
        val serverConfig = config.buildServerConfiguration()
        val translationConfig = config.buildTranslationConfiguration()

        // assert that nothing was changed from the default values
        assertTrue(config.executionMode.isCli)
        assertFalse(config.executionMode.isLsp)
        assertFalse(config.executionMode.isTui)
        assertNull(config.source)
        assertEquals(120L, config.timeout)
        assertEquals("findings.sarif", config.output)
        assertFalse(config.sarifOutput)

        assertContentEquals(
            arrayOf("./").map { s -> File(s).absolutePath }.toTypedArray(),
            serverConfig.markModelFiles
        )
        assertEquals(TypestateMode.DFA, serverConfig.typestateAnalysis)
        assertFalse(serverConfig.disableGoodFindings)
        assertFalse(serverConfig.pedantic)

        assertFalse(translationConfig.loadIncludes)
        assertEquals(0, translationConfig.includePaths.size, "Array of includes was not empty")
        assertEquals(
            2,
            translationConfig.frontends.size,
            "List of frontends did not only contain default frontends"
        )

        // no way to access useUnityBuild in TranslationConfiguration
        //        assertFalse(translationConfig.useUnityBuild)
    }

    @Test
    @Throws(Exception::class)
    fun languageOptionTest() {
        val config = Configuration.initConfig(null, "-c", "--additional-languages=PYTHON")
        val translationConfiguration = config.buildTranslationConfiguration(File("test.java"))
        // language frontends are not imported as library so the test won't work
        // assertEquals(3, translationConfiguration.frontends.size, "Expected size 1 but was
        // ${translationConfiguration.frontends.size}")
    }

    @Test
    @Throws(Exception::class)
    fun passesOptionTest() {
        val config =
            Configuration.initConfig(
                null,
                "-c",
                "--passes=de.fraunhofer.aisec.cpg.passes.EdgeCachePass," +
                    "de.fraunhofer.aisec.cpg.passes.FilenameMapper," +
                    "de.fraunhofer.aisec.cpg.passes.CallResolver"
            )
        val translationConfiguration = config.buildTranslationConfiguration(File("test.java"))

        val expectedPassesNames =
            arrayOf(
                "de.fraunhofer.aisec.cpg.passes.EdgeCachePass",
                "de.fraunhofer.aisec.cpg.passes.FilenameMapper",
                "de.fraunhofer.aisec.cpg.passes.CallResolver"
            )
        assertEquals(
            3,
            translationConfiguration.registeredPasses.size,
            "Expected size 3 but was ${translationConfiguration.registeredPasses.size}"
        )
        val passesNames = translationConfiguration.registeredPasses.map { s -> s.javaClass.name }
        assertContentEquals(expectedPassesNames, passesNames.toTypedArray())
    }

    @Test
    @Throws(Exception::class)
    fun invalidPassesOptionTest() {
        val config =
            Configuration.initConfig(
                null,
                "-c",
                "--passes=de.fraunhofer.aisec.cpg.passes.MyPass," +
                    "de.fraunhofer.aisec.cpg.passes.Pass," +
                    "de.fraunhofer.aisec.cpg.passes.scopes.BlockScope," +
                    "de.fraunhofer.aisec.cpg.passes.EdgeCachePass," +
                    "MyPass2"
            )
        val translationConfiguration = config.buildTranslationConfiguration(File("test.java"))
        assertEquals(
            1,
            translationConfiguration.registeredPasses.size,
            "Expected to have size 1 but was ${translationConfiguration.registeredPasses.size}"
        )
        assertEquals(
            "de.fraunhofer.aisec.cpg.passes.EdgeCachePass",
            translationConfiguration.registeredPasses[0].javaClass.name
        )
    }

    @Test
    @Throws(Exception::class)
    fun symbolsOptionTest() {
        val config = Configuration.initConfig(null, "-c", "--symbols=#=hash,*=star")
        val translationConfiguration = config.buildTranslationConfiguration(File("test.java"))
        assertEquals(
            2,
            translationConfiguration.symbols.size,
            "Expected size 2 but was ${translationConfiguration.symbols.size}"
        )
        assertTrue(
            translationConfiguration.symbols.containsKey("#"),
            "Did not contain \'#\' as a key"
        )
        assertEquals("hash", translationConfiguration.symbols["#"])
        assertTrue(
            translationConfiguration.symbols.containsKey("*"),
            "Did not contain \'*\' as a key"
        )
        assertEquals("star", translationConfiguration.symbols["*"])
    }
}
