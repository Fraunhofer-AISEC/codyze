package de.fraunhofer.aisec.codyze.config

import com.fasterxml.jackson.annotation.JsonProperty
import de.fraunhofer.aisec.codyze.analysis.TypestateMode
import java.io.File
import picocli.CommandLine
import picocli.CommandLine.ArgGroup

class CodyzeConfiguration {

    // TODO: names
    @ArgGroup(exclusive = true, multiplicity = "1", heading = "Execution Mode\n")
    var executionMode: ExecutionMode = ExecutionMode()

    @ArgGroup(exclusive = false, heading = "Analysis Options\n") var analysis = AnalysisMode()

    @CommandLine.Option(
        names = ["-s", "--source"],
        paramLabel = "<path>",
        description = ["Source file or folder to analyze."]
    )
    var source: File? = null

    @CommandLine.Option(
        names = ["-m", "--mark"],
        paramLabel = "<path>",
        description = ["Loads MARK policy files\n\t(Default: \${DEFAULT-VALUE})"],
        split = ","
    )
    var mark = arrayOf(File("./"))

    // TODO output standard stdout?
    @CommandLine.Option(
        names = ["-o", "--output"],
        paramLabel = "<file>",
        description = ["Write results to file. Use - for stdout.\n\t(Default: \${DEFAULT-VALUE})"]
    )
    var output = "findings.sarif"

    @CommandLine.Option(
        names = ["--timeout"],
        paramLabel = "<minutes>",
        description = ["Terminate analysis after timeout\n\t(Default: \${DEFAULT-VALUE})"]
    )
    var timeout = 120L

    @CommandLine.Option(
        names = ["--no-good-findings"],
        description =
            [
                "Disable output of \"positive\" findings which indicate correct implementations\n\t(Default: \${DEFAULT-VALUE})"]
    )
    var noGoodFindings = false

    @CommandLine.Option(names = ["--sarif"], description = ["Enables the SARIF output."])
    var sarifOutput: Boolean = false

    //    @JsonIgnore
    //    fun setExecutionMode(cli: Boolean, lsp: Boolean, tui: Boolean) {
    //        if (cli xor lsp xor tui && !(cli && lsp && tui)) {
    //            executionMode.cli = cli
    //            executionMode.lsp = lsp
    //            executionMode.tui = tui
    //        }
    //    }
    //
    //    fun isCli(): Boolean {
    //        return executionMode.cli
    //    }
    //
    //    fun isTui(): Boolean {
    //        return executionMode.tui
    //    }
    //
    //    fun isLsp(): Boolean {
    //        return executionMode.lsp
    //    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as CodyzeConfiguration
        return (sarifOutput == that.sarifOutput &&
            executionMode == that.executionMode &&
            analysis == that.analysis &&
            source == that.source)
    }
}

/**
 * Codyze runs in any of three modes:
 *
 * CLI: Non-interactive command line client. Accepts arguments from command line and runs analysis.
 *
 * LSP: Bind to stdout as a server for Language Server Protocol (LSP). This mode is for IDE support.
 *
 * TUI: The text based user interface (TUI) is an interactive console that allows exploring the
 * analyzed source code by manual queries.
 */
class ExecutionMode {
    @CommandLine.Option(
        names = ["-c"],
        required = true,
        description = ["Start in command line mode."]
    )
    var isCli = false

    @CommandLine.Option(
        names = ["-l"],
        required = true,
        description = ["Start in language server protocol (LSP) mode."]
    )
    var isLsp = false

    @CommandLine.Option(
        names = ["-t"],
        required = true,
        description = ["Start interactive console (Text-based User Interface)."]
    )
    var isTui = false

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as ExecutionMode
        return isCli == that.isCli && isLsp == that.isLsp && isTui == that.isTui
    }
}

class AnalysisMode {

    @JsonProperty("typestate")
    @CommandLine.Option(
        names = ["--typestate"],
        paramLabel = "<NFA|WPDS>",
        type = [TypestateMode::class],
        description =
            [
                "Typestate analysis mode\nNFA:  Non-deterministic finite automaton (faster, intraprocedural)\nWPDS: Weighted pushdown system (slower, interprocedural)\n\t(Default: \${DEFAULT-VALUE})"]
    )
    var tsMode = TypestateMode.NFA

    override fun equals(o: Any?): Boolean {
        return !(o == null || javaClass != o.javaClass)
    }
}
