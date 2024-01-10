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
import io.github.oshai.kotlinlogging.KotlinLogging
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.getKoin
import java.nio.file.Path

private val logger = KotlinLogging.logger {}

/** Entry point for Codyze. */
fun main(args: Array<String>) {
    startKoin { // Initialize the koin dependency injection
        // use Koin logger
        printLogger()
        // declare modules
        modules(executorCommands, backendCommands, outputBuilders, plugins)
    }

    // parse the CMD arguments
    var configFile: Path? = null
    val codyzeCli: CodyzeCli
    try {
        val configFileParser = ConfigFileParser() // use a pre-parser that only parses the config file option
        configFileParser.parse(args)
        configFile = configFileParser.configFile
    } finally {
        // parse the arguments based on the codyze options and the executorOptions/backendOptions
        codyzeCli = CodyzeCli(configFile = configFile)
        codyzeCli.subcommands(getKoin().getAll<ExecutorCommand<*>>())
        codyzeCli.main(args)
    }

    // get the used subcommands
    val executorCommand = codyzeCli.currentContext.invokedSubcommand as? ExecutorCommand<*>

    // allow backendCommand to be null in order to allow executors that do not use backends
    val backendCommand = executorCommand?.currentContext?.invokedSubcommand as? BackendCommand<*>

    // this should already be checked by clikt in [codyzeCli.main(args)]
    requireNotNull(executorCommand) { "UsageError! Please select one of the available executors." }

    val codyzeConfiguration = codyzeCli.codyzeOptions.asConfiguration()
    // the subcommands know how to instantiate their respective backend/executor
    val backend = backendCommand?.getBackend() // [null] if the chosen executor does not support modular backends
    val executor = executorCommand.getExecutor(codyzeConfiguration.goodFindings, codyzeConfiguration.pedantic, backend)

    val run = executor.evaluate()

    // use the chosen [OutputBuilder] to convert the SARIF format (a SARIF RUN) from the executor to the chosen format
    codyzeConfiguration.outputBuilder.toFile(run, codyzeConfiguration.output)

    // run each plugin
    for (plugin in codyzeConfiguration.plugins) {
        logger.info { "Executing Plugin \"${plugin.cliName}\"" }
        TODO("run all plugins - both source and compiled")
        //plugin.execute()
    }

    // aggregate into one SARIF
    TODO("take the separate sarif files and aggregate them")
}
