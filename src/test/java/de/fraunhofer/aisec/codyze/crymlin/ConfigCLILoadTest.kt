package de.fraunhofer.aisec.codyze.crymlin

import de.fraunhofer.aisec.codyze.analysis.TypestateMode
import de.fraunhofer.aisec.codyze.config.Configuration
import java.io.File
import java.lang.Exception
import kotlin.Throws
import kotlin.test.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Test

class ConfigCLILoadTest {

    @Test
    @Throws(Exception::class)
    fun correctConfigFileAndOptionsTest() {
        val options =
            arrayOf(
                "-c",
                "-s=new_source.java",
                "--timeout=160",
                "--sarif", // test if true stays true
                "--unity", // test if false is set to true
                "--analyze-includes=false", // test if false is set to false
                "-m=mark5,mark7,mark6"
            )
        val config = Configuration.initConfig(correctFile, *options)
        val serverConfig = config.buildServerConfiguration()
        val translationConfig = config.buildTranslationConfiguration()

        // assert that CLI configurations have a higher priority than config file configurations
        assertNotEquals(
            File("source.java"),
            config.source,
            "Option specified in CLI should be prioritized"
        )
        assertEquals(File("new_source.java"), config.source)
        assertContentEquals(
            arrayOf("mark5", "mark7", "mark6").map { s -> File(s).absolutePath }.toTypedArray(),
            serverConfig.markModelFiles,
            "Option specified in CLI should be prioritized"
        )
        assertNotEquals(140L, config.timeout, "Option specified in CLI should be prioritized")
        assertEquals(160L, config.timeout)
        assertTrue(config.sarifOutput)
        // loadIncludes is true because of sarifOutput
        assertTrue(translationConfig.loadIncludes)

        // no way to access useUnityBuild in TranslationConfiguration
        //        assertTrue(translationConfig.useUnityBuild)

        // assert that rest is either default value or data from config file
        assertEquals("result.out", config.output)
        assertEquals(TypestateMode.WPDS, serverConfig.typestateAnalysis)
        assertContentEquals(
            arrayOf("include1", "include2").map { s -> File(s).absolutePath }.toTypedArray(),
            translationConfig.includePaths
        )

        // Test for additional languages doesn't work anymore because the LanguageFrontends
        // are not in the library, so they won't be registered
        //        assertEquals(
        //            1,
        //            translationConfig.frontends.size,
        //            "Size of set of additional languages is not 1"
        //        )
        //        assertTrue(translationConfig.frontends.containsKey())

        // assert that nothing else was changed from the default values
        assertFalse(serverConfig.disableGoodFindings)
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
