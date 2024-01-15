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
package de.fraunhofer.aisec.codyze.cli

import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.path
import de.fraunhofer.aisec.codyze.core.config.Configuration
import de.fraunhofer.aisec.codyze.core.output.OutputBuilder
import de.fraunhofer.aisec.codyze.core.output.SarifBuilder
import org.koin.java.KoinJavaComponent.getKoin
import java.nio.file.Path
import kotlin.io.path.Path

@Suppress("UNUSED")
class CodyzeOptionGroup : OptionGroup(name = null) {
    val output: Path by option("-o", "--output", help = "Write results to file. Use - for stdout.")
        .path(mustBeWritable = true)
        .default(Path(System.getProperty("user.dir"), "findings.sarif"))

    val outputBuilder: OutputBuilder by option(
        "--output-format",
        help = "Format in which the analysis results are returned."
    )
        .choice(getKoin().getAll<OutputBuilder>().associateBy { it.cliName }, ignoreCase = true)
        .default(SarifBuilder(), defaultForHelp = "sarif")

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

    val pedantic: Boolean by option(
        "--pedantic",
        help =
        "Activates pedantic analysis mode. In this mode, Codyze analyzes all" +
            "MARK rules and report all findings. This option overrides `disabledMarkRules` and `noGoodFinding`" +
            "and ignores any Codyze source code comments."
    )
        .flag("--no-pedantic")

    fun asConfiguration() = Configuration(
        output = output,
        outputBuilder = outputBuilder,
        goodFindings = goodFindings,
        pedantic = pedantic
    )
}
