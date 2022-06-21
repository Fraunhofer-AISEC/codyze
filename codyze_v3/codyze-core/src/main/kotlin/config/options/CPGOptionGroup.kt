package de.fraunhofer.aisec.codyze_core.config.options

import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.*
import de.fraunhofer.aisec.codyze_core.config.enums.Language
import de.fraunhofer.aisec.cpg.passes.Pass
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
    internal val rawSymbols: Map<String, String> by
        option("--symbols", help = "Definition of additional symbols.")
            .associate(delimiter = File.pathSeparator)
    internal val rawAdditionalSymbols: Map<String, String> by
        option(
                "--additional-symbols",
                help =
                    "See --symbols, but appends the values to the ones specified in configuration file."
            )
            .associate(delimiter = File.pathSeparator)
    /**
     * Lazy property that combines all symbols from the different options into a single map.
     */
    val symbols by lazy {
        resolveSymbols(symbols = rawSymbols, additionalSymbols = rawAdditionalSymbols)
    }

    internal val rawPasses: List<Pass> by
        option("--passes", help = "Definition of additional symbols.")
            .convert { convertPass(it) }
            .split(delimiter = File.pathSeparator).default(emptyList())
    internal val rawPassesAdditions: List<Pass> by
        option(
                "--passes-additions",
                help =
                    "See --passes, but appends the values to the ones specified in configuration file."
            )
            .convert { convertPass(it) }
            .split(delimiter = File.pathSeparator).default(emptyList())
    /**
     * Lazy property that combines all symbols from the different options into a single map.
     */
    val passes by lazy {
         resolvePasses(passes = rawPasses, additionalPasses = rawPassesAdditions)
    }

    // TODO
    private fun resolveSymbols(symbols: Map<String, String>, additionalSymbols: Map<String, String>): Map<String, String> = TODO()

    // TODO
    private fun resolvePasses(passes: List<Pass>, additionalPasses: List<Pass>): List<Pass> {
        return passes + additionalPasses
    }

    private fun convertPass(className: String) : Pass{
        try {
            val clazz = Class.forName(className)
            if (Pass::class.java.isAssignableFrom(clazz))
                return clazz.getDeclaredConstructor().newInstance() as Pass
            else throw ReflectiveOperationException("$className is not a CPG Pass")
        } catch (e: InstantiationException) {
            throw InstantiationException("$className cannot be instantiated")
        } catch (e: ClassNotFoundException) {
            throw ClassNotFoundException("$className is not a known class", e)
        }
    }
}
