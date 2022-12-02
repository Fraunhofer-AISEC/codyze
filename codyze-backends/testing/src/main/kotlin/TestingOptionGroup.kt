package de.fraunhofer.aisec.codyzeBackends.testing

import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.*
import de.fraunhofer.aisec.codyzeCore.config.ConfigurationRegister
import de.fraunhofer.aisec.codyzeCore.wrapper.BackendOptions
import java.nio.file.Path

@Suppress("UNUSED")
class TestingOptionGroup(configurationRegister: ConfigurationRegister) :
    BackendOptions(name = "CPG Backend Options") {
    val rawSource: List<Path> by
    option("-s", "--source", "-ss", help = "Source files or folders to analyze.")
        .path(mustExist = true, mustBeReadable = true)
        .multiple(required = true)
        .also { configurationRegister.addOption("source", it) }
}
