/*
 * Copyright (c) 2023, Fraunhofer AISEC. All rights reserved.
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
package de.fraunhofer.aisec.codyze.core.plugin

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import de.fraunhofer.aisec.codyze.core.output.aggregator.Aggregate
import de.fraunhofer.aisec.codyze.core.output.aggregator.extractLastRun
import io.github.oshai.kotlinlogging.KotlinLogging
import org.koin.core.module.Module
import java.io.File
import java.nio.file.Path

val logger = KotlinLogging.logger { }

/**
 * Plugins perform a standalone analysis independent of the Codyze Executors.
 * They usually use already developed libraries from open-source analysis tools.
 * When developing a new Plugin, do not forget to add it to the respective [KoinModules],
 * otherwise it will not be selectable in the configuration.
 * Also, remember to add a page to docs/plugins.
 */
abstract class Plugin(private val cliName: String) :
    CliktCommand(name = cliName) {

    /*
     * Configure Clikt command
     */
    override val hiddenFromHelp: Boolean = true

    private val options by PluginOptionGroup(cliName)

    /**
     * Executes the respective analysis tool.
     * @param target The files to be analyzed
     * @param context Additional context, plugin-specific
     * @param output The location of the results
     */
    abstract fun execute(target: List<Path>, context: List<Path>, output: File)

    abstract fun module(): Module

    /**
     * Define two plugins as equal if they are of the same type and therefore have the same CLI name.
     * This is necessary to filter out duplicate Plugins when parsing the cli arguments
     */
    override fun equals(other: Any?): Boolean {
        if (other is Plugin) {
            return this.cliName == other.cliName
        }
        return false
    }

    override fun hashCode(): Int {
        return cliName.hashCode()
    }

    override fun run() {
        // Execute the Plugin and print to the specified location
        execute(
            options.target,
            options.context,
            options.output
        )

        // Add the run to the Aggregate if not specified to be separate
        if (!options.separate) {
            val run = extractLastRun(options.output)
            if (run != null) {
                Aggregate.addRun(run)
            }
            options.output.delete()
        }
    }
}
