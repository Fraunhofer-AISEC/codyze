/*
 * Copyright (c) 2024, Fraunhofer AISEC. All rights reserved.
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

import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.path
import java.io.File
import java.nio.file.Path

/**
 * Holds the common CLI options for all Plugins.
 * Used in e.g., [PMDPlugin] and [FindSecBugsPlugin].
 */
class PluginOptionGroup(pluginName: String) : OptionGroup(name = "Options for the $pluginName Plugin") {
    val target: List<Path> by option(
        "-t",
        "--target",
        help = "The files to be analyzed. May not always be source files depending on the plugin."
    )
        .path(mustExist = true, mustBeReadable = true)
        .multiple(required = true)

    val separate: Boolean by option(
        "-s",
        "--separate",
        help = "Whether the plugin report should stay separate from the codyze report."
    )
        .flag(
            "--combined",
            default = false,
            defaultForHelp = "combined"
        )

    val output: File by option(
        "-o",
        "--output",
        help = "The path of the resulting report. Only effective in combination with the \"--separate\" flag."
    )
        .file()
        .default(File("$pluginName.sarif"))

    val context: List<Path> by option(
        "-c",
        "--context",
        help = "Additional context required for some plugins (e.g. auxiliary classpaths for FindSecBugs)."
    )
        .path(mustExist = true, mustBeReadable = true)
        .multiple(required = false)
}
