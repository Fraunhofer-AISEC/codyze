@file:Suppress("UNUSED")

package de.fraunhofer.aisec.codyze_backends.cpg

import de.fraunhofer.aisec.codyze_core.wrapper.Backend
import de.fraunhofer.aisec.codyze_core.wrapper.BackendConfiguration
import de.fraunhofer.aisec.cpg.TranslationConfiguration
import de.fraunhofer.aisec.cpg.TranslationManager

open class CPGBackend(config: BackendConfiguration) : Backend {
    override val graph: Any by lazy {
        TranslationManager.builder()
            .config(config = (config as CPGConfiguration).toTranslationConfiguration())
            .build() // Initialize the CPG, based on the given Configuration
            .analyze()
            .get()
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
        source.firstOrNull()?.parent?.toFile().let { translationConfiguration.topLevel(it) }

        includePaths.forEach { translationConfiguration.includePath(it.toString()) }
        includeWhitelist.forEach { translationConfiguration.includeWhitelist(it.toString()) }
        includeBlocklist.forEach { translationConfiguration.includeBlocklist(it.toString()) }

        if (disableCleanup) translationConfiguration.disableCleanup()

        if (defaultPasses) translationConfiguration.defaultPasses()
        passes.forEach { translationConfiguration.registerPass(it) }

        translationConfiguration.optionalLanguage(
            "de.fraunhofer.aisec.cpg.frontends.python.PythonLanguage"
        )
        translationConfiguration.optionalLanguage(
            "de.fraunhofer.aisec.cpg.frontends.golang.GoLanguage"
        )
        translationConfiguration.optionalLanguage(
            "de.fraunhofer.aisec.cpg.frontends.llvm.LLVMIRLanguage"
        )
        translationConfiguration.optionalLanguage(
            "de.fraunhofer.aisec.cpg.frontends.typescript.TypeScriptLanguage"
        )

        additionalLanguages.forEach { translationConfiguration.registerLanguage(it) }

        return translationConfiguration.build()
    }
}
