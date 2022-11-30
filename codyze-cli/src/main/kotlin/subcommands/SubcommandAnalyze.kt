package de.fraunhofer.aisec.codyze.subcommands

import de.fraunhofer.aisec.codyze.CodyzeSubcommand
import de.fraunhofer.aisec.codyze_core.ProjectServer
import de.fraunhofer.aisec.codyze_core.config.buildConfiguration
import de.fraunhofer.aisec.codyze_core.timed
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/** Subcommand that analyzes a set of source files */
class Analyze : CodyzeSubcommand("Analyze a set of source files") {
    // possibly add subcommand-analyze specific options here

    override fun run() {
        logger.debug { "Executing 'analyze' subcommand..." }

        val project =
            timed("Starting project server took") {
                ProjectServer.connect(
                    config = buildConfiguration(codyzeConfigurationRegister, backendConfigurationRegister)
                )
            }

        val result = project.doStuff()
        // TODO print results
        println(Json.encodeToString(result))
    }
}
