package de.fraunhofer.aisec.codyze.subcommands

import de.fraunhofer.aisec.codyze.CodyzeSubcommand
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
