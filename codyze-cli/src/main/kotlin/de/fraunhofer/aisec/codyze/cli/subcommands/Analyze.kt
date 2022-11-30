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
package de.fraunhofer.aisec.codyze.cli.subcommands

import de.fraunhofer.aisec.codyze.cli.CodyzeSubcommand
import de.fraunhofer.aisec.codyze_core.ProjectServer
import de.fraunhofer.aisec.codyze_core.config.buildConfiguration
import de.fraunhofer.aisec.codyze_core.timed
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/** Subcommand that analyzes a set of source files */
class Analyze : CodyzeSubcommand("Analyze a set of source files") {
    // possibly add subcommand-analyze specific options here

    override fun run() {
        logger.debug { "Executing 'analyze' subcommand..." }

        val project =
            timed("Starting project server took") {
                ProjectServer.connect(
                    config = buildConfiguration(codyzeConfigurationRegister, backendConfigurationRegister)
                )
            }

        val result = project.doStuff()
        // TODO print results
        logger.info { Json.encodeToString(result) }
    }
}
