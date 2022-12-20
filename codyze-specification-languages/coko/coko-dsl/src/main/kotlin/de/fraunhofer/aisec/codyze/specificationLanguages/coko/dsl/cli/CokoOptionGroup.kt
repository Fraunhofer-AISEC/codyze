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
package de.fraunhofer.aisec.codyze.specificationLanguages.coko.dsl.cli

import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.path
import de.fraunhofer.aisec.codyze.core.config.resolvePaths
import de.fraunhofer.aisec.codyze.core.executor.ExecutorOptions
import mu.KotlinLogging
import java.nio.file.Path

private val logger = KotlinLogging.logger {}

@Suppress("UNUSED")
class CokoOptionGroup : ExecutorOptions("Coko Options") {
    private val rawSpec: List<Path> by option("--spec", help = "Loads the given specification files.")
        .path(mustExist = true, mustBeReadable = true, canBeDir = true)
        .multiple(required = true)
    private val rawSpecAdditions: List<Path> by option(
        "--spec-additions",
        help = "See --spec, but appends the values to the ones specified in configuration file."
    )
        .path(mustExist = true, mustBeReadable = true, canBeDir = true)
        .multiple()
    private val rawDisabledSpec: List<Path> by option(
        "--disabled-specs",
        help = "The specified files will be excluded from being parsed and" +
            "processed. The rule has to be specified by its fully qualified name." +
            "If there is no package name, specify rule as \".<rule>\". Use" +
            "\"<package>.*\" to disable an entire package."
    )
        .path(mustExist = true, mustBeReadable = true, canBeDir = true)
        .multiple()
    private val rawDisabledSpecAdditions: List<Path> by option(
        "--disabled-spec-additions",
        help = "See --disabled-specs, but appends the values to the ones specified in configuration file."
    )
        .path(mustExist = true, mustBeReadable = true, canBeDir = true)
        .multiple()

    val disabledSpecRules: List<String> by option(
        "--disabled-spec-rules",
        help = "Rules that will be ignored by the analysis."
    )
        .multiple()

    /**
     * Lazy property that combines all given specs from the different options into a list of spec
     * files to use.
     */
    val spec: List<Path> by lazy {
        validateSpec(
            resolvePaths(
                source = rawSpec,
                sourceAdditions = rawSpecAdditions,
                disabledSource = rawDisabledSpec,
                disabledSourceAdditions = rawDisabledSpecAdditions
            )
        )
    }
}
