package de.fraunhofer.aisec.codyze

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import de.fraunhofer.aisec.codyze.options.*
import java.nio.file.Path
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class LSP : CliktCommand("Start a language server") {
    // This is only here to correctly display the help message
    private val unusedConfigFile: Path by configFileOption()

    val codyzeOptions by CodyzeOptions()
    val analysisOptions by AnalysisOptions()
    val cpgOptions by CPGOptions()
    val translationOptions by TranslationOptions()
    override fun run() {
        logger.debug { "Executing 'lsp' subcommand..." }
        echo("When I grow up, I'll start a language server!")
    }
}