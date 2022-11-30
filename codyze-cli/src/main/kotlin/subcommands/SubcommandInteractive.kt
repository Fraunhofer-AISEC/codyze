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
package de.fraunhofer.aisec.codyze.subcommands

import de.fraunhofer.aisec.codyze.CodyzeSubcommand
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/** Subcommand that starts an interactive console. */
class Interactive : CodyzeSubcommand("Start an interactive console") {
    // possibly add subcommand-interactive specific options here

    override fun run() {
        logger.debug { "Executing 'interactive' subcommand..." }
        echo("When I grow up, I'll start an interactive console!")
    }
}
