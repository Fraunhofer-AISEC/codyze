/*
 * Copyright (c) 2022, Fraunhofer AISEC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
