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
package de.fraunhofer.aisec.codyze.specificationLanguages.coko.dsl

import de.fraunhofer.aisec.codyze.core.config.Configuration
import de.fraunhofer.aisec.codyze.core.wrapper.ExecutorConfiguration
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.dsl.cli.validateSpec
import mu.KotlinLogging
import java.nio.file.Path

private val logger = KotlinLogging.logger { }

data class CokoConfiguration(
    val codyzeConfiguration: Configuration,
    val spec: List<Path>,
    val disabledSpecRules: List<String>,
): ExecutorConfiguration {
    // perform some validation
    // the same validation should be performed when parsing the CLI arguments/options
    init {
        validateSpec(spec)
        logger.info { "Using following specs: $spec" }
    }

    /**
     * Nothing to normalize here yet.
     */
    override fun normalize() = this
}
