package de.fraunhofer.aisec.codyze

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import de.fraunhofer.aisec.codyze_core.config.options.*
import java.io.File
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class LSP : CliktCommand("Start a language server") {
    // This is only here to correctly display the help message
    // the configFile is actually parsed in the [ConfigFileParser] command and then passed to this
    // class in the constructor
    private val unusedConfigFile: File by configFileOption()

    val codyzeOptions by CodyzeOptions()
    val analysisOptions by AnalysisOptions()
    val cpgOptions by CPGOptions()
    val translationOptions by TranslationOptions()

    override fun run() {
        logger.debug { "Executing 'lsp' subcommand..." }
        echo("When I grow up, I'll start a language server!")
    }
}
