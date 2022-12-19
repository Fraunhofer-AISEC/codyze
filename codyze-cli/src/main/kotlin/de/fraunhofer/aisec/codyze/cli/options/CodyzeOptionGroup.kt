package de.fraunhofer.aisec.codyze.cli.options

import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.path
import de.fraunhofer.aisec.codyze.core.config.Configuration
import java.nio.file.Path
import kotlin.io.path.Path

@Suppress("UNUSED")
class CodyzeOptionGroup : OptionGroup(name = null) {
    private val output: Path by option("-o", "--output", help = "Write results to file. Use - for stdout.")
        .path(mustBeWritable = true)
        .default(Path(System.getProperty("user.dir"), "findings.sarif"))

    private val goodFindings: Boolean by option(
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

    private val pedantic: Boolean by option(
        "--pedantic",
        help =
        "Activates pedantic analysis mode. In this mode, Codyze analyzes all" +
            "MARK rules and report all findings. This option overrides `disabledMarkRules` and `noGoodFinding`" +
            "and ignores any Codyze source code comments."
    )
        .flag("--no-pedantic")

    fun asConfiguration() = Configuration(
        output = output,
        goodFindings = goodFindings,
        pedantic = pedantic
    )
}