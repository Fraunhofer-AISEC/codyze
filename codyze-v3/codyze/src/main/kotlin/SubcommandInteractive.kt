package de.fraunhofer.aisec.codyze

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import de.fraunhofer.aisec.codyze.options.*
import java.nio.file.Path
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class Interactive : Subcommand("Start an interactive console") {

    override fun run() {
        logger.debug { "Executing 'interactive' subcommand..." }
        echo("When I grow up, I'll start an interactive console!")
    }
}
