package de.fraunhofer.aisec.codyze.crymlin

import de.fraunhofer.aisec.codyze.config.Configuration
import kotlin.test.*
import org.junit.jupiter.api.Test

internal class BuildTranslationConfigTest {

    @Test
    fun enablePythonSupport() {
        val cliParameters = arrayOf("-c", "--enable-python-support")
        val config = Configuration.initConfig(null, *cliParameters)
        config.buildTranslationConfiguration()

        // able to handle missing frontends without crashing
        assert(true)
    }

    @Test
    fun useAdditionalLanguagePython() {
        val cliParameters = arrayOf("-c", "--additional-languages=python")
        val config = Configuration.initConfig(null, *cliParameters)
        config.buildTranslationConfiguration()

        // able to handle missing frontends without crashing
        assert(true)
    }

    @Test
    fun useUnityBuildTest() {
        val cliParameters = arrayOf("-c", "--unity")
        val config = Configuration.initConfig(null, *cliParameters)
        val translationConfig = config.buildTranslationConfiguration()

        assertTrue(
            translationConfig.loadIncludes,
            "LoadIncludes has to be set to true if unityBuild is enabled"
        )
    }
}