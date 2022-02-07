package de.fraunhofer.aisec.codyze.config

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
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
}
