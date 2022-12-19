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

import de.fraunhofer.aisec.codyze.core.wrapper.BackendCommand
import de.fraunhofer.aisec.codyze.core.wrapper.ExecutorCommand
import io.github.detekt.sarif4k.*
import mu.KotlinLogging
import org.koin.core.context.startKoin

private val logger = KotlinLogging.logger {}

/** Entry point for Codyze. Hands over control to the chosen subcommand immediately. */
fun main(args: Array<String>) {
    startKoin { // Initialize the koin dependency injection
        // use Koin logger
        printLogger()
        // declare modules
        modules(executorCommands, backendCommands)
    }

    val configFileParser = ConfigFileParser() // use a pre-parser that only parses the config file option
    configFileParser.parse(args)

    // parse the arguments based on the codyze options and the executorOptions/backendOptions
    val codyzeCli = CodyzeCli(configFile = configFileParser.configFile)
    codyzeCli.main(args)

    // get the used subcommands
    val executorCommand = codyzeCli.currentContext.invokedSubcommand as? ExecutorCommand<*>
    val backendCommand = executorCommand?.currentContext?.invokedSubcommand as? BackendCommand<*>
    require (executorCommand != null && backendCommand != null) { "UsageError!" }

    // the subcommands know how to instantiate their respective backend/executor
    val backend = backendCommand.getBackend()
    val executor = executorCommand.getExecutor(codyzeCli.codyzeOptions.asConfiguration(), backend)

    val results = executor.evaluate()
    val sarifSchema = SarifSchema210(
        schema = "https://json.schemastore.org/sarif-2.1.0.json",
        version = Version.The210,
        runs = listOf(Run(tool = Tool(driver = ToolComponent(name = "Codyze v3")), results = results,))
    )
    logger.info { sarifSchema.toString() }
}
