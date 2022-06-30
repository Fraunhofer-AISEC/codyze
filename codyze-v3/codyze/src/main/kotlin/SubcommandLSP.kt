package de.fraunhofer.aisec.codyze

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import de.fraunhofer.aisec.codyze.options.*
import java.nio.file.Path
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class LSP : Subcommand("Start a language server") {

    override fun run() {
        logger.debug { "Executing 'lsp' subcommand..." }
        echo("When I grow up, I'll start a language server!")
    }
}
