package de.fraunhofer.aisec.codyze.options

import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.path
import de.fraunhofer.aisec.codyze_core.Executor
import de.fraunhofer.aisec.codyze_core.ProjectServer
import de.fraunhofer.aisec.codyze_core.config.ConfigurationRegister
import de.fraunhofer.aisec.codyze_core.config.combineSources
import de.fraunhofer.aisec.codyze_core.config.validateFromError
import de.fraunhofer.aisec.codyze_core.config.validateSpec
import java.nio.file.Path
import kotlin.io.path.Path

@Suppress("UNUSED")
class CodyzeOptionGroup(configurationRegister: ConfigurationRegister) :
    OptionGroup(name = "Codyze Options") {
    private val rawSpec: List<Path> by option("--spec", help = "Loads the given specification files.")
        .path(mustExist = true, mustBeReadable = true, canBeDir = true)
        .multiple(required = true)
        .validateFromError { validateSpec(spec) }
    private val rawSpecAdditions: List<Path> by option(
        "--spec-additions",
        help =
        "See --spec, but appends the values to the ones specified in configuration file."
    )
        .path(mustExist = true, mustBeReadable = true, canBeDir = true)
        .multiple()
    private val rawDisabledSpec: List<Path> by option(
        "--disabled-specs",
        help =
        "The specified files will be excluded from being parsed and" +
            "processed. The rule has to be specified by its fully qualified name." +
            "If there is no package name, specify rule as \".<rule>\". Use" +
            "\"<package>.*\" to disable an entire package."
    )
        .path(mustExist = true, mustBeReadable = true, canBeDir = true)
        .multiple()
    private val rawDisabledSpecAdditions: List<Path> by option(
        "--disabled-spec-additions",
        help =
        "See --disabled-specs, but appends the values to the ones specified in configuration file."
    )
        .path(mustExist = true, mustBeReadable = true, canBeDir = true)
        .multiple()

    // TODO: get specDescription from spec files? Now that Codyze does not check their file type
    // anymore, this is an option
    val specDescription: Path by option(
        "--spec-description",
        help = "A .json file mapping rule IDs to rule descriptions."
    )
        .path(mustExist = true, mustBeReadable = true)
        .default(Path(System.getProperty("user.dir"), "findingDescription.json"))
        .also { configurationRegister.addOption("specDescription", it) }
    val disabledSpecRules: List<String> by option(
        "--disabled-spec-rules",
        help = "Rules that will be ignored by the analysis."
    )
        .multiple()
        .also { configurationRegister.addOption("disabledSpecRules", it) }

    /**
     * Lazy property that combines all given specs from the different options into a list of spec
     * files to use.
     */
    val spec: List<Path> by lazy {
        resolvePaths(
            source = rawSpec,
            sourceAdditions = rawSpecAdditions,
            disabledSource = rawDisabledSpec,
            disabledSourceAdditions = rawDisabledSpecAdditions
        )
    }
        .also {
            configurationRegister.addLazyOption(
                name = "spec",
                lazyProperty = it,
                thisRef = this,
                property = ::spec
            )
        }

    val executor: Executor? by option(
        "--executor",
        help =
        "Manually choose Executor to use with the given spec files. If unspecified, Codyze randomly selects an executor capable of evaluating the given specification files."
    )
        .choice(*(ProjectServer.executors.map { it.name }).toTypedArray(), ignoreCase = true)
        .convert {
            it.let { ProjectServer.executors.first { executor -> executor.name == it } }
        }
        .also { configurationRegister.addOption("executor", it) }

    val output: Path by option("-o", "--output", help = "Write results to file. Use - for stdout.")
        .path(mustBeWritable = true)
        .default(Path(System.getProperty("user.dir"), "findings.sarif"))
        .also { configurationRegister.addOption("output", it) }
    val goodFindings: Boolean by option(
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
        .also { configurationRegister.addOption("goodFindings", it) }
    val pedantic: Boolean by option(
        "--pedantic",
        help =
        "Activates pedantic analysis mode. In this mode, Codyze analyzes all" +
            "MARK rules and report all findings. This option overrides `disabledMarkRules` and `noGoodFinding`" +
            "and ignores any Codyze source code comments."
    )
        .flag("--no-pedantic")
        .also { configurationRegister.addOption("pedantic", it) }
    val timeout: Int by option("--timeout", help = "Terminate analysis after timeout. [minutes]")
        .int()
        .default(120)
        .also { configurationRegister.addOption("timeout", it) }

    private fun resolvePaths(
        source: List<Path>,
        sourceAdditions: List<Path>,
        disabledSource: List<Path>,
        disabledSourceAdditions: List<Path>
    ): List<Path> {
        return (
            combineSources(source, sourceAdditions) -
                combineSources(disabledSource, disabledSourceAdditions)
            )
            .toList()
    }
}
