package de.fraunhofer.aisec.codyze_common.options

import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.path
import java.io.File
import java.nio.file.Path
import kotlin.io.path.Path

class CodyzeOptions : OptionGroup(name = "Codyze Options") {
    val source: List<Path> by
        option("-s", "--source", help = "Source files or folders to analyze.")
            .path(mustExist = true, mustBeReadable = true)
            .multiple(required = true, default = listOf(Path(System.getProperty("user.dir"))))
    val sourceAdditions: List<Path> by
        option(
                "--source-additions",
                help =
                    "See --source, but appends the values to the ones specified in configuration file"
            )
            .path(mustExist = true, mustBeReadable = true)
            .multiple()
    val disabledSource: List<Path> by
        option(
                "--disabled-source",
                help =
                    "Files or folders specified here will not be analyzed. Symbolic links are not followed when filtering out these paths"
            )
            .path(mustExist = true, mustBeReadable = true)
            .multiple()
    val disabledSourceAdditions: List<Path> by
        option(
                "--disabled-source-additions",
                help =
                    "See --disabled-sources, but appends the values to the ones specified in configuration file."
            )
            .path(mustExist = true, mustBeReadable = true)
            .multiple()

    val spec: List<Path> by
        option("--spec", help = "Loads the given specification files.")
            .path(mustExist = true, mustBeReadable = true, canBeDir = true)
            .multiple(required = true, default = listOf(Path(System.getProperty("user.dir"))))
    val specAdditions: List<Path>? by
        option(
                "--spec-additions",
                help =
                    "See --spec, but appends the values to the ones specified in configuration file."
            )
            .path(mustExist = true, mustBeReadable = true, canBeDir = true)
            .multiple()
    val disabledSpec: List<Path>? by
        option(
                "--disabled-specs",
                help =
                    "The specified files will be excluded from being parsed and" +
                        "processed. The rule has to be specified by its fully qualified name." +
                        "If there is no package name, specify rule as \".<rule>\". Use" +
                        "\"<package>.*\" to disable an entire package."
            )
            .path(mustExist = true, mustBeReadable = true, canBeDir = true)
            .multiple()
    val disabledSpecAdditions: List<Path>? by
        option(
                "--disabled-spec-additions",
                help =
                    "See --disabled-specs, but appends the values to the ones specified in configuration file."
            )
            .path(mustExist = true, mustBeReadable = true, canBeDir = true)
            .multiple()

    val output: File by
        option("-o", "--output", help = "Write results to file. Use - for stdout.")
            .file(mustBeWritable = true)
            .default(File(System.getProperty("user.dir"), "findings.sarif"))

    val goodFindings: Boolean by
        option(
                "--good-findings",
                help =
                    "Enable/Disable output of \"positive\" findings which indicate correct implementations."
            )
            .flag(
                "--no-good-findings",
                "--disable-good-findings",
                default = true,
                defaultForHelp = "enable"
            )
    val pedantic: Boolean by
        option(
                "--pedantic",
                help =
                    "Activates pedantic analysis mode. In this mode, Codyze analyzes all" +
                        "MARK rules and report all findings. This option overrides `disabledMarkRules` and `noGoodFinding`" +
                        "and ignores any Codyze source code comments."
            )
            .flag("--no-pedantic")
    val timeout: Int by
        option("--timeout", help = "Terminate analysis after timeout. [minutes]").int().default(120)
}
