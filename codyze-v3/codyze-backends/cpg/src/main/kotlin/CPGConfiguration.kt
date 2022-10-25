package de.fraunhofer.aisec.codyze_backends.cpg

import de.fraunhofer.aisec.codyze_core.config.Configuration
import de.fraunhofer.aisec.codyze_core.wrapper.BackendConfiguration
import de.fraunhofer.aisec.cpg.passes.Pass
import mu.KotlinLogging
import java.nio.file.Path

private val logger = KotlinLogging.logger {}

data class CPGConfiguration(
    val source: List<Path>,
    val useUnityBuild: Boolean,
    val typeSystemActiveInFrontend: Boolean,
    val debugParser: Boolean,
    val disableCleanup: Boolean,
    val codeInNodes: Boolean,
    val matchCommentsToNodes: Boolean,
    val processAnnotations: Boolean,
    val failOnError: Boolean,
    val useParallelFrontends: Boolean,
    val defaultPasses: Boolean,
    val additionalLanguages: Set<Language>,
    val symbols: Map<String, String>,
    val passes: List<Pass>,
    val loadIncludes: Boolean,
    val includePaths: List<Path>,
    val includeWhitelist: List<Path>,
    val includeBlacklist: List<Path>,
    val typestate: TypestateMode,
): BackendConfiguration {
    init {
        logger.info { "Analyzing following sources $source" }
        logger.info {
            "Analyzing following includes $includePaths"
        }
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