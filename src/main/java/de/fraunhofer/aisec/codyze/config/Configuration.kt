package de.fraunhofer.aisec.codyze.config

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import de.fraunhofer.aisec.codyze.analysis.ServerConfiguration
import de.fraunhofer.aisec.cpg.ExperimentalGolang
import de.fraunhofer.aisec.cpg.ExperimentalPython
import de.fraunhofer.aisec.cpg.TranslationConfiguration
import de.fraunhofer.aisec.cpg.frontends.golang.GoLanguageFrontend
import de.fraunhofer.aisec.cpg.frontends.python.PythonLanguageFrontend
import de.fraunhofer.aisec.cpg.passes.EdgeCachePass
import de.fraunhofer.aisec.cpg.passes.IdentifierPass
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import org.slf4j.LoggerFactory
import picocli.CommandLine

class Configuration {

    // Added as Mixin so the already initialized objects are used instead of new ones created
    @CommandLine.Mixin var codyze = CodyzeConfiguration()
    @CommandLine.Mixin var cpg = CpgConfiguration()

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
    @ExperimentalGolang
    @ExperimentalPython
    fun buildServerConfiguration(): ServerConfiguration {
        val config =
            ServerConfiguration.builder()
                .launchLsp(codyze.executionMode.isLsp)
                .launchConsole(codyze.executionMode.isTui)
                .typestateAnalysis(codyze.analysis.tsMode)
                .disableGoodFindings(codyze.noGoodFindings)
                .markFiles(*codyze.mark.map { m -> m.absolutePath }.toTypedArray())
                // TODO: remove all cpg config and replace with TranslationConfiguration
                .analyzeIncludes(cpg.translation.analyzeIncludes)
                .includePath(cpg.translation.includes)
                .useUnityBuild(cpg.useUnityBuild)
        if (cpg.additionalLanguages.contains(Language.PYTHON) || cpg.enablePython) {
            config.registerLanguage(
                PythonLanguageFrontend::class.java,
                PythonLanguageFrontend.PY_EXTENSIONS
            )
        }
        if (cpg.additionalLanguages.contains(Language.GO) || cpg.enableGo) {
            config.registerLanguage(
                GoLanguageFrontend::class.java,
                GoLanguageFrontend.GOLANG_EXTENSIONS
            )
        }

        val disabledRulesMap = mutableMapOf<String, Pair<Boolean, MutableSet<String>>>()
        for (mName in codyze.disabledMarkRules) {
            val index = mName.lastIndexOf('.')
            val packageName = mName.subSequence(0, index).toString()
            val markName = mName.subSequence(index + 1, mName.length).toString()
            if (!markName.isEmpty()) {
                disabledRulesMap.putIfAbsent(packageName, Pair(false, mutableSetOf()))
                if (markName == "*")
                    disabledRulesMap.replace(
                        packageName,
                        disabledRulesMap.getValue(packageName).copy(first = true)
                    )
                else disabledRulesMap[packageName]?.second?.add(markName)
            }
        }
        config.disableMark(disabledRulesMap)

        return config.build()
    }

    /**
     * Builds TranslationConfiguration object with available configurations
     *
     * @return TranslationConfiguration
     */
    @ExperimentalGolang
    @ExperimentalPython
    fun buildTranslationConfiguration(): TranslationConfiguration {
        val files: MutableList<File> = ArrayList()
        files.add(File(codyze.source!!.absolutePath))
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
                .sourceLocations(*files.toTypedArray())
        if (cpg.additionalLanguages.contains(Language.PYTHON) || cpg.enablePython) {
            translationConfig.registerLanguage(
                PythonLanguageFrontend::class.java,
                PythonLanguageFrontend.PY_EXTENSIONS
            )
        }
        if (cpg.additionalLanguages.contains(Language.GO) || cpg.enableGo) {
            translationConfig.registerLanguage(
                GoLanguageFrontend::class.java,
                GoLanguageFrontend.GOLANG_EXTENSIONS
            )
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
                    String.format(
                        "Could not parse configuration file correctly because %s is not a valid argument name for %s configurations.",
                        e.propertyName,
                        e.path[0].fieldName
                    )
                )
            } catch (e: FileNotFoundException) {
                printErrorMessage(String.format("File at %s not found.", configFile.absolutePath))
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
