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
}
