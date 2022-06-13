package de.fraunhofer.aisec.codyze_core.config.options

import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.*
import de.fraunhofer.aisec.codyze_core.config.enums.Language
import java.io.File

class CPGOptions : OptionGroup(name = "CPG Options") {
    val unity: Boolean by
        option("--unity", help = "Enables unity builds (C++ only) for files in the path.")
            .flag("--no-unity", "--disable-unity", default = false)
    val typeSystemInFrontend: Boolean by
        option(
                "--type-system-in-frontend",
                "--disable-type-system-in-frontend",
                help =
                    "If deactivated, the type listener system starts after the frontends are done building the initial AST structure."
            )
            .flag(
                "--no-type-system-in-frontend",
                "--disable-type-system-in-frontend",
                default = true,
                defaultForHelp = "enable"
            )
    val debugParser: Boolean by
        option("--debug-parser", help = "Generate debug output for the cpg parser.")
            .flag("--no-debug-parser", default = false)
    val disableCleanup: Boolean by
        option(
                "--disable-cleanup",
                help =
                    "Switch off cleaning up TypeManager memory after the analysis. Use only for testing."
            )
            .flag("--no-disable-cleanup", "--enable-cleanup", default = false)
    val codeInNodes: Boolean by
        option(
                "--code-in-nodes",
                help = "Controls showing the code of a node as parameter in the node."
            )
            .flag(
                "--no-code-in-nodes",
                "--disable-code-in-nodes",
                default = true,
                defaultForHelp = "enable"
            )
    val annotations: Boolean by
        option(
                "--annotations",
                help = "Enables processing annotations or annotation-like elements."
            )
            .flag("--no-annotations", "--disable-annotations", default = false)
    val failOnError: Boolean by
        option(
                "--fail-on-error",
                help =
                    "Should the parser/translation fail on errors (enabled) or try to continue in a best-effort manner (disabled)."
            )
            .flag("--no-fail-on-error", "--disable-fail-on-error", default = false)
    val parallelFrontends: Boolean by
        option(
                "--parallel-frontends",
                help =
                    "Enables parsing the ASTs for the source files in parallel, but the passes afterwards will still run in a single thread."
            )
            .flag("--no-parallel-frontends", "--synchronous-frontends", default = false)
    val defaultPasses: Boolean by
        option("--default-passes", help = "Controls the usage of default passes for cpg.")
            .flag(
                "--no-default-passes",
                "--disable-default-passes",
                default = true,
                defaultForHelp = "enable"
            )

    val additionalLanguages: List<Language> by
        option(
                "--additional-languages",
                help =
                    "Enables the experimental support for additional languages (${Language.values()})." +
                        "Additional files need to be placed in certain locations. Please follow the CPG README."
            )
            .enum<Language>(ignoreCase = true)
            .multiple()
    val symbols: Map<String, String> by
        option("--symbols", help = "Definition of additional symbols.")
            .associate(delimiter = File.pathSeparator)
    val additionalSymbols: Map<String, String> by
        option(
                "--additional-symbols",
                help =
                    "See --symbols, but appends the values to the ones specified in configuration file."
            )
            .associate(delimiter = File.pathSeparator)
    val passes: Map<String, String> by
        option("--passes", help = "Definition of additional symbols.")
            .associate(delimiter = File.pathSeparator)
    val additionalPasses: Map<String, String> by
        option(
                "--additional-passes",
                help =
                    "See --symbols, but appends the values to the ones specified in configuration file."
            )
            .associate(delimiter = File.pathSeparator)
}
