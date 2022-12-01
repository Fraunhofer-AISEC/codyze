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
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.output.CliktHelpFormatter
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.*
import de.fraunhofer.aisec.codyze.cli.source.JsonValueSource
import de.fraunhofer.aisec.codyze.cli.subcommands.*
import de.fraunhofer.aisec.codyze.core.config.configFileOption
import de.fraunhofer.aisec.codyze.core.helper.VersionProvider
import mu.KotlinLogging
import org.koin.core.context.startKoin
import java.nio.file.Path
import kotlin.io.path.Path

private val logger = KotlinLogging.logger {}

/**
 * A [CliktCommand] to parse the --config option.
 *
 * This class is only used as a pre-parser to parse the --config option and provide the received
 * config[[Path]] as context to the [[CodyzeCli]] command.
 */
class ConfigFileParser : CliktCommand(treatUnknownOptionsAsArgs = true) {
    val configFile: Path? by configFileOption()
    val arguments by argument().multiple() // necessary when using 'treatUnknownOptionsAsArgs'. Contains all given

    // arguments except for configFile.
    override fun run() {} // does nothing because this command is only used to parse the config file
    // for the [[Codyze]] command.
}

/**
 * Main [CliktCommand]. Provides some options to all included subcommands.
 *
 * The configFile is actually parsed in the [ConfigFileParser] command and then passed to this class
 * as an argument
 */
class CodyzeCli(val configFile: Path = Path(System.getProperty("user.dir"), "config.json")) :
    NoOpCliktCommand(
        help = "Codyze finds security flaws in source code",
        printHelpOnEmptyArgs = true
    ) {
    init {
        versionOption(
            VersionProvider.getVersion("codyze-core"),
            names = setOf("--version", "-V"),
            message = { "Codyze version $it" }
        )
        context {
            valueSource = JsonValueSource.from(configFile, requireValid = true)
            helpFormatter = CliktHelpFormatter(showDefaultValues = true, requiredOptionMarker = "*")
        }
    }
}

/** Entry point for Codyze. Hands over control to the chosen subcommand immediately. */
fun main(args: Array<String>) {
    startKoin { // Initialize the koin dependency injection
        // use Koin logger
        printLogger()
        // declare modules
        modules(executorModule, codyzeModule)
    }

    val configFileParser =
        ConfigFileParser() // create a pre-parser that only parses the config file option
    var codyzeCli =
        CodyzeCli() // create a default codyze parser -> this will be used if the user does not
    // specify a config file
    try {
        configFileParser.parse(args)
        configFileParser.configFile?.let {
            codyzeCli = CodyzeCli(configFile = it)
        } // recreate the codyze parser if the user used the config file option
    } finally {
        codyzeCli
            .subcommands(Analyze(), LSP(), Interactive())
            .main(
                args
            ) // parse the given arguments and run the <run> method of the chosen subcommand.
    }
}