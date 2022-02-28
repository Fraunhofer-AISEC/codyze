package de.fraunhofer.aisec.codyze.crymlin

import de.fraunhofer.aisec.codyze.analysis.TypestateMode
import de.fraunhofer.aisec.codyze.config.Configuration
import java.io.File
import kotlin.Exception
import kotlin.Throws
import kotlin.test.*
import org.junit.jupiter.api.Test

internal class ConfigLoadTest {

    @Test
    @Throws(Exception::class)
    fun correctConfigFileTest() {
        val config = Configuration.initConfig(correctFile, "-c")
        val serverConfig = config.buildServerConfiguration()
        val translationConfig = config.buildTranslationConfiguration()

        // assert that the data in the config file was parsed and set correctly
        assertEquals(File("source.java"), config.source)
        assertContentEquals(
            arrayOf("mark1", "mark4", "mark3", "mark2")
                .map { s -> File(s).absolutePath }
                .toTypedArray(),
            serverConfig.markModelFiles
        )
        assertEquals("result.out", config.output)
        assertEquals(140L, config.timeout)
        assertTrue(config.sarifOutput)
        assertEquals(TypestateMode.WPDS, serverConfig.typestateAnalysis)

        assertFalse(translationConfig.loadIncludes)
        assertContentEquals(
            arrayOf("include1", "include2").map { s -> File(s).absolutePath }.toTypedArray(),
            translationConfig.includePaths
        )

        // Test for additional languages doesn't really work because the LanguageFrontends
        // are not in the library, so they won't be registered
        //        assertEquals(
        //            1,
        //            translationConfig.frontends.size,
        //            "Size of set of additional languages is not 1"
        //        )
        //        assertTrue(translationConfig.frontends.containsKey())

        // assert that nothing else was changed from the default values
        assertFalse(serverConfig.disableGoodFindings)
        assertFalse(serverConfig.pedantic)

        // no way to access useUnityBuild in TranslationConfiguration
        //        assertFalse(translationConfig.useUnityBuild)
    }

    @Test
    @Throws(Exception::class)
    fun incorrectConfigFileTest() {
        val config = Configuration.initConfig(incorrectFile, "-c")
        val serverConfig = config.buildServerConfiguration()
        val translationConfig = config.buildTranslationConfiguration()

        // assert that nothing was changed from the default values
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

    companion object {
        private val correctFile =
            File(
                ConfigLoadTest::class
                    .java
                    .classLoader
                    .getResource("config-files/correct_structure.yml")
                    .toURI()
            )
        private val incorrectFile =
            File(
                ConfigLoadTest::class
                    .java
                    .classLoader
                    .getResource("config-files/incorrect_structure.yml")
                    .toURI()
            )
    }
}
