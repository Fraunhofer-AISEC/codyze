package de.fraunhofer.aisec.codyze.plugin.plugins

import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.multiple
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
            "-c",
            "--combined",
            default = false,
            defaultForHelp = "combined"
        )

    val output: File by option(
        "-o",
        "--output",
        help = "The path of the resulting report. Only effective in combination with the \"--separate\" flag."
    )
        .file(mustBeWritable = true)
        .default(File("$pluginName.sarif"))
}