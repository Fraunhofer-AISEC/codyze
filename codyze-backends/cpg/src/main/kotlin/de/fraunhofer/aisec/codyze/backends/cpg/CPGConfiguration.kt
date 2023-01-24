/*
 * Copyright (c) 2022, Fraunhofer AISEC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fraunhofer.aisec.codyze.backends.cpg

import de.fraunhofer.aisec.codyze.core.backend.BackendConfiguration
import de.fraunhofer.aisec.cpg.passes.Pass
import mu.KotlinLogging
import java.nio.file.Path

private val logger = KotlinLogging.logger {}

/**
 * Holds the CPG configuration to run the CPG backend with
 *
 * To add a new configuration option do the following:
 * 1. add a property to [CPGConfiguration]
 * 2. add a new CLI option to the [CPGOptionGroup]
 * 3. update the [BackendCommand.getBackend] methods for all implementations of that interface e.g., [BaseCpgBackend]
 */
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
    val additionalLanguages: Set<String>,
    val symbols: Map<String, String>,
    val passes: List<Pass>,
    val loadIncludes: Boolean,
    val includePaths: List<Path>,
    val includeAllowlist: List<Path>,
    val includeBlocklist: List<Path>,
) : BackendConfiguration {
    init {
        logger.info { "Analyzing following sources $source" }
        logger.info { "Analyzing following includes $includePaths" }
    }

    fun normalize(): BackendConfiguration {
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
