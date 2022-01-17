package de.fraunhofer.aisec.codyze.config

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import java.io.File
import java.util.*
import picocli.CommandLine
import picocli.CommandLine.ArgGroup

class CpgConfiguration {
    @ArgGroup(exclusive = false, heading = "Translation Options\n")
    var translation = TranslationSettings()

    @JsonDeserialize(using = LanguageDeserializer::class)
    var additionalLanguages: Set<Language> = EnumSet.noneOf(Language::class.java)

    // TODO: maybe change to enum set instead of booleans for each language
    @JsonIgnore
    @CommandLine.Option(names = ["--enable-python-support"], description = ["Enables the experimental Python support. Additional files need to be placed in certain locations. Please follow the CPG README."])
    var enablePython = false

    @JsonIgnore
    @CommandLine.Option(names = ["--enable-go-support"], description = ["Enables the experimental Go support. Additional files need to be placed in certain locations. Please follow the CPG README."])
    var enableGo = false

    @set:JsonProperty("unity")
    @CommandLine.Option(names = ["--unity"], description = ["Enables unity builds (C++ only) for files in the path"])
    var useUnityBuild = false
}

class TranslationSettings {
    @CommandLine.Option(names = ["--analyze-includes"], description = ["Enables parsing of include files. By default, if --includes are given, the parser will resolve symbols/templates from these include, but not load their parse tree. This will enforced to true, if unity builds are used."])
    var analyzeIncludes = false

    @CommandLine.Option(names = ["--includes"], description = ["Path(s) containing include files. Path must be separated by : (Mac/Linux) or ; (Windows)"], split = ":|;")
    var includes: Array<File>? = null
}
