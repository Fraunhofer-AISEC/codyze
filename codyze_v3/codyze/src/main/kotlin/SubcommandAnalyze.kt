package de.fraunhofer.aisec.codyze

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import de.fraunhofer.aisec.codyze_core.AnalysisServer
import de.fraunhofer.aisec.codyze_core.config.options.*
import java.io.File
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class Analyze : CliktCommand("Analyze a set of source files") {
    // This is only here to correctly display the help message
    private val unusedConfigFile: File by configFileOption()

    val codyzeOptions by CodyzeOptions()
    val analysisOptions by AnalysisOptions()
    val cpgOptions by CPGOptions()
    val translationOptions by TranslationOptions()
    override fun run() {
        logger.debug { "Executing 'analyze' subcommand..." }
        echo("When I grow up, I'll analyze source code files!")

        val server = AnalysisServer
        println(server.executors)
    }
}
