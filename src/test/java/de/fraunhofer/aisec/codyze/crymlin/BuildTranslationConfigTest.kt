package de.fraunhofer.aisec.codyze.crymlin

import de.fraunhofer.aisec.codyze.config.Configuration
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
}
