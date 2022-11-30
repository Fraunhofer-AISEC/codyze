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
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Subcommand that starts a language server.
 *
 * This server can be connected to an IDE to automatically analyze source code while programming.
 */
class LSP : CodyzeSubcommand("Start a language server") {
    // possibly add subcommand-lsp specific options here

    override fun run() {
        logger.debug { "Executing 'lsp' subcommand..." }
        echo("When I grow up, I'll start a language server!")
    }
}
