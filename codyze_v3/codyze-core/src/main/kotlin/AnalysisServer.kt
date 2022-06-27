package de.fraunhofer.aisec.codyze_core

import de.fraunhofer.aisec.codyze_core.config.Configuration
import mu.KotlinLogging
import org.koin.java.KoinJavaComponent.getKoin

private val logger = KotlinLogging.logger {}

object AnalysisServer {

    val projects = mutableMapOf<Configuration, Project>()
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
        // CPG: translationconfiguration gibt einem einen translationManager (der hat ein 'analyze')
        // Und das gibt einem ein 'TranslationResult'
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

    fun connect(config: Configuration): Project {
        // if project exists -> "reload" else create new project
        return projects.getOrPut(config) { Project(config) }
    }
}
