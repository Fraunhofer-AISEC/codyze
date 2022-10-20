@file:Suppress("UNUSED")
package de.fraunhofer.aisec.codyze.backends.cpg

import de.fraunhofer.aisec.codyze_core.wrapper.BackendManager
import de.fraunhofer.aisec.cpg.TranslationConfiguration
import de.fraunhofer.aisec.cpg.TranslationManager

open class CPGManager(config: CPGConfiguration): BackendManager {
    private val translationManager: TranslationManager
    override lateinit var cpg: Any

    init {
        translationManager = TranslationManager.builder()
            .config(config = config.toTranslationConfiguration())
            .build() // Initialize the CPG, based on the given Configuration
    }

    /** Return a [TranslationConfiguration] object to pass to the CPG */
    private fun CPGConfiguration.toTranslationConfiguration(): TranslationConfiguration {
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

        // TODO: very hacky, but needed for the Go frontend
        translationConfiguration.topLevel(source.first().parent.toFile())

        includePaths.forEach { translationConfiguration.includePath(it.toString()) }
        includeWhitelist.forEach { translationConfiguration.includeWhitelist(it.toString()) }
        includeBlacklist.forEach { translationConfiguration.includeBlacklist(it.toString()) }

        if (disableCleanup) translationConfiguration.disableCleanup()

        if (defaultPasses) translationConfiguration.defaultPasses()
        passes.forEach { translationConfiguration.registerPass(it) }

        additionalLanguages.forEach {
            val (clazz, types) = it
            translationConfiguration.registerLanguage(clazz, types)
        }

        return translationConfiguration.build()
    }

    override fun initialize() {
        cpg = translationManager.analyze().get()
    }
}
