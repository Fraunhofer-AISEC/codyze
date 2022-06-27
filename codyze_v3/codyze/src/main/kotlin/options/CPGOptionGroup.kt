package de.fraunhofer.aisec.codyze.options

import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.*
import de.fraunhofer.aisec.codyze_core.config.Language
import de.fraunhofer.aisec.cpg.passes.Pass
import java.io.File

@Suppress("UNUSED")
class CPGOptions : OptionGroup(name = "CPG Options") {
    val useUnityBuild: Boolean by
    option("--unity", help = "Enables unity builds (C++ only) for files in the path.")
        .flag("--no-unity", "--disable-unity", default = false)
        .also { ConfigurationRegister.addOption("useUnityBuild", it) }
    val typeSystemActiveInFrontend: Boolean by
    option(
        "--type-system-in-frontend",
        help =
        "If deactivated, the type listener system starts after the frontends are done building the initial AST structure."
    )
        .flag(
            "--no-type-system-in-frontend",
            "--disable-type-system-in-frontend",
            default = true,
            defaultForHelp = "enable"
        )
        .also { ConfigurationRegister.addOption("typeSystemActiveInFrontend", it) }
    val debugParser: Boolean by
    option("--debug-parser", help = "Generate debug output for the cpg parser.")
        .flag("--no-debug-parser", default = false)
        .also { ConfigurationRegister.addOption("debugParser", it) }
    val disableCleanup: Boolean by
    option(
        "--disable-cleanup",
        help =
        "Switch off cleaning up TypeManager memory after the analysis. Use only for testing."
    )
        .flag("--no-disable-cleanup", "--enable-cleanup", default = false)
        .also { ConfigurationRegister.addOption("disableCleanup", it) }
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
        .also { ConfigurationRegister.addOption("codeInNodes", it) }
    val matchCommentsToNodes: Boolean by
    option(
        "--match-comments-to-nodes",
        help =
        "Controls whether the CPG frontend shall use a heuristic matching of comments found in the source file to match them to the closest AST node and save it in the comment property."
    )
        .flag(
            "--no-match-comments-to-nodes",
            "--disable-match-comments-to-nodes",
            default = false,
        )
        .also { ConfigurationRegister.addOption("matchCommentsToNodes", it) }
    val processAnnotations: Boolean by
    option(
        "--annotations",
        help = "Enables processing annotations or annotation-like elements."
    )
        .flag("--no-annotations", "--disable-annotations", default = false)
        .also { ConfigurationRegister.addOption("processAnnotations", it) }
    val failOnError: Boolean by
    option(
        "--fail-on-error",
        help =
        "Should the parser/translation fail on errors (enabled) or try to continue in a best-effort manner (disabled)."
    )
        .flag("--no-fail-on-error", "--disable-fail-on-error", default = false)
        .also { ConfigurationRegister.addOption("failOnError", it) }
    val useParallelFrontends: Boolean by
    option(
        "--parallel-frontends",
        help =
        "Enables parsing the ASTs for the source files in parallel, but the passes afterwards will still run in a single thread."
    )
        .flag("--no-parallel-frontends", "--synchronous-frontends", default = false)
        .also { ConfigurationRegister.addOption("useParallelFrontends", it) }
    val defaultPasses: Boolean by
    option("--default-passes", help = "Controls the usage of default passes for cpg.")
        .flag(
            "--no-default-passes",
            "--disable-default-passes",
            default = true,
            defaultForHelp = "enable"
        )
        .also { ConfigurationRegister.addOption("defaultPasses", it) }

    val additionalLanguages: List<Language> by
    option(
        "--additional-languages",
        help =
        "Enables the experimental support for additional languages (${Language.values().joinToString(", ")}). " +
                "Additional files need to be placed in certain locations. Please follow the CPG README."
    )
        .enum<Language>(ignoreCase = true)
        .multiple()
        .also { ConfigurationRegister.addOption("additionalLanguages", it) }

    private val rawSymbols: Map<String, String> by
    option("--symbols", help = "Definition of additional symbols.")
        .associate(delimiter = File.pathSeparator)
    private val rawSymbolsAdditions: Map<String, String> by
    option(
        "--symbols-additions",
        help =
        "See --symbols, but appends the values to the ones specified in configuration file."
    )
        .associate(delimiter = File.pathSeparator)
    /** Lazy property that combines all symbols from the different options into a single map. */
    val symbols: Map<String, String> by
    lazy { resolveSymbols(symbols = rawSymbols, additionalSymbols = rawSymbolsAdditions) }
        .also {
            ConfigurationRegister.addLazy(
                name = "symbols",
                lazyProperty = it,
                thisRef = this,
                property = ::symbols
            )
        }

    private val rawPasses: List<Pass> by
    option("--passes", help = "Definition of additional symbols.")
        .convert { convertPass(it) }
        .multiple()
    private val rawPassesAdditions: List<Pass> by
    option(
        "--passes-additions",
        help =
        "See --passes, but appends the values to the ones specified in configuration file."
    )
        .convert { convertPass(it) }
        .multiple()
    /** Lazy property that combines all symbols from the different options into a single map. */
    val passes: List<Pass> by
    lazy { resolvePasses(passes = rawPasses, additionalPasses = rawPassesAdditions) }
        .also {
            ConfigurationRegister.addLazy(
                name = "passes",
                lazyProperty = it,
                thisRef = this,
                property = ::passes
            )
        }

    // TODO
    private fun resolveSymbols(
        symbols: Map<String, String>,
        additionalSymbols: Map<String, String>
    ): Map<String, String> {
        return symbols + additionalSymbols
    }

    // TODO
    private fun resolvePasses(passes: List<Pass>, additionalPasses: List<Pass>): List<Pass> {
        return passes + additionalPasses
    }

    private fun convertPass(className: String): Pass {
        try {
            val clazz = Class.forName(className)
            if (Pass::class.java.isAssignableFrom(clazz)) // TODO: use 'isSubtypeOf' ?
                return clazz.getDeclaredConstructor().newInstance() as Pass
            else throw ReflectiveOperationException("$className is not a CPG Pass")
        } catch (e: InstantiationException) {
            throw InstantiationException("$className cannot be instantiated")
        } catch (e: ClassNotFoundException) {
            throw ClassNotFoundException("$className is not a known class", e)
        }
    }
}
