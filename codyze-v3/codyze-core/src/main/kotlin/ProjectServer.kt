package de.fraunhofer.aisec.codyze_core

import de.fraunhofer.aisec.codyze_core.config.Configuration
import mu.KotlinLogging
import org.koin.java.KoinJavaComponent.getKoin

private val logger = KotlinLogging.logger {}

/**
 * A server that manages all [Project]s. Each [Configuration] corresponds to one potential [Project].
 */
object ProjectServer {

    /** All projects that are connected to the server. */
    val projects = mutableMapOf<Configuration, Project>()
    /** All built-in executors that are available for the analysis. */
    var executors = emptyList<Executor>() // initialized in <registerExecutors>

    /**
     * Initialize the CPG, the available executors and populate the CPG with source code files.
     *
     * 1. load source code (TODO)
     * 2. initialize CPG with given source code (TODO)
     * 3. initialize executors
     */
    init {
        registerExecutors()
    }

    /**
     * Run once when first initializing the AnalysisServer.
     *
     * Uses Koin (a DI library) to get access to all executors
     */
    private fun registerExecutors() {
        executors = getKoin().getAll<Executor>()
        logger.debug {
            "Found executors for following file types: ${executors.flatMap { it.supportedFileExtensions }}"
        }
    }

    /**
     * Returns the project associated with the given [Configuration] object.
     *
     * If no such project exists, a new project with this configuration is added to [projects].
     */
    fun connect(config: Configuration): Project {
        // TODO: remove 'unused' projects from this.projects
        // if project exists -> "reload" else create new project
        return projects.getOrPut(config) { Project(config) }
    }
}
