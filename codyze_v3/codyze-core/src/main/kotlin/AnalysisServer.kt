package codyze_core

import de.fraunhofer.aisec.codyze.config.Configuration
import de.fraunhofer.aisec.codyze.sarif.schema.Sarif210
import java.nio.file.Path

// @MG
// TODO SARIF JSON schema to Kotlin converting ->
// https://github.com/pwall567/json-kotlin-schema-codegen
// TODO corresponding Gradle plugin -> https://github.com/pwall567/json-kotlin-gradle

class AnalysisServer {

    var projects = emptyMap<String, Project>()
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
    fun connect(confFilePath: Path): Sarif210 {
        // load config from config path
        val config = loadConfiguration(confFilePath = confFilePath)
        // TODO is the newly created project saved to the map
        // if project exists -> "reload" else create new project
        val project = projects.getOrDefault(confFilePath.toRealPath().toString(), Project(config))
        val results = project.doStuff()
        // complete SARIF model by integrating results, e.g. add "Codyze" as tool name, etc.
        // return or print SARIF model
        // TODO what format should we give to LSP?
    }

    private fun loadConfiguration(confFilePath: Path): Configuration {
        // TODO implement
        // call loading facilities from Configuration class
        return Configuration()
    }
}
