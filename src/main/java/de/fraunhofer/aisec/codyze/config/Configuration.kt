package de.fraunhofer.aisec.codyze.config

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonLocation
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import de.fraunhofer.aisec.codyze.Main.ConfigFilePath
import de.fraunhofer.aisec.codyze.analysis.ServerConfiguration
import de.fraunhofer.aisec.codyze.config.converters.FileDeserializer
import de.fraunhofer.aisec.codyze.config.converters.OutputDeserializer
import de.fraunhofer.aisec.codyze.config.converters.PassTypeConverter
import de.fraunhofer.aisec.cpg.TranslationConfiguration
import de.fraunhofer.aisec.cpg.frontends.LanguageFrontend
import de.fraunhofer.aisec.cpg.passes.Pass
import java.io.File
import java.io.IOException
import org.slf4j.LoggerFactory
import picocli.CommandLine

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class Configuration {

    @JsonIgnore
    @CommandLine.ArgGroup(exclusive = true, heading = "Execution Mode\n")
    val executionMode: ExecutionMode = ExecutionMode()

    @CommandLine.Option(
        names = ["-s", "--source"],
        paramLabel = "<path>",
        split = ",",
        description = ["Source files or folders to analyze.\n\t(Default: \${DEFAULT-VALUE})"]
    )
    var source: Array<File> = arrayOf(File("./"))
        private set

    @JsonProperty("disabled-sources")
    @CommandLine.Option(
        names = ["--disabled-sources"],
        paramLabel = "<path>",
        split = ",",
        description = ["Files or folders specified here will not be analyzed. Symbolic links are not followed when filtering out these paths"]
    )
    var disabledSource: Array<File> = emptyArray()

    // TODO output standard stdout?
    @CommandLine.Option(
        names = ["-o", "--output"],
        paramLabel = "<file>",
        description = ["Write results to file. Use - for stdout.\n\t(Default: \${DEFAULT-VALUE})"]
    )
    @JsonDeserialize(using = OutputDeserializer::class)
    var output = "findings.sarif"
        private set

    @CommandLine.Option(
        names = ["--timeout"],
        paramLabel = "<minutes>",
        description = ["Terminate analysis after timeout.\n\t(Default: \${DEFAULT-VALUE})"]
    )
    var timeout = 120L
        private set

    @JsonProperty("sarif")
    @CommandLine.Option(
        names = ["--sarif"],
        negatable = true,
        description =
            [
                "Controls whether the output is written in the SARIF format.\n\t(Default: \${DEFAULT-VALUE})"],
        fallbackValue = "true"
    )
    var sarifOutput = true
        private set

    private var codyze = CodyzeConfiguration()
    private var cpg = CpgConfiguration()

    constructor()

    constructor(codyzeConfiguration: CodyzeConfiguration, cpgConfiguration: CpgConfiguration) {
        this.codyze = codyzeConfiguration
        this.cpg = cpgConfiguration
    }

    /**
     * Builds ServerConfiguration object with available configurations
     *
     * @return ServerConfiguration
     */
    fun buildServerConfiguration(): ServerConfiguration {
        this.normalize()
        val config =
            ServerConfiguration.builder()
                .launchLsp(executionMode.isLsp)
                .launchConsole(executionMode.isTui)
                .typestateAnalysis(codyze.analysis.tsMode)
                .disableGoodFindings(codyze.noGoodFindings)
                .markFiles(*codyze.mark.map { m -> m.absolutePath }.toTypedArray())
                .pedantic(codyze.pedantic)

        val disabledRulesMap = mutableMapOf<String, DisabledMarkRulesValue>()
        for (mName in codyze.disabledMarkRules) {
            val index = mName.lastIndexOf('.')
            val packageName = mName.subSequence(0, index).toString()
            val markName = mName.subSequence(index + 1, mName.length).toString()
            if (markName.isNotEmpty()) {
                disabledRulesMap.putIfAbsent(packageName, DisabledMarkRulesValue())
                if (markName == "*") disabledRulesMap.getValue(packageName).isDisablePackage = true
                else disabledRulesMap[packageName]?.disabledMarkRuleNames?.add(markName)
            } else
                log.warn(
                    "Error while parsing disabled-mark-rules: \'$mName\' is not a valid name for a mark rule. Continue parsing disabled-mark-rules"
                )
            config.disableMark(disabledRulesMap)
        }

        return config.build()
    }

    /**
     * Builds TranslationConfiguration object with available configurations
     *
     * @return TranslationConfiguration
     */
    fun buildTranslationConfiguration(vararg sources: File): TranslationConfiguration {
        this.normalize()
        val translationConfig =
            TranslationConfiguration.builder()
                .debugParser(if (executionMode.isLsp) false else cpg.debugParser)
                .failOnError(cpg.failOnError)
                .codeInNodes(cpg.codeInNodes)
                .loadIncludes(cpg.translation.analyzeIncludes)
                .useUnityBuild(cpg.useUnityBuild)
                .processAnnotations(cpg.processAnnotations)
                .symbols(cpg.symbols)
                .useParallelFrontends(cpg.useParallelFrontends)
                .typeSystemActiveInFrontend(cpg.typeSystemInFrontend)
                .defaultLanguages()
                .sourceLocations(*filterFiles(disabledSource, *sources))

        for (file in cpg.translation.includes) {
            translationConfig.includePath(file.absolutePath)
        }
        for (s in cpg.translation.enabledIncludes) {
            translationConfig.includeWhitelist(s.absolutePath)
        }
        for (s in cpg.translation.disabledIncludes) {
            translationConfig.includeBlacklist(s.absolutePath)
        }

        if (cpg.disableCleanup) {
            translationConfig.disableCleanup()
        }

        if (cpg.defaultPasses == null) {
            if (cpg.passes.isEmpty()) {
                translationConfig.defaultPasses()
            }
        } else {
            if (cpg.defaultPasses!!) {
                translationConfig.defaultPasses()
            } else {
                if (cpg.passes.isEmpty()) {
                    // TODO: error handling for no passes if needed
                }
            }
        }
        for (p in cpg.passes) {
            translationConfig.registerPass(p)
        }

        for (l in cpg.additionalLanguages) {
            val frontendClazz =
                try {
                    @Suppress("UNCHECKED_CAST")
                    Class.forName(l.frontendClassName) as Class<LanguageFrontend>
                } catch (e: Throwable) {
                    log.warn("Unable to initialize {} frontend for CPG", l.name)
                    null
                }

            if (frontendClazz != null) {
                @Suppress("UNCHECKED_CAST")
                val extensions =
                    frontendClazz.fields.find { f -> f.name.endsWith("_EXTENSIONS") }?.get(null) as
                        List<String>

                translationConfig.registerLanguage(frontendClazz, extensions)
            }
        }

        return translationConfig.build()
    }

    /**
     * Filters out files that are in the excludedFiles array from files
     *
     * @param excludedFiles array of files and/or directories that should be excluded
     * @param files files and/or directories from which the excluded files should be filtered out
     * from
     *
     * @return array of filtered files
     */
    private fun filterFiles(excludedFiles: Array<File>, vararg files: File): Array<File> {
        if (excludedFiles.isEmpty()) return arrayOf(*files)

        var result: MutableList<File> =
            listOf(*files).map { f -> f.absoluteFile.normalize() }.toMutableList()

        for (excludedFile in excludedFiles) {
            val excludedNormalizedFile = excludedFile.absoluteFile.normalize()

            // will be list of included files after filtering out excludedFile
            val newResult = mutableListOf<File>()

            for (includedFile in result) {
                // excludedPath is located under includedFile
                if (includedFile.isDirectory &&
                        excludedNormalizedFile.startsWith(
                            includedFile.absolutePath + File.separator
                        )
                ) {
                    newResult.addAll(findSiblings(excludedNormalizedFile, includedFile))
                } else if (
                // includedFile is located under excludedPath or excludedPath is equal to
                // includedFile
                (excludedNormalizedFile.isDirectory &&
                        includedFile.startsWith(
                            excludedNormalizedFile.absolutePath + File.separator
                        )) || excludedNormalizedFile == includedFile
                ) {
                    // do nothing
                } else {
                    // add includedFile because it was not in this excluded path
                    newResult.add(includedFile)
                }
            }
            result = newResult
        }

        return result.toTypedArray()
    }

    /**
     * Find all sibling files by traversing file tree upwards until root is reached
     *
     * @param start starting file
     * @param root function searches until here
     *
     * @return list of sibling files
     */
    private fun findSiblings(start: File, root: File): List<File> {
        val result = mutableListOf<File>()

        var current = start
        while (current != root) {
            // find siblings of excludedPath because they should still be included
            val siblings = current.parentFile.listFiles { f -> f != current }
            if (siblings != null) result.addAll(siblings)
            current = current.parentFile
        }

        return result
    }

    private fun normalize() {
        // In pedantic analysis mode all MARK rules are analyzed and all findings reported
        if (codyze.pedantic) {
            codyze.noGoodFindings = false
            codyze.disabledMarkRules = emptyList()
        }

        // we need to force load includes for unity builds, otherwise nothing will be parsed
        if (cpg.useUnityBuild) cpg.translation.analyzeIncludes = true

        if (executionMode.isLsp) {
            // we don't want the parser to print to the terminal when in LSP mode
            cpg.debugParser = false
        }

        if (!executionMode.isCli && !executionMode.isLsp && !executionMode.isTui) {
            executionMode.isCli = true
        }
    }

    // Parse CLI arguments into config class
    private fun parseCLI(vararg args: String?) {

        CommandLine(this)
            // Added as Mixin so the already initialized objects are used instead of new ones
            // created
            .addMixin("codyze", codyze)
            .addMixin("cpg", cpg)
            .addMixin("analysis", codyze.analysis)
            .addMixin("translation", cpg.translation)
            .registerConverter(Pass::class.java, PassTypeConverter())
            .setCaseInsensitiveEnumValuesAllowed(true)
            // setUnmatchedArgumentsAllowed is true because both classes don't have the config path
            // option which would result in exceptions, side effect is that all unknown options are
            // ignored
            .setUnmatchedArgumentsAllowed(true)
            .parseArgs(*args)
    }

    companion object {
        private val log = LoggerFactory.getLogger(Configuration::class.java)

        /**
         * Initializes a new Configuration object populated with the configurations specified in the
         * configFile and in the CLI args
         *
         * @param configFile a yaml file with configurations for codyze
         * @param args CLI arguments
         * @return the new Configuration object
         */
        @JvmStatic
        fun initConfig(configFile: File?, vararg args: String?): Configuration {
            val config: Configuration =
                if (configFile != null) {
                    parseFile(configFile)
                } else {
                    val defaultConfigFile = ConfigFilePath().configFile
                    if (defaultConfigFile.isFile) {
                        parseFile(defaultConfigFile)
                    } else {
                        Configuration()
                    }
                }
            config.parseCLI(*args)
            return config
        }

        // parse yaml configuration file with jackson
        private fun parseFile(configFile: File): Configuration {

            val module =
                SimpleModule()
                    .setDeserializerModifier(
                        object : BeanDeserializerModifier() {
                            override fun modifyDeserializer(
                                config: DeserializationConfig,
                                beanDesc: BeanDescription,
                                deserializer: JsonDeserializer<*>
                            ): JsonDeserializer<*> {
                                if (beanDesc.beanClass == File::class.java)
                                    return FileDeserializer(configFile, deserializer)
                                return super.modifyDeserializer(config, beanDesc, deserializer)
                            }
                        }
                    )
            val mapper =
                YAMLMapper.builder().enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS).build()
            mapper
                .enable(JsonParser.Feature.IGNORE_UNDEFINED)
                .enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .registerModule(module)
            mapper.injectableValues =
                InjectableValues.Std()
                    .addValue("configFileBasePath", configFile.absoluteFile.parentFile.absolutePath)
            mapper.propertyNamingStrategy = PropertyNamingStrategies.KebabCaseStrategy()
            var config: Configuration? = null
            try {
                config = mapper.readValue(configFile, Configuration::class.java)
            } catch (e: IOException) {
                printErrorMessage(configFile.absolutePath, e.toString())
            }
            return config ?: Configuration()
        }

        // print error message to log
        private fun printErrorMessage(source: String, msg: String) {
            log.warn(
                "Parsing configuration file failed ({}): {}. Continue without configurations from file.",
                source,
                msg
            )
        }

        fun getLocation(tokenLocation: JsonLocation): String {
            return if (tokenLocation.contentReference() != null &&
                    tokenLocation.contentReference().rawContent is File
            )
                " (${(tokenLocation.contentReference().rawContent as File).absolutePath})"
            else ""
        }
    }
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
    @CommandLine.Option(
        names = ["-c"],
        required = true,
        description = ["Start in command line mode (default)."]
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
}
