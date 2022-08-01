package de.fraunhofer.aisec.codyze

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
