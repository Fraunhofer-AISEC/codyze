/*
 * Copyright (c) 2022, Fraunhofer AISEC. All rights reserved.
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
package de.fraunhofer.aisec.codyze.backends.cpg

import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.*
import de.fraunhofer.aisec.codyze.core.backend.BackendOptions
import de.fraunhofer.aisec.codyze.core.config.combineSources
import de.fraunhofer.aisec.codyze.core.config.resolvePaths
import de.fraunhofer.aisec.cpg.passes.Pass
import java.nio.file.Path

@Suppress("UNUSED")
class CPGOptionGroup : BackendOptions(helpName = "CPG Backend Options") {
    private val rawSource: List<Path> by option(
        "-s",
        "--source",
        "-ss",
        help = "Source files or folders to analyze."
    )
        .path(mustExist = true, mustBeReadable = true)
        .multiple(required = true)
    private val rawSourceAdditions: List<Path> by option(
        "--source-additions",
        help =
        "See --source, but appends the values to the ones specified in configuration file"
    )
        .path(mustExist = true, mustBeReadable = true)
        .multiple()
    private val rawDisabledSource: List<Path> by option(
        "--disabled-source",
        help = "Files or folders specified here will not be analyzed. " +
            "Symbolic links are not followed when filtering out these paths"
    )
        .path(mustExist = true, mustBeReadable = true)
        .multiple()
    private val rawDisabledSourceAdditions: List<Path> by option(
        "--disabled-source-additions",
        help =
        "See --disabled-sources, but appends the values to the ones specified in configuration file."
    )
        .path(mustExist = true, mustBeReadable = true)
        .multiple()

    /**
     * Lazy property that combines all given sources from the different options into a list of files
     * to analyze.
     */
    val source: List<Path> by lazy {
        resolvePaths(
            source = rawSource,
            sourceAdditions = rawSourceAdditions,
            disabledSource = rawDisabledSource,
            disabledSourceAdditions = rawDisabledSourceAdditions
        )
    }

    val useUnityBuild: Boolean by option(
        "--unity",
        help = "Enables unity builds (C++ only) for files in the path."
    )
        .flag("--no-unity", "--disable-unity", default = false)
    val typeSystemActiveInFrontend: Boolean by option(
        "--type-system-in-frontend",
        help = "If deactivated, the type listener system starts after the frontends " +
            "are done building the initial AST structure."
    )
        .flag(
            "--no-type-system-in-frontend",
            "--disable-type-system-in-frontend",
            default = true,
            defaultForHelp = "enable"
        )
    val debugParser: Boolean by option("--debug-parser", help = "Generate debug output for the cpg parser.")
        .flag("--no-debug-parser", default = false)
    val disableCleanup: Boolean by option(
        "--disable-cleanup",
        help =
        "Switch off cleaning up TypeManager memory after the analysis. Use only for testing."
    )
        .flag("--no-disable-cleanup", "--enable-cleanup", default = false)
    val codeInNodes: Boolean by option(
        "--code-in-nodes",
        help = "Controls showing the code of a node as parameter in the node."
    )
        .flag(
            "--no-code-in-nodes",
            "--disable-code-in-nodes",
            default = true,
            defaultForHelp = "enable"
        )
    val matchCommentsToNodes: Boolean by option(
        "--match-comments-to-nodes",
        help = "Controls whether the CPG frontend shall use a heuristic matching of comments " +
            "found in the source file to match them to the closest AST node and save it in the comment property."
    )
        .flag(
            "--no-match-comments-to-nodes",
            "--disable-match-comments-to-nodes",
            default = false,
        )
    val processAnnotations: Boolean by option(
        "--annotations",
        help = "Enables processing annotations or annotation-like elements."
    )
        .flag("--no-annotations", "--disable-annotations", default = false)
    val failOnError: Boolean by option(
        "--fail-on-error",
        help =
        "Should the parser/translation fail on errors (enabled) or try to continue in a best-effort manner (disabled)."
    )
        .flag("--no-fail-on-error", "--disable-fail-on-error", default = false)
    val useParallelFrontends: Boolean by option(
        "--parallel-frontends",
        help = "Enables parsing the ASTs for the source files in parallel, " +
            "but the passes afterwards will still run in a single thread."
    )
        .flag("--no-parallel-frontends", "--synchronous-frontends", default = false)
    val defaultPasses: Boolean by option(
        "--default-passes",
        help = "Controls the usage of default passes for cpg."
    )
        .flag(
            "--no-default-passes",
            "--disable-default-passes",
            default = true,
            defaultForHelp = "enable"
        )

    val additionalLanguages: Set<String> by option(
        "--additional-language",
        help = "Add an additional language frontend by its fully qualified class name (FQN). " +
            "You must make sure that the class is available on the class path."
    )
        .multiple()
        .unique()

    private val rawSymbols: Map<String, String> by option(
        "--symbols",
        help = "Definition of additional symbols."
    ).associate()
    private val rawSymbolsAdditions: Map<String, String> by option(
        "--symbols-additions",
        help =
        "See --symbols, but appends the values to the ones specified in configuration file."
    )
        .associate()

    /** Lazy property that combines all symbols from the different options into a single map. */
    val symbols: Map<String, String> by lazy {
        resolveSymbols(
            symbols = rawSymbols,
            additionalSymbols = rawSymbolsAdditions
        )
    }

    private val rawPasses: List<Pass> by option("--passes", help = "Definition of additional symbols.")
        .convert { convertPass(it) }
        .multiple()
    private val rawPassesAdditions: List<Pass> by option(
        "--passes-additions",
        help =
        "See --passes, but appends the values to the ones specified in configuration file."
    )
        .convert { convertPass(it) }
        .multiple()

    /** Lazy property that combines all symbols from the different options into a single map. */
    val passes: List<Pass> by lazy { resolvePasses(passes = rawPasses, additionalPasses = rawPassesAdditions) }

    val loadIncludes: Boolean by option(
        "--analyze-includes",
        help =
        "Enables parsing of include files. By default, if --includes are given,\n" +
            "the parser will resolve symbols/templates from these include, but\n" +
            "not load their parse tree. This will enforced to true, if unity\n" +
            "builds are used."
    )
        .flag("--no-analyze-includes", "--disable-analyze-includes", default = false)

    private val rawIncludes: List<Path> by option("--includes", help = "Path(s) containing include files.")
        .path(mustExist = true, mustBeReadable = true)
        .multiple()
    private val rawIncludeAdditions: List<Path> by option(
        "--include-additions",
        help =
        "See --includes, but appends the values to the ones specified in configuration file."
    )
        .path(mustExist = true, mustBeReadable = true)
        .multiple()
    private val rawEnabledIncludes: List<Path> by option(
        "--enabled-includes",
        help =
        "If includes is not empty, only the specified files will be parsed and\n" +
            "processed in the cpg, unless it is a part of the disabled list, in\n" +
            "which it will be ignored."
    )
        .path(mustExist = true, mustBeReadable = true)
        .multiple()
        .validate {
            if (it.isNotEmpty()) {
                require(rawIncludes.isNotEmpty() or rawIncludeAdditions.isNotEmpty()) {
                    "--enabled-includes can only be used when includes are given."
                }
            }
        }
    private val rawEnabledIncludesAdditions: List<Path> by option(
        "--enabled-includes-additions",
        help =
        "See --enabled-includes, but appends the values to the ones specified in configuration file."
    )
        .path(mustExist = true, mustBeReadable = true)
        .multiple()
        .validate {
            if (it.isNotEmpty()) {
                require(rawIncludes.isNotEmpty() or rawIncludeAdditions.isNotEmpty()) {
                    "--enabled-includes-additions can only be used when includes are given."
                }
            }
        }
    private val rawDisabledIncludes: List<Path> by option(
        "--disabled-includes",
        help =
        "If includes is not empty, the specified files will be excluded from\n" +
            "being parsed and processed in the cpg. The disabled list entries\n" +
            "always take priority over the enabled list entries."
    )
        .path(mustExist = true, mustBeReadable = true)
        .multiple()
        .validate {
            if (it.isNotEmpty()) {
                require(rawIncludes.isNotEmpty() or rawIncludeAdditions.isNotEmpty()) {
                    "--disabled-includes can only be used when includes are given."
                }
            }
        }
    private val rawDisabledIncludesAdditions: List<Path> by option(
        "--disabled-includes-additions",
        help =
        "See --disabled-includes, but appends the values to the ones specified in configuration file."
    )
        .path(mustExist = true, mustBeReadable = true)
        .multiple()
        .validate {
            if (it.isNotEmpty()) {
                require(rawIncludes.isNotEmpty() or rawIncludeAdditions.isNotEmpty()) {
                    "--disabled-includes-additions can only be used when includes are given."
                }
            }
        }

    val includePaths: List<Path> by lazy { combineSources(rawIncludes, rawIncludeAdditions).toList() }

    val includeAllowlist: List<Path> by lazy {
        combineSources(rawEnabledIncludes, rawEnabledIncludesAdditions).toList()
    }

    val includeBlocklist: List<Path> by lazy {
        combineSources(rawDisabledIncludes, rawDisabledIncludesAdditions).toList()
    }

    private fun resolveSymbols(
        symbols: Map<String, String>,
        additionalSymbols: Map<String, String>
    ): Map<String, String> {
        return symbols + additionalSymbols
    }

    private fun resolvePasses(passes: List<Pass>, additionalPasses: List<Pass>): List<Pass> {
        return passes + additionalPasses
    }

    @Suppress("SwallowedException", "ThrowsCount")
    private fun convertPass(className: String) =
        try {
            val clazz = Class.forName(className)
            if (Pass::class.java.isAssignableFrom(clazz)) {
                // TODO: use 'isSubtypeOf' ?
                clazz.getDeclaredConstructor().newInstance() as Pass
            } else {
                throw ReflectiveOperationException("$className is not a CPG Pass")
            }
        } catch (e: InstantiationException) {
            throw InstantiationException("$className cannot be instantiated")
        } catch (e: ClassNotFoundException) {
            throw ClassNotFoundException("$className is not a known class", e)
        }
}
