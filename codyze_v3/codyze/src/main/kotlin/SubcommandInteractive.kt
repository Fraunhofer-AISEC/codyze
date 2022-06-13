package de.fraunhofer.aisec.codyze

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import de.fraunhofer.aisec.codyze_core.config.options.*
import java.io.File
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class Interactive : CliktCommand("Start an interactive console") {
    // This is only here to correctly display the help message
    // the configFile is actually parsed in the [ConfigFileParser] command and then passed to this
    // class in the constructor
    private val unusedConfigFile: File by
        option(
                "--config",
                help =
                    "Parse configuration settings from this file. If no file path is" +
                        "specified, Codyze will try to load the configuration file from the default path",
            )
            .file()
            .default(File(System.getProperty("user.dir"), "config.json"))

    val codyzeOptions by CodyzeOptions()
    val analysisOptions by AnalysisOptions()
    val cpgOptions by CPGOptions()
    val translationOptions by TranslationOptions()

    override fun run() {
        logger.debug { "Executing 'interactive' subcommand..." }
        echo("When I grow up, I'll start an interactive console!")
    }
}
