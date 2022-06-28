package de.fraunhofer.aisec.codyze.crymlin

import de.fraunhofer.aisec.codyze.config.Configuration
import kotlin.test.*
import org.junit.jupiter.api.Test

internal class BuildServerConfigTest {
    @Test
    fun pedanticNoGoodFindingsTest() {
        val cliParameters = arrayOf("-c", "--pedantic", "--noGoodFindings")
        val config = Configuration.initConfig(null, *cliParameters)
        val serverConfig = config.buildServerConfiguration()

        assertTrue(serverConfig.pedantic, "pedantic should be true but was false")
        assertFalse(
            serverConfig.disableGoodFindings,
            "disableGoodFindings should be false because in pedantic mode all findings should be reported"
        )
    }

    @Test
    fun pedanticDisabledMarkRulesTest() {
        val cliParameters =
            arrayOf("-c", "--pedantic", "--disabled-mark-rules=java.UseValidAlgorithm")
        val config = Configuration.initConfig(null, *cliParameters)
        val serverConfig = config.buildServerConfiguration()

        assertTrue(serverConfig.pedantic, "pedantic should be true but was false")
        assertEquals(
            0,
            serverConfig.packageToDisabledMarkRules.size,
            "disableGoodFindings should be false because in pedantic mode all findings should be reported"
        )
    }
}
