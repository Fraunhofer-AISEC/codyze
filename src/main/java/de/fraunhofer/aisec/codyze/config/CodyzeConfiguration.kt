package de.fraunhofer.aisec.codyze.config

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import de.fraunhofer.aisec.codyze.analysis.TypestateMode
import java.io.File
import picocli.CommandLine.ArgGroup
import picocli.CommandLine.Option

class CodyzeConfiguration {

    // TODO: names
    @JsonIgnore
    @ArgGroup(exclusive = true, multiplicity = "1", heading = "Execution Mode\n")
    val executionMode: ExecutionMode = ExecutionMode()

    val analysis = AnalysisMode()

    @Option(
        names = ["-s", "--source"],
        paramLabel = "<path>",
        description = ["Source file or folder to analyze."]
    )
    var source: File? = null

    @Option(
        names = ["-m", "--mark"],
        paramLabel = "<path>",
        description = ["Loads MARK policy files.\n\t(Default: \${DEFAULT-VALUE})"],
        split = ","
    )
    var mark: Array<File> = arrayOf(File("./"))

    // TODO output standard stdout?
    @Option(
        names = ["-o", "--output"],
        paramLabel = "<file>",
        description = ["Write results to file. Use - for stdout.\n\t(Default: \${DEFAULT-VALUE})"]
    )
    var output = "findings.sarif"

    @Option(
        names = ["--timeout"],
        paramLabel = "<minutes>",
        description = ["Terminate analysis after timeout.\n\t(Default: \${DEFAULT-VALUE})"]
    )
    var timeout = 120L

    @Option(
        names = ["--no-good-findings"],
        description =
            [
                "Disable output of \"positive\" findings which indicate correct implementations\n" +
                    "\t(Default: \${DEFAULT-VALUE})"],
        fallbackValue = "true"
    )
    var noGoodFindings = false

    @JsonProperty("sarif")
    @Option(
        names = ["--sarif"],
        description = ["Enables the SARIF output."],
        fallbackValue = "true"
    )
    var sarifOutput: Boolean = false

    @Option(
        names = ["--disabled-mark-rules"],
        paramLabel = "<package.rule>",
        description =
            [
                "The specified mark rules will be excluded from being parsed and processed. The rule has to be specified by its fully qualified name (package.rule). If there is no package name, specify rule as \".rule\". Use \'*\' to disable an entire package."],
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
 * Codyze runs in any of three modes:
 * - CLI: Non-interactive command line client. Accepts arguments from command line and runs
 * analysis.
 * - LSP: Bind to stdout as a server for Language Server Protocol (LSP). This mode is for IDE
 * support.
 * - TUI: The text based user interface (TUI) is an interactive console that allows exploring the
 * analyzed source code by manual queries.
 */
class ExecutionMode {
    @Option(names = ["-c"], required = true, description = ["Start in command line mode."])
    var isCli = false

    @Option(
        names = ["-l"],
        required = true,
        description = ["Start in language server protocol (LSP) mode."]
    )
    var isLsp = false

    @Option(
        names = ["-t"],
        required = true,
        description = ["Start interactive console (Text-based User Interface)."]
    )
    var isTui = false
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
