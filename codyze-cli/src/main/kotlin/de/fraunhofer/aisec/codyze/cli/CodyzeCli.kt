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
package de.fraunhofer.aisec.codyze.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.NoOpCliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.output.MordantHelpFormatter
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.versionOption
import com.github.ajalt.clikt.parameters.types.path
import de.fraunhofer.aisec.codyze.core.VersionProvider
import java.nio.file.Path
import kotlin.io.path.Path

/**
 * The Clikt [option] for the config file. Defined as extension function because it is used in multiple [CliktCommand]s.
 */
fun CliktCommand.configFileOption() =
    option(
        "--config",
        help = "Parse configuration settings from this file. " +
            "If no file path is specified, Codyze will try to load the configuration file from the default path"
    )
        .path(mustExist = true, canBeDir = false, mustBeReadable = true)
        .default(Path(System.getProperty("user.dir"), "codyze.json"))

/**
 * A [CliktCommand] to parse the --config option.
 *
 * This class is only used as a pre-parser to parse the --config option and provide the received
 * config[[Path]] as context to the [[CodyzeCli]] command.
 */
@Suppress("Unused")
class ConfigFileParser : CliktCommand(treatUnknownOptionsAsArgs = true) {
    val configFile: Path by configFileOption()

    // necessary when using 'treatUnknownOptionsAsArgs'. Contains all given arguments except for configFile
    val arguments by argument().multiple()

    // does nothing because this command is only used to parse the config file for the [[Codyze]] command
    override fun run() = Unit
}

/**
 * Main [CliktCommand]. Provides the common Codyze options. Each executor must provide a [CliktCommand] that is
 * registered as a subcommand on [CodyzeCli].
 *
 * The configFile is actually parsed in the [ConfigFileParser] command and then passed to this class
 * as an argument
 */
@Suppress("Unused", "UnusedPrivateMember")
class CodyzeCli(val configFile: Path?) :
    NoOpCliktCommand(help = "Codyze finds security flaws in source code", printHelpOnEmptyArgs = true) {

    init {
        versionOption(
            VersionProvider.getVersion("codyze-core"),
            names = setOf("--version", "-V"),
            message = { "Codyze version $it" }
        )
        context {
            valueSource = configFile?.let { JsonValueSource.from(it, requireValid = true) }
            helpFormatter = { MordantHelpFormatter(it, requiredOptionMarker = "*", showDefaultValues = true) }
        }
    }

    // This is only here to correctly display the help message
    private val unusedConfigFile: Path? by configFileOption()

    val codyzeOptions by CodyzeOptionGroup()
}
