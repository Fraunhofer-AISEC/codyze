package de.fraunhofer.aisec.codyze.config

import com.fasterxml.jackson.annotation.JsonProperty
import de.fraunhofer.aisec.codyze.analysis.TypestateMode
import java.io.File
import picocli.CommandLine.Option

class CodyzeConfiguration {

    // TODO: names
    val analysis = AnalysisMode()

    @Option(
        names = ["-m", "--mark"],
        paramLabel = "<path>",
        description = ["Loads MARK policy files.\n\t(Default: \${DEFAULT-VALUE})"],
        split = ","
    )
    var mark: Array<File> = arrayOf(File("./"))

    // TODO: change name or make into warning levels
    @Option(
        names = ["--no-good-findings"],
        description =
            [
                "Disable output of \"positive\" findings which indicate correct implementations\n" +
                    "\t(Default: \${DEFAULT-VALUE})"],
        fallbackValue = "true"
    )
    var noGoodFindings = false

    @Option(
        names = ["--disabled-mark-rules"],
        paramLabel = "<package>.<rule>",
        description =
            [
                "The specified mark rules will be excluded from being parsed and processed. The rule has to be specified by its fully qualified name. If there is no package name, specify rule as \".<rule>\". Use \'<package>.*\' to disable an entire package."],
        split = ","
    )
    var disabledMarkRules: List<String> = emptyList()

    @Option(
        names = ["--pedantic"],
        description =
            [
                "Activates pedantic analysis mode. In this mode, Codyze analyzes all MARK rules and report all findings. This option overrides `disabledMarkRules` and `noGoodFinding` and ignores any Codyze source code comments."]
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
                    "\t(Default: \${DEFAULT-VALUE})"]
    )
    var tsMode = TypestateMode.DFA
}
