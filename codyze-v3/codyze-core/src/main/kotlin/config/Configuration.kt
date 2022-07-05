package de.fraunhofer.aisec.codyze_core.config

import de.fraunhofer.aisec.codyze_core.Executor
import de.fraunhofer.aisec.cpg.TranslationConfiguration
import de.fraunhofer.aisec.cpg.passes.Pass
import java.nio.file.Path
import kotlin.io.path.extension
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Holds the whole configuration to run Codyze with
 *
 * To add a new configuration option do the following:
 * 1. add a property to either [Configuration] or [Configuration.CPGConfiguration]
 * 2. add the new property to all factory methods of the [Configuration] class (e.g.,
 * [Configuration.from] or [Configuration.CPGConfiguration.from])
 * 3. add a new CLI option to one (or all) subcommand(s). It does not matter whether the option is
 * inside an [OptionGroup].
 * 4. make sure to add a '?' for any CLI option that might be [null]. Options that might be [null]
 * and are not specified as such can cause issues with the map delegate used in the factory methods.
 * 5. after adding the new CLI option, register it at the [ConfigurationRegister]. Only then will it
 * be part of the map returned by [ConfigurationRegister.options] which is used to initialize the
 * [Configuration] object
 */
data class Configuration(
    val typestate: TypestateMode,
    val spec: List<Path>,
    val specDescription: Path,
    val disabledSpecRules: List<String>,
    val output: Path,
    val goodFindings: Boolean,
    val pedantic: Boolean,
    val timeout: Int,
    val executor: Executor? =
        null, // if null, Codyze randomly chooses an Executor capable of evaluating the given
    // specs. If no Executor is found, an error is thrown
    val cpgConfiguration: CPGConfiguration,
) {
    // perform some validation
    // the same validation should be performed when parsing the CLI arguments/options
    init {
        validateSpec(spec)
    }

    /** Filename extension of all [spec] files. All [spec] files share the same extension. */
    val specFileExtension by lazy { spec[0].extension }

    companion object {
        /**
         * Build a [Configuration] object from a [map] and a [cpgConfiguration]. The same map used
         * to initialize a [cpgConfiguration] can be re-used here.
         */
        fun from(map: Map<String, Any?>, cpgConfiguration: CPGConfiguration) =
            object {
                    val typestate: TypestateMode by map
                    val spec: List<Path> by map
                    val specDescription: Path by map
                    val disabledSpecRules: List<String> by map
                    val output: Path by map
                    val goodFindings: Boolean by map
                    val pedantic: Boolean by map
                    val timeout: Int by map
                    val executor: Executor? by map

                    val data =
                        Configuration(
                            typestate = typestate,
                            spec = spec,
                            specDescription = specDescription,
                            disabledSpecRules = disabledSpecRules,
                            output = output,
                            goodFindings = goodFindings,
                            pedantic = pedantic,
                            timeout = timeout,
                            executor = executor,
                            cpgConfiguration = cpgConfiguration,
                        )
                }
                .data
    }

    data class CPGConfiguration(
        val source: List<Path>,
        val debugParser: Boolean,
        val loadIncludes: Boolean,
        val includePaths: List<Path>,
        val includeWhitelist: List<Path>,
        val includeBlacklist: List<Path>,
        val disableCleanup: Boolean,
        val codeInNodes: Boolean,
        val processAnnotations: Boolean,
        val failOnError: Boolean,
        val symbols: Map<String, String>,
        val useUnityBuild: Boolean,
        val useParallelFrontends: Boolean,
        val typeSystemActiveInFrontend: Boolean,
        val matchCommentsToNodes: Boolean,
        val passes: List<Pass>,
        val defaultPasses: Boolean,
        val additionalLanguages: Set<Language>,
    ) {
        companion object {
            /** Build a [CPGConfiguration] object from a [map] */
            fun from(map: Map<String, Any?>) =
                object {
                        val source: List<Path> by map
                        val debugParser: Boolean by map
                        val loadIncludes: Boolean by map
                        val includePaths: List<Path> by map
                        val includeWhitelist: List<Path> by map
                        val includeBlacklist: List<Path> by map
                        val disableCleanup: Boolean by map
                        val codeInNodes: Boolean by map
                        val processAnnotations: Boolean by map
                        val failOnError: Boolean by map
                        val symbols: Map<String, String> by map
                        val useUnityBuild: Boolean by map
                        val useParallelFrontends: Boolean by map
                        val typeSystemActiveInFrontend: Boolean by map
                        val matchCommentsToNodes: Boolean by map
                        val passes: List<Pass> by map

                        val defaultPasses: Boolean by map
                        val additionalLanguages: Set<Language> by map

                        val data =
                            CPGConfiguration(
                                source = source,
                                debugParser = debugParser,
                                loadIncludes = loadIncludes,
                                includePaths = includePaths,
                                includeWhitelist = includeWhitelist,
                                includeBlacklist = includeBlacklist,
                                disableCleanup = disableCleanup,
                                codeInNodes = codeInNodes,
                                processAnnotations = processAnnotations,
                                failOnError = failOnError,
                                symbols = symbols,
                                useUnityBuild = useUnityBuild,
                                useParallelFrontends = useParallelFrontends,
                                typeSystemActiveInFrontend = typeSystemActiveInFrontend,
                                matchCommentsToNodes = matchCommentsToNodes,
                                passes = passes,
                                defaultPasses = defaultPasses,
                                additionalLanguages = additionalLanguages
                            )
                    }
                    .data
        }

        /** Return a [TranslationConfiguration] object to pass to the CPG */
        fun toTranslationConfiguration(): TranslationConfiguration {
            val translationConfiguration =
                TranslationConfiguration.builder()
                    .debugParser(debugParser)
                    .loadIncludes(loadIncludes)
                    .codeInNodes(codeInNodes)
                    .processAnnotations(processAnnotations)
                    .failOnError(failOnError)
                    .useParallelFrontends(useParallelFrontends)
                    .typeSystemActiveInFrontend(typeSystemActiveInFrontend)
                    .defaultLanguages()
                    .sourceLocations(source.map { (it.toFile()) })
                    .symbols(symbols)
                    .useUnityBuild(useUnityBuild)
                    .processAnnotations(processAnnotations)

            includePaths.forEach { translationConfiguration.includePath(it.toString()) }
            includeWhitelist.forEach { translationConfiguration.includeWhitelist(it.toString()) }
            includeBlacklist.forEach { translationConfiguration.includeBlacklist(it.toString()) }

            if (disableCleanup) translationConfiguration.disableCleanup()

            if (defaultPasses) translationConfiguration.defaultPasses()
            passes.forEach { translationConfiguration.registerPass(it) }

            // TODO:
            // additionalLanguages.forEach {
            // translationConfiguration.registerLanguage(it.toString()) }
            return translationConfiguration.build()
        }
    }

    /**
     * Return a normalized [Configuration]
     *
     * You should pretty much always only use the normalized [Configuration] to run Codyze.
     * Otherwise, some settings might contradict each other.
     */
    fun normalize(): Configuration {
        var goodFindings = this.goodFindings
        if (this.pedantic and !goodFindings) {
            goodFindings = true // In pedantic analysis mode all findings reported
            logger.info { "Normalized 'goodFindings' to true because 'pedantic' is true" }
        }

        var loadIncludes = this.cpgConfiguration.loadIncludes
        if (this.cpgConfiguration.useUnityBuild and !loadIncludes) {
            loadIncludes =
                true // we need to force load includes for unity builds, otherwise nothing will be
            // parsed
            logger.info { "Normalized 'loadIncludes' to true because 'useUnityBuild' is true" }
        }

        // construct the normalized configuration objects
        val normalizedCpgConfiguration = this.cpgConfiguration.copy(loadIncludes = loadIncludes)
        val normalizedConfiguration =
            this.copy(goodFindings = goodFindings, cpgConfiguration = normalizedCpgConfiguration)
        return normalizedConfiguration
    }

    /** Return an [ExecutorConfiguration] object */
    fun toExecutorConfiguration(): ExecutorConfiguration =
        ExecutorConfiguration(
            typestate = this.typestate,
            spec = this.spec,
            specDescription = this.specDescription,
            disabledSpecRules = this.disabledSpecRules,
            goodFindings = this.goodFindings,
            pedantic = this.pedantic,
            timeout = this.timeout
        )
}

/** A simplified version of the full [Configuration] used for [Executor] initialization */
data class ExecutorConfiguration(
    val typestate: TypestateMode,
    val spec: List<Path>,
    val specDescription: Path,
    val disabledSpecRules: List<String>,
    val goodFindings: Boolean,
    val pedantic: Boolean,
    val timeout: Int,
)
