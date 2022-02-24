package de.fraunhofer.aisec.codyze.crymlin

import de.fraunhofer.aisec.codyze.config.Configuration
import org.junit.jupiter.api.Test

internal class CLITest {

    @Test
    fun useAdditionalLanguagePython() {
        val cliParameters = arrayOf("-c", "--additional-languages=python")
        val config = Configuration.initConfig(null, *cliParameters)
        config.buildServerConfiguration()

        // able to handle missing frontends without crashing
        assert(true)
    }
}
