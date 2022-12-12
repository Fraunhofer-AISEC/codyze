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
package de.fraunhofer.aisec.codyze.core.config

import de.fraunhofer.aisec.codyze.core.Executor
import de.fraunhofer.aisec.codyze.core.wrapper.BackendConfiguration
import mu.KotlinLogging
import java.nio.file.Path

private val logger = KotlinLogging.logger {}

/**
 * Holds the whole configuration to run Codyze with
 *
 * To add a new Codyze configuration option do the following:
 * 1. add a property to [Configuration]
 * 2. add the new property to the [Configuration.from] factory method
 * 3. add a new CLI option to one (or all) subcommand(s). It does not matter whether the option is
 * inside an (already existing) OptionGroup.
 * 4. make sure to add a '?' for any CLI option that might be null. Options that might be null and
 * are not specified as such can cause issues with the map delegate used in the factory methods.
 * 5. after adding the new CLI option, register it at the [ConfigurationRegister]. Only then will it
 * be part of the map returned by [ConfigurationRegister.options] which is used to initialize the
 * [Configuration] object
 */
data class Configuration(
    val spec: List<Path>,
    val specDescription: Path,
    val disabledSpecRules: List<String>,
    val output: Path,
    val goodFindings: Boolean,
    val pedantic: Boolean,
    val timeout: Int,
    val executor: Executor? =
        null, // if null, Codyze randomly chooses an Executor capable of evaluating the given
    // specs. If no Executor is found, an error is thrown
    val backendConfiguration: BackendConfiguration,
) {
    // perform some validation
    // the same validation should be performed when parsing the CLI arguments/options
    init {
        validateSpec(spec)
        logger.info { "Using following specs: $spec" }
    }

    /** Filename extension of all [spec] files. All [spec] files share the same extension. */
    val specFileExtension by lazy { spec[0].extensions }

    companion object {
        /**
         * Build a [Configuration] object from a [map] and a [backendConfiguration]. The same map
         * used to initialize a [backendConfiguration] can be re-used here.
         */
        fun from(map: Map<String, Any?>, backendConfiguration: BackendConfiguration) =
            object {
                val spec: List<Path> by map
                val specDescription: Path by map
                val disabledSpecRules: List<String> by map
                val output: Path by map
                val goodFindings: Boolean by map
                val pedantic: Boolean by map
                val timeout: Int by map
                val executor: Executor? by map

                val data =
                    Configuration(
                        spec = spec,
                        specDescription = specDescription,
                        disabledSpecRules = disabledSpecRules,
                        output = output,
                        goodFindings = goodFindings,
                        pedantic = pedantic,
                        timeout = timeout,
                        executor = executor,
                        backendConfiguration = backendConfiguration,
                    )
            }
                .data
    }

    /**
     * Return a normalized [Configuration]
     *
     * You should pretty much always only use the normalized [Configuration] to run Codyze.
     * Otherwise, some settings might contradict each other.
     */
    fun normalize(): Configuration {
        val normalizedBackendConfiguration = backendConfiguration.normalize(this)

        var goodFindings = this.goodFindings
        if (this.pedantic and !goodFindings) {
            goodFindings = true // In pedantic analysis mode all findings reported
            logger.info { "Normalized 'goodFindings' to true because 'pedantic' is true" }
        }

        return this.copy(
            goodFindings = goodFindings,
            backendConfiguration = normalizedBackendConfiguration
        )
    }

    /** Return an [ExecutorConfiguration] object */
    fun toExecutorConfiguration(): ExecutorConfiguration =
        ExecutorConfiguration(
            spec = this.spec,
            specDescription = this.specDescription,
            disabledSpecRules = this.disabledSpecRules,
            goodFindings = this.goodFindings,
            pedantic = this.pedantic,
            timeout = this.timeout
        )
}

/** A simplified version of the full [Configuration] used for [Executor] initialization */
data class ExecutorConfiguration(
    val spec: List<Path>,
    val specDescription: Path,
    val disabledSpecRules: List<String>,
    val goodFindings: Boolean,
    val pedantic: Boolean,
    val timeout: Int,
)
