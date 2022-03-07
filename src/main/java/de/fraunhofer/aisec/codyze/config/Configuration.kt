package de.fraunhofer.aisec.codyze.config

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import de.fraunhofer.aisec.codyze.analysis.ServerConfiguration
import de.fraunhofer.aisec.cpg.TranslationConfiguration
import de.fraunhofer.aisec.cpg.frontends.LanguageFrontend
import de.fraunhofer.aisec.cpg.passes.EdgeCachePass
import de.fraunhofer.aisec.cpg.passes.IdentifierPass
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import org.slf4j.LoggerFactory
import picocli.CommandLine

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class Configuration {

    @JsonIgnore
    @CommandLine.ArgGroup(exclusive = true, multiplicity = "1", heading = "Execution Mode\n")
    val executionMode: ExecutionMode = ExecutionMode()

    @CommandLine.Option(
        names = ["-s", "--source"],
        paramLabel = "<path>",
        description = ["Source file or folder to analyze."]
    )
    var source: File? = null

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
        description = ["Terminate analysis after timeout.\n\t(Default: \${DEFAULT-VALUE})"]
    )
    var timeout = 120L

    @JsonProperty("sarif")
    @CommandLine.Option(
        names = ["--sarif"],
        description = ["Enables the SARIF output."],
        fallbackValue = "true"
    )
    var sarifOutput: Boolean = false

    private var codyze = CodyzeConfiguration()
    private var cpg = CpgConfiguration()

    constructor()

    constructor(codyzeConfiguration: CodyzeConfiguration, cpgConfiguration: CpgConfiguration) {
        this.codyze = codyzeConfiguration
        this.cpg = cpgConfiguration
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
            .setCaseInsensitiveEnumValuesAllowed(true)
            // setUnmatchedArgumentsAllowed is true because both classes don't have the config path
            // option which would result in exceptions, side effect is that all unknown options are
            // ignored
            .setUnmatchedArgumentsAllowed(true)
            .parseArgs(*args)
    }

    /**
     * Builds ServerConfiguration object with available configurations
     *
     * @return ServerConfiguration
     */
    fun buildServerConfiguration(): ServerConfiguration {
        val config =
            ServerConfiguration.builder()
                .launchLsp(executionMode.isLsp)
                .launchConsole(executionMode.isTui)
                .typestateAnalysis(codyze.analysis.tsMode)
                .disableGoodFindings(
                    if (codyze.pedantic) {
                        false
                    } else {
                        codyze.noGoodFindings
                    }
                )
                .markFiles(*codyze.mark.map { m -> m.absolutePath }.toTypedArray())

        if (!codyze.pedantic) {
            val disabledRulesMap = mutableMapOf<String, DisabledMarkRulesValue>()
            for (mName in codyze.disabledMarkRules) {
                val index = mName.lastIndexOf('.')
                val packageName = mName.subSequence(0, index).toString()
                val markName = mName.subSequence(index + 1, mName.length).toString()
                if (markName.isNotEmpty()) {
                    disabledRulesMap.putIfAbsent(packageName, DisabledMarkRulesValue())
                    if (markName == "*")
                        disabledRulesMap.getValue(packageName).isDisablePackage = true
                    else disabledRulesMap[packageName]?.disabledMarkRuleNames?.add(markName)
                } else
                    log.warn(
                        "Error while parsing disabled-mark-rules: \'$mName\' is not a valid name for a mark rule. Continue parsing disabled-mark-rules"
                    )
            }
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
        val translationConfig =
            TranslationConfiguration.builder()
                .debugParser(!executionMode.isLsp)
                .failOnError(false)
                .codeInNodes(true)
                // we need to force load includes for unity builds, otherwise nothing will be parsed
                .loadIncludes(cpg.translation.analyzeIncludes || cpg.useUnityBuild)
                .useUnityBuild(cpg.useUnityBuild)
                .defaultPasses()
                .defaultLanguages()
                .registerPass(IdentifierPass())
                .registerPass(EdgeCachePass())
                .sourceLocations(*sources)
        if (cpg.additionalLanguages.contains(Language.PYTHON) || cpg.enablePython) {
            val pythonFrontendClazz =
                try {
                    @Suppress("UNCHECKED_CAST")
                    Class.forName(Language.PYTHON.frontendClassName) as Class<LanguageFrontend>
                } catch (e: Throwable) {
                    log.warn("Unable to initialize Python frontend for CPG")
                    null
                }

            if (pythonFrontendClazz != null) {
                @Suppress("UNCHECKED_CAST")
                val extensions =
                    pythonFrontendClazz
                        .fields
                        .find { f -> f.name.endsWith("_EXTENSIONS") }
                        ?.get(null) as
                        List<String>

                translationConfig.registerLanguage(pythonFrontendClazz, extensions)
            }
        }
        if (cpg.additionalLanguages.contains(Language.GO) || cpg.enableGo) {
            val golangFrontendClazz =
                try {
                    @Suppress("UNCHECKED_CAST")
                    Class.forName(Language.GO.frontendClassName) as Class<LanguageFrontend>
                } catch (e: Throwable) {
                    log.warn("Unable to initialize Golang frontend for CPG")
                    null
                }
            if (golangFrontendClazz != null) {
                @Suppress("UNCHECKED_CAST")
                val extensions =
                    golangFrontendClazz
                        .fields
                        .find { f -> f.name.endsWith("_EXTENSIONS") }
                        ?.get(null) as
                        List<String>

                translationConfig.registerLanguage(golangFrontendClazz, extensions)
            }
        }
        for (file in cpg.translation.includes) translationConfig.includePath(file.absolutePath)
        return translationConfig.build()
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
                if (configFile != null) parseFile(configFile) else Configuration()
            config.parseCLI(*args)
            return config
        }

        // parse yaml configuration file with jackson
        private fun parseFile(configFile: File): Configuration {
            val mapper =
                YAMLMapper.builder().enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS).build()
            mapper.enable(JsonParser.Feature.IGNORE_UNDEFINED)
            mapper.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)
            mapper.propertyNamingStrategy = PropertyNamingStrategies.KebabCaseStrategy()
            var config: Configuration? = null
            try {
                config = mapper.readValue(configFile, Configuration::class.java)
            } catch (e: UnrecognizedPropertyException) {
                printErrorMessage(
                    "Could not parse configuration file correctly because \"${e.propertyName}\" is not a valid argument name for ${e.path[0].fieldName} configurations."
                )
            } catch (e: FileNotFoundException) {
                printErrorMessage("File at ${configFile.absolutePath} not found.")
            } catch (e: IOException) {
                printErrorMessage(e.message)
            }
            return config ?: Configuration()
        }

        // print error message to log
        private fun printErrorMessage(msg: String?) {
            log.warn("{} Continue without configurations from file.", msg)
        }
    }
}
