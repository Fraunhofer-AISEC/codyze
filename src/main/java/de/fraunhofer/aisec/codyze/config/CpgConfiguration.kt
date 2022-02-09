package de.fraunhofer.aisec.codyze.config

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

    @set:JsonProperty("unity")
    @Option(
        names = ["--unity"],
        description = ["Enables unity builds (C++ only) for files in the path."],
        fallbackValue = "true"
    )
    var useUnityBuild = false

    // TODO: name!!!!!
    @Option(
        names = ["--type-system-in-frontend"],
        negatable = true,
        description =
            [
                "If false, type listener is only activated after the frontends are done building the initial AST structure.(Default: \${DEFAULT-VALUE})"],
        fallbackValue = "true"
    )
    var typeSystemActiveInFrontend = true

    @Option(
        names = ["--default-passes"],
        negatable = true,
        description = ["Controls the usage of default passes for cpg.\n\t(Default: true)"],
        fallbackValue = "true"
    )
    // Set to null to differentiate if it was set or not
    var defaultPasses: Boolean? = null

    @JsonDeserialize(using = PassListDeserializer::class)
    var passes: List<Pass> = ArrayList()

    @Option(
        names = ["--passes"],
        paramLabel = "pass",
        description =
            [
                "CPG passes in the order in which they should be executed, fully qualified name of the classes only. If default-passes is specified, the default passes are executed first."],
        split = ","
    )
    @JvmName("setPassesNull")
    fun setPasses(passes: List<Pass?>) {
        this.passes = passes.filterNotNull()
    }

    @Option(
        names = ["--debug-parser"],
        description = ["Controls debug output generation for the cpg parser"],
        fallbackValue = "true"
    )
    var debugParser = false

    @Option(
        names = ["--no-cleanup"],
        description =
            [
                "Switch off cleaning up TypeManager memory after the analysis. Set to true only for testing"],
        fallbackValue = "true"
    )
    var disableCleanup = false

    @Option(
        names = ["--code-in-nodes"],
        negatable = true,
        description =
            [
                "Controls showing the code of a node as parameter in the node\n\t(Default: \${DEFAULT-VALUE})"],
        fallbackValue = "true"
    )
    var codeInNodes = true

    @Option(
        names = ["--annotations"],
        description = ["Enables processing annotations or annotation-like elements"],
        fallbackValue = "true"
    )
    var processAnnotations = false

    @Option(
        names = ["--fail-on-error"],
        description =
            [
                "Should the parser/translation fail on errors (true) or try to continue in a best-effort manner (false)\n\t(Default: \${DEFAULT-VALUE})"],
        fallbackValue = "true"
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
                "Enables parsing the ASTs for the source files in parallel, but the passes afterwards will still run in a single thread"],
        fallbackValue = "true"
    )
    var useParallelFrontends = false
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
    var includes: Array<File> = emptyArray()

    @Option(
        names = ["--enabled-includes"],
        paramLabel = "<path>",
        description =
            [
                "If includes is not empty, only the specified files will be parsed and processed in the cpg, unless it is a port(?) of the blacklist, in which it will be ignored. Path must be separated by : (Mac/Linux) or ; (Windows)"],
        split = ":|;"
    )
    var enabledIncludes: List<String> = ArrayList()

    @Option(
        names = ["--disabled-includes"],
        paramLabel = "<path>",
        description =
            [
                "If includes is not empty, the specified files will be excluded from being parsed and processed in the cpg. The blacklist entries always take priority over the whitelist entries. Path must be separated by : (Mac/Linux) or ; (Windows)"],
        split = ":|;"
    )
    var disabledIncludes: List<String> = ArrayList()
}
