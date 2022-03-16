package de.fraunhofer.aisec.codyze.config

import com.fasterxml.jackson.core.JsonLocation
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import de.fraunhofer.aisec.codyze.analysis.ServerConfiguration
import de.fraunhofer.aisec.cpg.TranslationConfiguration
import de.fraunhofer.aisec.cpg.frontends.LanguageFrontend
import de.fraunhofer.aisec.cpg.passes.Pass
import java.io.File
import java.io.IOException
import org.slf4j.LoggerFactory
import picocli.CommandLine

class Configuration {

    // Added as Mixin so the already initialized objects are used instead of new ones created
    @CommandLine.Mixin var codyze = CodyzeConfiguration()
    @CommandLine.Mixin var cpg = CpgConfiguration()

    /**
     * Builds ServerConfiguration object with available configurations
     *
     * @return ServerConfiguration
     */
    fun buildServerConfiguration(): ServerConfiguration {
        this.normalize()
        val config =
            ServerConfiguration.builder()
                .launchLsp(codyze.executionMode.isLsp)
                .launchConsole(codyze.executionMode.isTui)
                .typestateAnalysis(codyze.analysis.tsMode)
                .disableGoodFindings(codyze.noGoodFindings)
                .markFiles(*codyze.mark.map { m -> m.absolutePath }.toTypedArray())
                .pedantic(codyze.pedantic)
                // TODO: remove all cpg config and replace with TranslationConfiguration
                .analyzeIncludes(cpg.translation.analyzeIncludes)
                .includePath(cpg.translation.includes)
                .useUnityBuild(cpg.useUnityBuild)

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

                config.registerLanguage(frontendClazz, extensions)
            }
        }

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
    fun buildTranslationConfiguration(): TranslationConfiguration {
        this.normalize()

        val files: MutableList<File> = ArrayList()
        files.add(File(codyze.source!!.absolutePath))
        val translationConfig =
            TranslationConfiguration.builder()
                .debugParser(cpg.debugParser)
                .failOnError(cpg.failOnError)
                .codeInNodes(cpg.codeInNodes)
                .loadIncludes(cpg.translation.analyzeIncludes)
                .useUnityBuild(cpg.useUnityBuild)
                .processAnnotations(cpg.processAnnotations)
                .symbols(cpg.symbols)
                .useParallelFrontends(cpg.useParallelFrontends)
                .typeSystemActiveInFrontend(cpg.typeSystemInFrontend)
                .defaultLanguages()
                .sourceLocations(*files.toTypedArray())

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

    private fun normalize() {
        // In pedantic analysis mode all MARK rules are analyzed and all findings reported
        if (codyze.pedantic) {
            codyze.noGoodFindings = false
            codyze.disabledMarkRules = emptyList()
        }

        // we need to force load includes for unity builds, otherwise nothing will be parsed
        if (cpg.useUnityBuild) cpg.translation.analyzeIncludes = true

        if (codyze.executionMode.isLsp) {
            // we don't want the parser to print to the terminal when in LSP mode
            cpg.debugParser = false
        }
    }

    // Parse CLI arguments into config class
    private fun parseCLI(vararg args: String?) {

        CommandLine(this)
            // Added as Mixin so the already initialized objects are used instead of new ones
            // created
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
                if (configFile != null) parseFile(configFile) else Configuration()
            config.parseCLI(*args)
            return config
        }

        // parse yaml configuration file with jackson
        private fun parseFile(configFile: File): Configuration {
            val mapper =
                YAMLMapper.builder().enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS).build()
            mapper
                .enable(JsonParser.Feature.IGNORE_UNDEFINED)
                .enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)
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
