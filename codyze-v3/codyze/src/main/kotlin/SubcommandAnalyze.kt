package de.fraunhofer.aisec.codyze

import de.fraunhofer.aisec.codyze.options.*
import de.fraunhofer.aisec.codyze_core.Project
import de.fraunhofer.aisec.codyze_core.ProjectServer
import java.time.Instant
import kotlin.time.Duration
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/** Subcommand that analyzes a set of source files */
class Analyze : CodyzeSubcommand("Analyze a set of source files") {
    // possibly add subcommand-analyze specific options here

    override fun run() {
        logger.debug { "Executing 'analyze' subcommand..." }

        val start = Instant.now()
        val project: Project =
            ProjectServer.connect(config = ConfigurationRegister.toConfiguration())
        logger.debug {
            "Analysis server started in ${ java.time.Duration.between(start, Instant.now()) } ms"
        }

        logger.info { "Analyzing following sources ${project.config.cpgConfiguration.source}" }
        logger.info {
            "Following following includes ${project.config.cpgConfiguration.includePaths}"
        }
        logger.info { "Using following specs ${project.config.spec}" }

        val result = project.doStuff()
        // TODO print results
        println(Json.encodeToString(result))
    }
}
