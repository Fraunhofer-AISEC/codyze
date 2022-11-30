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
package de.fraunhofer.aisec.codyze.core

import de.fraunhofer.aisec.codyze.core.config.Configuration
import io.github.detekt.sarif4k.*

/**
 * An object that saves the context of an analysis.
 *
 * This enables switching between different analyses (e.g. switching between projects in an IDE).
 */
class Project(val config: Configuration) {
    /** [Executor] that is capable of evaluating the [Configuration.spec] given in [config] */
    val executor = config.executor ?: getRandomCapableExecutor()

    /** Return the first registered Executor capable of evaluating [config.specFileExtension] */
    private fun getRandomCapableExecutor(): Executor {
        val randomCapableExecutor: Executor? =
            ProjectServer.executors.find { it.supportedFileExtension == config.specFileExtension }
        if (randomCapableExecutor != null) {
            return randomCapableExecutor
        } else {
            throw RuntimeException(
                "Did not find any Executor supporting '${config.specFileExtension}' files."
            ) // TODO change to custom exception
        }
    }

    fun doStuff(): SarifSchema210 {
        executor.initialize(config.backendConfiguration, config.toExecutorConfiguration())
        val results: List<Result> = executor.evaluate()

        // complete SARIF model by integrating results, e.g. add "Codyze" as tool name, etc.
        // TODO what format should we give to LSP?
        return SarifSchema210(
            schema = "https://json.schemastore.org/sarif-2.1.0.json",
            version = Version.The210,
            runs = listOf(Run(tool = Tool(driver = ToolComponent(name = "Codyze v3")), results = results,))
        )
    }
}
