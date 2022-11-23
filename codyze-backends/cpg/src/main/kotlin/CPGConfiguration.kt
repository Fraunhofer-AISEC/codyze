package de.fraunhofer.aisec.codyze_backends.cpg

import de.fraunhofer.aisec.codyze_core.config.Configuration
import de.fraunhofer.aisec.codyze_core.config.ConfigurationRegister
import de.fraunhofer.aisec.codyze_core.wrapper.BackendConfiguration
import de.fraunhofer.aisec.cpg.passes.Pass
import java.nio.file.Path
import mu.KotlinLogging

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
data class CPGConfiguration(
    val source:
        List<
            Path
        >, // this is the first argument and thus must be registered first in the [CPGOptions]
    val useUnityBuild: Boolean,
    val typeSystemActiveInFrontend:
        Boolean, // this is the third argument and thus must be the third CLI option to be
    // registered in [CPGOptions]
    val debugParser: Boolean,
    val disableCleanup: Boolean,
    val codeInNodes: Boolean,
    val matchCommentsToNodes: Boolean,
    val processAnnotations: Boolean,
    val failOnError: Boolean,
    val useParallelFrontends: Boolean,
    val defaultPasses: Boolean,
    val additionalLanguages: Set<String>,
    val symbols: Map<String, String>,
    val passes: List<Pass>,
    val loadIncludes: Boolean,
    val includePaths: List<Path>,
    val includeWhitelist: List<Path>,
    val includeBlocklist: List<Path>,
    val typestate: TypestateMode,
) : BackendConfiguration {
    init {
        logger.info { "Analyzing following sources $source" }
        logger.info { "Analyzing following includes $includePaths" }
    }

    override fun normalize(configuration: Configuration): BackendConfiguration {
        var loadIncludes = loadIncludes
        if (useUnityBuild and !loadIncludes) {
            loadIncludes =
                true // we need to force load includes for unity builds, otherwise nothing will be
            // parsed
            logger.info { "Normalized 'loadIncludes' to true because 'useUnityBuild' is true" }
        }

        // construct the normalized configuration objects
        return copy(loadIncludes = loadIncludes)
    }
}
