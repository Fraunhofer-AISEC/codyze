package de.fraunhofer.aisec.codyze.subcommands

import de.fraunhofer.aisec.codyze.CodyzeSubcommand
import de.fraunhofer.aisec.codyze_core.Project
import de.fraunhofer.aisec.codyze_core.ProjectServer
import de.fraunhofer.aisec.codyze_core.config.buildConfiguration
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
                ProjectServer.connect(
                    config =
                        buildConfiguration(
                            codyzeConfigurationRegister,
                            backendConfigurationRegister
                        )
                )
            }
        logger.debug {
            "Project server started in ${ projectServerDuration.inWholeMilliseconds } ms"
        }

        val result = project.doStuff()
        // TODO print results
        println(Json.encodeToString(result))
    }
}
