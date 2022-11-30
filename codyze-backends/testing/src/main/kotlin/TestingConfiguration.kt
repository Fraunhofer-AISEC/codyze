package de.fraunhofer.aisec.codyzeBackends.testing

import de.fraunhofer.aisec.codyzeCore.config.Configuration
import de.fraunhofer.aisec.codyzeCore.config.ConfigurationRegister
import de.fraunhofer.aisec.codyzeCore.wrapper.BackendConfiguration
import mu.KotlinLogging
import java.nio.file.Path

private val logger = KotlinLogging.logger {}

/**
 * Holds the CPG configuration to run Codyze with
 *
 * To add a new CPG configuration option do the following:
 * 1. add a property to [CPGConfiguration]
 * 3. add a new CLI option to the [CPGOptionGroup]
 * 4. make sure that the CLI option is NOT nullable. Null options might cause problems with the used
 * dependency injection
 * 5. after adding the new CLI option, register it at the [ConfigurationRegister]. Only then will it
 * be part of the map returned by [ConfigurationRegister.options] which is used to initialize the
 * [CPGConfiguration] object
 * 6. Make sure that the place of the new option in the argument list (e.g., source is argument 1 to
 * [CPGConfiguration]) corresponds to the place to which it is registered in the [CPGOptionGroup]
 * (e.g., source is registered first in the [CPGOptionGroup])
 */
data class TestingConfiguration(
    val source:
        List<
            Path
            >, // this is the first argument and thus must be registered first in the [CPGOptions]
) : BackendConfiguration {
    init {
        logger.info { "Analyzing following sources $source" }
    }

    override fun normalize(configuration: Configuration) = this
}
