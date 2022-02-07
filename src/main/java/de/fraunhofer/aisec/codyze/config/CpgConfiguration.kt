package de.fraunhofer.aisec.codyze.config

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import de.fraunhofer.aisec.cpg.passes.Pass
import java.io.File
import java.util.*
import picocli.CommandLine.Option

/** This class consists of configuration settings for CPG. */
class CpgConfiguration {

    val translation: TranslationSettings = TranslationSettings()

    @Option(
        names = ["--additional-languages"],
        split = ",",
        paramLabel = "language",
        description =
            [
                "Enables the experimental support for additional languages (currently \${COMPLETION-CANDIDATES}). Additional files need to be placed in certain locations. Please follow the CPG README."]
    )
    @JsonDeserialize(using = LanguageDeserializer::class)
    var additionalLanguages: Set<Language> = EnumSet.noneOf(Language::class.java)

    // TODO: maybe change to enum set instead of booleans for each language
    @JsonIgnore
    @Option(
        names = ["--enable-python-support"],
        description =
            [
                "Enables the experimental Python support. Additional files need to be placed in certain locations. Please follow the CPG README."]
    )
    var enablePython = false

    @JsonIgnore
    @Option(
        names = ["--enable-go-support"],
        description =
            [
                "Enables the experimental Go support. Additional files need to be placed in certain locations. Please follow the CPG README."]
    )
    var enableGo = false

    @set:JsonProperty("unity")
    @Option(
        names = ["--unity"],
        description = ["Enables unity builds (C++ only) for files in the path."],
        fallbackValue = "true"
    )
    var useUnityBuild = false

    // TODO: name!!!!!
    @Option(
        names = ["--no-type-system-in-frontend"],
        negatable = true,
        description =
            [
                """If false, type listener is only activated after the frontends are done building the initial AST structure.
	(Default: ${"$"}{DEFAULT-VALUE})"""]
    )
    var typeSystemActiveInFrontend = true

    // TODO: default value true or false?
    @Option(
        names = ["--no-default-passes"],
        negatable = true,
        description = ["Enables default passes for cpg.\n\t(Default: \${DEFAULT-VALUE})"]
    )
    var defaultPasses = true

    @Option(
        names = ["--debug-parser"],
        description = ["Enables debug output generation for the cpg parser"]
    )
    var debugParser = false

    @Option(
        names = ["--no-cleanup"],
        description =
            [
                "Switch off cleaning up TypeManager memory after the analysis. " +
                    "Set to true only for testing"]
    )
    var disableCleanup = false

    @Option(
        names = ["--code-in-nodes"],
        negatable = true,
        description = ["Enables showing the code of a node as parameter in the node"]
    )
    var codeInNodes = true

    @Option(
        names = ["--annotations"],
        description = ["Enables processing annotations or annotation-like elements"]
    )
    var processAnnotations = false

    @Option(
        names = ["--fail-on-error"],
        description =
            [
                "Should the parser/translation fail on errors (true) " +
                    "or try to continue in a best-effort manner (false, default)"]
    )
    var failOnError = false

    @Option(
        names = ["--symbols"],
        paramLabel = "<symbol=definition>",
        description = ["Definition of additional symbols"],
        split = ","
    )
    var symbols: Map<String, String> = HashMap()

    @Option(
        names = ["--parallel-frontends"],
        description =
            [
                "Enables parsing the ASTs for the source files in parallel, " +
                    "but the passes afterwards will still run in a single thread"]
    )
    var useParallelFrontends = false

    var passes: List<Pass> = ArrayList()
}

/** This class consists of settings that control how CPG will process includes. */
class TranslationSettings {
    @Option(
        names = ["--analyze-includes"],
        description =
            [
                "Enables parsing of include files. By default, if --includes are given, the parser will resolve symbols/templates from these include, but not load their parse tree. This will enforced to true, if unity builds are used."],
        fallbackValue = "true"
    )
    var analyzeIncludes = false

    @Option(
        names = ["--includes"],
        description =
            [
                "Path(s) containing include files. Path must be separated by \':\' (Mac/Linux) or \';\' (Windows)."],
        split = ":|;"
    )
    var includes: Array<File>? = null

    @Option(
        names = ["--enabled-includes"],
        paramLabel = "<path>",
        description =
            [
                "If includes is not empty, only the specified " +
                    "files will be parsed and processed in the cpg, unless it is a port(?) of the blacklist, in which it will " +
                    "be ignored. Path must be separated by : (Mac/Linux) or ; (Windows)"],
        split = ":|;"
    )
    var enabledIncludes: List<String> = ArrayList()

    @Option(
        names = ["--disabled-includes"],
        paramLabel = "<path>",
        description =
            [
                "If includes is not empty, the specified " +
                    "files will be excluded from being parsed and processed in the cpg. The blacklist entries always take " +
                    "priority over the whitelist entries. Path must be separated by : (Mac/Linux) or ; (Windows)"],
        split = ":|;"
    )
    var disabledIncludes: List<String> = ArrayList()
}
