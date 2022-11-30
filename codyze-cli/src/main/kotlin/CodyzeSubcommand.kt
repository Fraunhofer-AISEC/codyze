package de.fraunhofer.aisec.codyze

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import de.fraunhofer.aisec.codyze.options.CodyzeOptionGroup
import de.fraunhofer.aisec.codyze_core.config.ConfigurationRegister
import de.fraunhofer.aisec.codyze_core.config.configFileOption
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf
import java.nio.file.Path

/** Contains all options that are shared among all Codyze subcommands. */
@Suppress("UNUSED")
abstract class CodyzeSubcommand(help: String = "") : CliktCommand(help = help), KoinComponent {
    // This is only here to correctly display the help message
    private val unusedConfigFile: Path by configFileOption()

    protected val codyzeConfigurationRegister = ConfigurationRegister()
    protected val backendConfigurationRegister = ConfigurationRegister()

    val codyzeOptions by CodyzeOptionGroup(codyzeConfigurationRegister)
    val backendOptions by get {
        parametersOf(backendConfigurationRegister)
    } // inject the backend options that were configured in koin
}
