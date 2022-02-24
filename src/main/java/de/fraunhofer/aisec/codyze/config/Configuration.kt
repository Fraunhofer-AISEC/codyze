package de.fraunhofer.aisec.codyze.config

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

class Configuration {

    // Added as Mixin so the already initialized objects are used instead of new ones created
    @CommandLine.Mixin val codyze = CodyzeConfiguration()
    @CommandLine.Mixin val cpg = CpgConfiguration()

    // Parse CLI arguments into config class
    private fun parseCLI(vararg args: String?) {

        CommandLine(this)
            // Added as Mixin so the already initialized objects are used instead of new ones
            // created
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
        return ServerConfiguration.builder()
                .launchLsp(codyze.executionMode.isLsp)
                .launchConsole(codyze.executionMode.isTui)
                .typestateAnalysis(codyze.analysis.tsMode)
                .disableGoodFindings(
                    if (codyze.pedantic) {
                        false
                    } else {
                        codyze.noGoodFindings
                    }
                )
                .markFiles(*codyze.mark.map { m -> m.absolutePath }.toTypedArray())
                .build()
    }

    /**
     * Builds TranslationConfiguration object with available configurations
     *
     * @return TranslationConfiguration
     */
    fun buildTranslationConfiguration(vararg sources: File): TranslationConfiguration {
        val translationConfig =
            TranslationConfiguration.builder()
                .debugParser(true)
                .failOnError(false)
                .codeInNodes(true)
                .loadIncludes(cpg.translation.analyzeIncludes)
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
        for (file in cpg.translation.includes!!) translationConfig.includePath(file.absolutePath)
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
