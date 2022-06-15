package de.fraunhofer.aisec.codyze_core.config.options

import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.path
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.isRegularFile
import kotlin.streams.asSequence

class CodyzeOptions : OptionGroup(name = "Codyze Options") {
    internal val rawSource: List<Path> by
        option("-s", "--source", help = "Source files or folders to analyze.")
            .path(mustExist = true, mustBeReadable = true)
            .multiple(required = true)
    internal val rawSourceAdditions: List<Path> by
        option(
                "--source-additions",
                help =
                    "See --source, but appends the values to the ones specified in configuration file"
            )
            .path(mustExist = true, mustBeReadable = true)
            .multiple()
    internal val rawDisabledSource: List<Path> by
        option(
                "--disabled-source",
                help =
                    "Files or folders specified here will not be analyzed. Symbolic links are not followed when filtering out these paths"
            )
            .path(mustExist = true, mustBeReadable = true)
            .multiple()
    internal val rawDisabledSourceAdditions: List<Path> by
        option(
                "--disabled-source-additions",
                help =
                    "See --disabled-sources, but appends the values to the ones specified in configuration file."
            )
            .path(mustExist = true, mustBeReadable = true)
            .multiple()

    /**
     * Lazy property that combines all given sources from the different options into a list of files to analyze.
     */
    val source: List<Path> by lazy {
        resolvePaths(source = rawSource, sourceAdditions = rawSourceAdditions, disabledSource = rawDisabledSource, disabledSourceAdditions = rawDisabledSourceAdditions)
    }

    internal val rawSpec: List<Path> by
        option("--spec", help = "Loads the given specification files.")
            .path(mustExist = true, mustBeReadable = true, canBeDir = true)
            .multiple(required = true)
    internal val rawSpecAdditions: List<Path> by
        option(
                "--spec-additions",
                help =
                    "See --spec, but appends the values to the ones specified in configuration file."
            )
            .path(mustExist = true, mustBeReadable = true, canBeDir = true)
            .multiple()
    internal val rawDisabledSpec: List<Path> by
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
    internal val rawDisabledSpecAdditions: List<Path> by
        option(
                "--disabled-spec-additions",
                help =
                    "See --disabled-specs, but appends the values to the ones specified in configuration file."
            )
            .path(mustExist = true, mustBeReadable = true, canBeDir = true)
            .multiple()
    /**
     * Lazy property that combines all given specs from the different options into a list of spec files to use.
     */
    val spec: List<Path> by lazy {
        resolvePaths(source = rawSpec, sourceAdditions = rawSpecAdditions, disabledSource = rawDisabledSpec, disabledSourceAdditions = rawDisabledSpecAdditions)
    }

    val output: Path by
        option("-o", "--output", help = "Write results to file. Use - for stdout.")
            .path(mustBeWritable = true)
            .default(Path(System.getProperty("user.dir"), "findings.sarif"))

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

    private fun resolvePaths(source: List<Path>, sourceAdditions: List<Path>, disabledSource: List<Path>, disabledSourceAdditions: List<Path>): List<Path> {
        return (combineSources(source, sourceAdditions) - combineSources(disabledSource, disabledSourceAdditions)).toList()
    }
}
