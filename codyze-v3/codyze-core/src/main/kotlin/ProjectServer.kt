package de.fraunhofer.aisec.codyze_core

import de.fraunhofer.aisec.codyze_core.config.Configuration
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * A server that manages [Project]s. Each [Configuration] corresponds to one potential [Project] .
 */
object ProjectServer {

    /** All projects that are connected to the server. */
    val projects: MutableMap<Configuration, Project> = mutableMapOf()

    /**
     * Connect to the project associated with the given [Configuration] object.
     *
     * If this project does not exist, a new project from this [Configuration] is added to the list
     * of open [projects].
     *
     * @param config [Configuration] to get a [Project] from
     * @return [Project] associated with the given [Configuration]
     */
    fun connect(config: Configuration): Project {
        // if project exists -> "reload" else create new project
        return projects.getOrPut(config) { Project(config) }
    }

    /**
     * Disconnect from the [Project] associated with the given [Configuration] object.
     *
     * If no [Project] for the given [Configuration] exists, this method does nothing.
     */
    fun disconnect(config: Configuration) {
        projects.remove(config)
    }
}
