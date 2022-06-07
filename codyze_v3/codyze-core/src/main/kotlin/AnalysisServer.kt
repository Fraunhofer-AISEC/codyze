package de.fraunhofer.aisec.codyze_core

// import de.fraunhofer.aisec.codyze_core.config.Configuration

class AnalysisServer {

    // var projects = emptyMap<String, Project>()
    var executors = emptyList<Executor>()

    // load source code
    // "initialize" CPG and Executor
    // populate CPG with source code files

    init {
        registerExecutors()
    }

    // can be static or run once when AnalysisServer is created
    fun registerExecutors() {
        // TODO implement
    }

    // spawn a new Project
    //    fun connect(confFilePath: Path): SarifSchema210 {
    //        // load config from config path
    //        val config = loadConfiguration(confFilePath = confFilePath)
    //        // TODO is the newly created project saved to the map
    //        // if project exists -> "reload" else create new project
    //        val project = projects.getOrDefault(confFilePath.toRealPath().toString(),
    // Project(config))
    //        val results = project.doStuff()
    //        // complete SARIF model by integrating results, e.g. add "Codyze" as tool name, etc.
    //        // return or print SARIF model
    //        // TODO what format should we give to LSP?
    //    }

    //    private fun loadConfiguration(confFilePath: Path): Configuration {
    //        // TODO implement
    //        // call loading facilities from Configuration class
    //        return Configuration()
    //    }
}
