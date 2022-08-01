package de.fraunhofer.aisec.codyze

import de.fraunhofer.aisec.codyze.options.*
import de.fraunhofer.aisec.codyze_core.Project
import de.fraunhofer.aisec.codyze_core.ProjectServer
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/** Subcommand that analyzes a set of source files */
class Analyze : CodyzeSubcommand("Analyze a set of source files") {
    // possibly add subcommand-analyze specific options here

    @OptIn(ExperimentalTime::class)
    override fun run() {
        logger.debug { "Executing 'analyze' subcommand..." }

        val (project: Project, projectServerDuration: Duration) =
            measureTimedValue {
                ProjectServer.connect(config = ConfigurationRegister.toConfiguration())
            }
        logger.debug {
            "Project server started in ${ projectServerDuration.inWholeMilliseconds } ms"
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
