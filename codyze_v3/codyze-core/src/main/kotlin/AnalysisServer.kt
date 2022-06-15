package de.fraunhofer.aisec.codyze_core

import mu.KotlinLogging
import org.koin.java.KoinJavaComponent.getKoin

// import de.fraunhofer.aisec.codyze_core.config.Configuration
private val logger = KotlinLogging.logger {}

object AnalysisServer {

    // var projects = emptyMap<String, Project>()
    var executors = emptyList<Executor>()  // initialized in <init>

    /**
     * Initialize the CPG, the available executors and populate the CPG with source code files.
     *
     * 1. load source code (TODO)
     * 2. initialize CPG with given source code (TODO)
     * 3. initialize executors
     */
    init {
        registerExecutors()
        // CPG: translationconfiguration gibt einem einen translationManager (der hat ein 'analyze') Und das gibt einem ein 'TranslationResult'
    }

    /**
     * Run once when first initializing the AnalysisServer.
     *
     * Uses Koin (a DI library) to get access to all executors
     */
    private fun registerExecutors() {
        executors = getKoin().getAll<Executor>()
        logger.debug { "Found executors for following file types: ${executors.flatMap { it.supportedFileExtensions }}" }
    }

// spawn a new Project
//    fun connect(confFilePath: Path): SarifSchema210 {
//        // TODO is the newly created project saved to the map
//        // if project exists -> "reload" else create new project
//        val project = projects.getOrDefault(confFilePath.toRealPath().toString(),
// Project(config))
//        val results = project.doStuff()
//        // complete SARIF model by integrating results, e.g. add "Codyze" as tool name, etc.
//        // return or print SARIF model
//        // TODO what format should we give to LSP?
//    }
}
