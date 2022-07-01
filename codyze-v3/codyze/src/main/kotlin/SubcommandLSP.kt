package de.fraunhofer.aisec.codyze

import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class LSP : CodyzeSubcommand("Start a language server") {
    // possibly add subcommand-lsp specific options here

    override fun run() {
        logger.debug { "Executing 'lsp' subcommand..." }
        echo("When I grow up, I'll start a language server!")
    }
}
