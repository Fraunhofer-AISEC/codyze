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

import com.github.ajalt.clikt.core.subcommands
import de.fraunhofer.aisec.codyze.core.backend.BackendCommand
import de.fraunhofer.aisec.codyze.core.executor.ExecutorCommand
import io.github.detekt.sarif4k.*
import mu.KotlinLogging
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.getKoin

private val logger = KotlinLogging.logger {}

/** Entry point for Codyze. */
fun main(args: Array<String>) {
    startKoin { // Initialize the koin dependency injection
        // use Koin logger
        printLogger()
        // declare modules
        modules(executorCommands, backendCommands, outputBuilders)
    }

    val configFileParser = ConfigFileParser() // use a pre-parser that only parses the config file option
    configFileParser.parse(args)

    // parse the arguments based on the codyze options and the executorOptions/backendOptions
    val codyzeCli = CodyzeCli(configFile = configFileParser.configFile)
        .subcommands(getKoin().getAll<ExecutorCommand<*>>())
    codyzeCli.main(args)

    // get the used subcommands
    val executorCommand = codyzeCli.currentContext.invokedSubcommand as? ExecutorCommand<*>
    val backendCommand = executorCommand?.currentContext?.invokedSubcommand as? BackendCommand<*>

    // this should already be checked by clikt in [codyzeCli.main(args)]
    requireNotNull(executorCommand) { "UsageError! Please select one of the possible executors." }

    val codyzeConfiguration = codyzeCli.codyzeOptions.asConfiguration()
    // the subcommands know how to instantiate their respective backend/executor
    val backend = backendCommand?.getBackend() // [null] if the chosen executor does not support modular backends
    val executor = executorCommand.getExecutor(codyzeConfiguration, backend)

    val run = executor.evaluate()
    codyzeConfiguration.outputBuilder.toFile(run, codyzeConfiguration.output)
}
