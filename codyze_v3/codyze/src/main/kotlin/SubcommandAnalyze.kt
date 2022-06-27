package de.fraunhofer.aisec.codyze

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import de.fraunhofer.aisec.codyze_core.AnalysisServer
import de.fraunhofer.aisec.codyze.options.*
import java.nio.file.Path
import java.time.Duration
import java.time.Instant
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

@Suppress("UNUSED")
class Analyze : CliktCommand("Analyze a set of source files") {
    // This is only here to correctly display the help message
    private val unusedConfigFile: Path by configFileOption()

    val codyzeOptions by CodyzeOptions()
    val analysisOptions by AnalysisOptions()
    val cpgOptions by CPGOptions()
    val translationOptions by TranslationOptions()

    override fun run() {
        logger.debug { "Executing 'analyze' subcommand..." }

        val start = Instant.now()
        val server = AnalysisServer
        logger.debug {
            "Analysis server started in ${ Duration.between(start, Instant.now()).toMillis() } ms"
        }

        logger.info { "Analyzing following sources ${server.config.cpgConfiguration.source}" }
        logger.info {
            "Following following includes ${server.config.cpgConfiguration.includePaths}"
        }
        logger.info { "Following following specs ${server.config.spec}" }
    }
}
