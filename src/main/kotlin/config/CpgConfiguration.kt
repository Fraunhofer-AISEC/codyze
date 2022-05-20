package de.fraunhofer.aisec.codyze.legacy.config

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import de.fraunhofer.aisec.codyze.legacy.config.converters.LanguageDeserializer
import de.fraunhofer.aisec.codyze.legacy.config.converters.PassListDeserializer
import de.fraunhofer.aisec.cpg.passes.Pass
import java.io.File
import java.util.*
import picocli.CommandLine.ArgGroup
import picocli.CommandLine.Option

/** This class consists of configuration settings for CPG. */
class CpgConfiguration {

    val translation: TranslationSettings = TranslationSettings()

    @Option(
        names = ["--additional-languages"],
        split = "\${sys:path.separator}",
        paramLabel = "<language>",
        description =
            [
                "Enables the experimental support for additional languages (currently \${COMPLETION-CANDIDATES}). Additional files need to be placed in certain locations. Please follow the CPG README."
            ]
    )
    @JsonDeserialize(using = LanguageDeserializer::class)
    var additionalLanguages: EnumSet<Language> = EnumSet.noneOf(Language::class.java)

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
                "If false, type listener system is only activated after the frontends are done building the initial AST structure.\n\t(Default: \${DEFAULT-VALUE})"
            ],
        fallbackValue = "true"
    )
    var typeSystemInFrontend = true

    @Option(
        names = ["--debug-parser"],
        description = ["Controls debug output generation for the cpg parser."],
        fallbackValue = "true"
    )
    var debugParser = false

    @Option(
        names = ["--disable-cleanup"],
        description =
            [
                "Switch off cleaning up TypeManager memory after the analysis. Set to true only for testing."
            ],
        fallbackValue = "true"
    )
    var disableCleanup = false

    @Option(
        names = ["--code-in-nodes"],
        negatable = true,
        description =
            [
                "Controls showing the code of a node as parameter in the node.\n\t(Default: \${DEFAULT-VALUE})"
            ],
        fallbackValue = "true"
    )
    var codeInNodes = true

    @JsonProperty("annotations")
    @Option(
        names = ["--annotations"],
        description = ["Enables processing annotations or annotation-like elements."],
        fallbackValue = "true"
    )
    var processAnnotations = false

    @Option(
        names = ["--fail-on-error"],
        description =
            [
                "Should the parser/translation fail on errors (true) or try to continue in a best-effort manner (false).\n\t(Default: \${DEFAULT-VALUE})"
            ],
        fallbackValue = "true"
    )
    var failOnError = false

    @JsonIgnore @ArgGroup(exclusive = true) val symbolsCLI = SymbolsArgGroup()
    var symbols: Map<String, String> = HashMap()

    @JsonProperty("parallel-frontends")
    @Option(
        names = ["--parallel-frontends"],
        description =
            [
                "Enables parsing the ASTs for the source files in parallel, but the passes afterwards will still run in a single thread."
            ],
        fallbackValue = "true"
    )
    var useParallelFrontends = false

    @Option(
        names = ["--default-passes"],
        negatable = true,
        description = ["Controls the usage of default passes for cpg.\n\t(Default: true)"],
        fallbackValue = "true"
    )
    // Set to null to differentiate if it was set or not
    var defaultPasses: Boolean? = null

    @JsonIgnore @ArgGroup(exclusive = true) val passesCLI = PassesArgGroup()
    @JsonDeserialize(using = PassListDeserializer::class) var passes: List<Pass> = ArrayList()
}

/** This class consists of settings that control how CPG will process includes. */
class TranslationSettings {
    @Option(
        names = ["--analyze-includes"],
        description =
            [
                "Enables parsing of include files. By default, if --includes are given, the parser will resolve symbols/templates from these include, but not load their parse tree. This will enforced to true, if unity builds are used."
            ],
        fallbackValue = "true"
    )
    var analyzeIncludes = false

    @JsonIgnore @ArgGroup(exclusive = true) val includesCLI = IncludesArgGroup()
    var includes: Array<File> = emptyArray()

    @JsonIgnore @ArgGroup(exclusive = true) val enabledIncludesCLI = EnabledIncludesArgGroup()
    var enabledIncludes: Array<File> = emptyArray()

    @JsonIgnore @ArgGroup(exclusive = true) val disabledIncludesCLI = DisabledIncludesArgGroup()
    var disabledIncludes: Array<File> = emptyArray()
}

class PassesArgGroup {
    var append = false
    var matched = false

    var passes: List<Pass> = emptyList()

    @Option(
        names = ["--passes"],
        paramLabel = "<pass>",
        description =
            [
                "CPG passes in the order in which they should be executed, fully qualified name of the classes only. If default-passes is specified, the default passes are executed first."
            ],
        split = "\${sys:path.separator}"
    )
    fun match(passes: List<Pass?>) {
        matched = true
        this.passes = passes.filterNotNull()
    }

    @Option(
        names = ["--passes+"],
        paramLabel = "<pass>",
        description =
            ["See passes, but appends the values to the ones specified in configuration file."],
        split = "\${sys:path.separator}"
    )
    fun append(value: List<Pass?>) {
        append = true
        match(value)
    }
}

class SymbolsArgGroup {
    var append = false
    var matched = false

    var symbols: Map<String, String> = emptyMap()

    @Option(
        names = ["--symbols"],
        paramLabel = "<symbol>=<definition>",
        description = ["Definition of additional symbols."],
        split = "\${sys:path.separator}"
    )
    fun match(value: Map<String, String>) {
        matched = true
        this.symbols = value
    }

    @Option(
        names = ["--symbols+"],
        paramLabel = "<symbol>=<definition>",
        description =
            ["See --symbols, but appends the values to the ones specified in configuration file."],
        split = "\${sys:path.separator}"
    )
    fun append(value: Map<String, String>) {
        append = true
        match(value)
    }
}

class IncludesArgGroup {
    var append = false
    var matched = false

    var includes: Array<File> = emptyArray()

    @Option(
        names = ["--includes"],
        paramLabel = "<path>",
        description = ["Path(s) containing include files."],
        split = "\${sys:path.separator}"
    )
    fun match(value: Array<File>) {
        matched = true
        this.includes = value
    }

    @Option(
        names = ["--includes+"],
        paramLabel = "<path>",
        description =
            ["See --includes, but appends the values to the ones specified in configuration file."],
        split = "\${sys:path.separator}"
    )
    fun append(value: Array<File>) {
        append = true
        match(value)
    }
}

class EnabledIncludesArgGroup {
    var append = false
    var matched = false

    var enabledIncludes: Array<File> = emptyArray()

    @Option(
        names = ["--enabled-includes"],
        paramLabel = "<path>",
        description =
            [
                "If includes is not empty, only the specified files will be parsed and processed in the cpg, unless it is a part of the disabled list, in which it will be ignored."
            ],
        split = "\${sys:path.separator}"
    )
    fun match(value: Array<File>) {
        matched = true
        this.enabledIncludes = value
    }

    @Option(
        names = ["--enabled-includes+"],
        paramLabel = "<path>",
        description =
            [
                "See --enabled-includes, but appends the values to the ones specified in configuration file."
            ],
        split = "\${sys:path.separator}"
    )
    fun append(value: Array<File>) {
        append = true
        match(value)
    }
}

class DisabledIncludesArgGroup {
    var append = false
    var matched = false

    var disabledIncludes: Array<File> = emptyArray()

    @Option(
        names = ["--disabled-includes"],
        paramLabel = "<path>",
        description =
            [
                "If includes is not empty, the specified files will be excluded from being parsed and processed in the cpg. The disabled list entries always take priority over the enabled list entries."
            ],
        split = "\${sys:path.separator}"
    )
    fun match(value: Array<File>) {
        matched = true
        this.disabledIncludes = value
    }

    @Option(
        names = ["--disabled-includes+"],
        paramLabel = "<path>",
        description =
            [
                "See --disabled-includes, but appends the values to the ones specified in configuration file."
            ],
        split = "\${sys:path.separator}"
    )
    fun append(value: Array<File>) {
        append = true
        match(value)
    }
}
