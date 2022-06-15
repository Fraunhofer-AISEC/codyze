package de.fraunhofer.aisec.codyze_core.config.options

import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.*
import java.nio.file.Path

class TranslationOptions : OptionGroup(name = "Translation Options") {
    val analyzeIncludes: Boolean by
        option(
                "--analyze-includes",
                help =
                    "Enables parsing of include files. By default, if --includes are given,\n" +
                        "the parser will resolve symbols/templates from these include, but\n" +
                        "not load their parse tree. This will enforced to true, if unity\n" +
                        "builds are used."
            )
            .flag("--no-analyze-includes", "--disable-analyze-includes", default = false)

    internal val rawIncludes: List<Path> by
        option("--includes", help = "Path(s) containing include files.")
            .path(mustExist = true, mustBeReadable = true)
            .multiple()
    internal val rawIncludeAdditions: List<Path> by
        option(
                "--include-additions",
                help =
                    "See --includes, but appends the values to the ones specified in configuration file."
            )
            .path(mustExist = true, mustBeReadable = true)
            .multiple()
    internal val rawEnabledIncludes: List<Path> by
        option(
                "--enabled-includes",
                help =
                    "If includes is not empty, only the specified files will be parsed and\n" +
                        "processed in the cpg, unless it is a part of the disabled list, in\n" +
                        "which it will be ignored."
            )
            .path(mustExist = true, mustBeReadable = true)
            .multiple()
            .validate {
                if (it.isNotEmpty()) require( rawIncludes.isNotEmpty() or rawIncludeAdditions.isNotEmpty()) {
                        "--enabled-includes can only be used when includes are given."
                    }
            }
    internal val rawEnabledIncludesAdditions: List<Path> by
        option(
                "--enabled-includes-additions",
                help =
                    "See --enabled-includes, but appends the values to the ones specified in configuration file."
            )
            .path(mustExist = true, mustBeReadable = true)
            .multiple()
            .validate {
                if (it.isNotEmpty()) require( rawIncludes.isNotEmpty() or rawIncludeAdditions.isNotEmpty()) {
                    "--enabled-includes-additions can only be used when includes are given."
                }
            }
    internal val rawDisabledIncludes: List<Path> by
        option(
                "--disabled-includes",
                help =
                    "If includes is not empty, the specified files will be excluded from\n" +
                        "being parsed and processed in the cpg. The disabled list entries\n" +
                        "always take priority over the enabled list entries."
            )
            .path(mustExist = true, mustBeReadable = true)
            .multiple()
            .validate {
                if (it.isNotEmpty()) require( rawIncludes.isNotEmpty() or rawIncludeAdditions.isNotEmpty()) {
                    "--diabled-includes can only be used when includes are given."
                }
            }
    internal val rawDisabledIncludesAdditions: List<Path> by
        option(
                "--disabled-includes-additions",
                help =
                    "See --disabled-includes, but appends the values to the ones specified in configuration file."
            )
            .path(mustExist = true, mustBeReadable = true)
            .multiple()
            .validate {
                if (it.isNotEmpty()) require( rawIncludes.isNotEmpty() or rawIncludeAdditions.isNotEmpty()) {
                    "--disabled-includes-additions can only be used when includes are given."
                }
            }

    val includes by lazy {
        resolveIncludes(includes = rawIncludes, includeAdditions = rawIncludeAdditions, enabledIncludes = rawEnabledIncludes, enabledIncludeAdditions = rawEnabledIncludesAdditions, disabledIncludes = rawDisabledIncludes, disabledIncludeAdditions = rawDisabledIncludesAdditions)
    }

    // TODO: write tests
    private fun resolveIncludes(includes: List<Path>, includeAdditions: List<Path>, enabledIncludes: List<Path>, enabledIncludeAdditions: List<Path>, disabledIncludes: List<Path>, disabledIncludeAdditions: List<Path>): List<Path> {
        val allIncludes = combineSources(includes, includeAdditions)
        val allEnabledIncludes = combineSources(enabledIncludes, enabledIncludeAdditions)
        val allDisabledIncludes = combineSources(disabledIncludes, disabledIncludeAdditions)

        return ((allIncludes intersect allEnabledIncludes) - allDisabledIncludes).toList()
    }
}
