package de.fraunhofer.aisec.codyze.config

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import de.fraunhofer.aisec.codyze.analysis.TypestateMode
import java.io.File
import picocli.CommandLine.ArgGroup
import picocli.CommandLine.Option

class CodyzeConfiguration {

    // TODO: names
    val analysis = AnalysisMode()

    @JsonIgnore @ArgGroup(exclusive = true) val markCLI = MarkArgGroup()
    var mark: Array<File> = arrayOf(File("./"))

    // TODO: change name or make into warning levels
    @Option(
        names = ["--no-good-findings"],
        description =
            [
                "Disable output of \"positive\" findings which indicate correct implementations.\n" +
                    "\t(Default: \${DEFAULT-VALUE})"
            ],
        fallbackValue = "true"
    )
    var noGoodFindings = false

    @JsonIgnore @ArgGroup(exclusive = true) val disabledMarkRulesCLI = DisabledMarkArgGroup()
    var disabledMarkRules: List<String> = emptyList()

    @Option(
        names = ["--pedantic"],
        description =
            [
                "Activates pedantic analysis mode. In this mode, Codyze analyzes all MARK rules and report all findings. This option overrides `disabledMarkRules` and `noGoodFinding` and ignores any Codyze source code comments."
            ]
    )
    var pedantic = false
}

/**
 * Codyze offers two modes af analyzing typestates:
 * - DFA: Based on a deterministic finite automaton (faster, intraprocedural)
 * - WPDS: Based on a weighted pushdown system (slower, interprocedural)
 */
class AnalysisMode {

    @JsonProperty("typestate")
    @Option(
        names = ["--typestate"],
        paramLabel = "<DFA|WPDS>",
        type = [TypestateMode::class],
        description =
            [
                "Typestate analysis mode.\n" +
                    "DFA:  Deterministic finite automaton (faster, intraprocedural)\n" +
                    "WPDS: Weighted pushdown system (slower, interprocedural)\n" +
                    "\t(Default: \${DEFAULT-VALUE})"
            ]
    )
    var tsMode = TypestateMode.DFA
}

class MarkArgGroup {
    var append = false
    var matched = false

    var mark: Array<File> = emptyArray()

    @Option(
        names = ["-m", "--mark"],
        paramLabel = "<path>",
        description = ["Loads MARK policy files.\n\t(Default: \${sys:mark})"],
        split = "\${sys:path.separator}"
    )
    fun match(value: Array<File>) {
        matched = true
        this.mark = value
    }

    @Option(
        names = ["--mark+"],
        paramLabel = "<path>",
        description =
            ["See --mark, but appends the values to the ones specified in configuration file."],
        split = "\${sys:path.separator}"
    )
    fun append(value: Array<File>) {
        append = true
        match(value)
    }
}

class DisabledMarkArgGroup {
    var append = false
    var matched = false

    var disabledMarkRules: List<String> = emptyList()
    @Option(
        names = ["--disabled-mark-rules"],
        paramLabel = "<package>.<rule>",
        description =
            [
                "The specified mark rules will be excluded from being parsed and processed. The rule has to be specified by its fully qualified name. If there is no package name, specify rule as \".<rule>\". Use \"<package>.*\" to disable an entire package."
            ],
        split = "\${sys:path.separator}"
    )
    fun match(value: List<String>) {
        matched = true
        this.disabledMarkRules = value
    }

    @Option(
        names = ["--disabled-mark-rules+"],
        paramLabel = "<package>.<rule>",
        description =
            [
                "See --disabled-mark-rules, but appends the values to the ones specified in configuration file."
            ],
        split = "\${sys:path.separator}"
    )
    fun append(value: List<String>) {
        append = true
        match(value)
    }
}
