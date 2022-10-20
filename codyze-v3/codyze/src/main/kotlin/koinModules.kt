package de.fraunhofer.aisec.codyze

import com.github.ajalt.clikt.parameters.groups.OptionGroup
import de.fraunhofer.aisec.codyze.backends.cpg.*
import de.fraunhofer.aisec.codyze.backends.cpg.coko.CokoCpgManager
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.CokoBackendManager
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_dsl.host.CokoExecutor
import de.fraunhofer.aisec.codyze_core.Executor
import de.fraunhofer.aisec.codyze_core.wrapper.BackendConfiguration
import de.fraunhofer.aisec.codyze_core.wrapper.BackendManager
import org.koin.dsl.bind
import org.koin.dsl.module

val codyzeModule = module {
    factory { params -> CPGOptions(params.get()) } bind OptionGroup::class
    factory { params ->
        CPGConfiguration(
            source = params.get(),
            useUnityBuild = params.get(),
            typeSystemActiveInFrontend = params.get(),
            debugParser = params.get(),
            disableCleanup = params.get(),
            codeInNodes = params.get(),
            matchCommentsToNodes = params.get(),
            processAnnotations = params.get(),
            failOnError = params.get(),
            useParallelFrontends = params.get(),
            defaultPasses = params.get(),
            additionalLanguages = params.get(),
            symbols = params.get(),
            passes = params.get(),
            loadIncludes = params.get(),
            includePaths = params.get(),
            includeWhitelist = params.get(),
            includeBlacklist = params.get(),
            typestate = params.get()
        )
    } bind BackendConfiguration::class
    factory { CPGManager(get()) } bind BackendManager::class
    factory { CokoCpgManager(get())} bind CokoBackendManager::class
}

val executorModule = module {
    // factoryOf(::MarkExecutor) bind Executor::class
    factory { CokoExecutor() } bind Executor::class
}
