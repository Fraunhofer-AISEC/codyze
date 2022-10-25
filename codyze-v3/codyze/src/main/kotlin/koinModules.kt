package de.fraunhofer.aisec.codyze

import com.github.ajalt.clikt.parameters.groups.OptionGroup
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.CokoBackend
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_dsl.host.CokoExecutor
import de.fraunhofer.aisec.codyze_backends.cpg.CPGConfiguration
import de.fraunhofer.aisec.codyze_backends.cpg.CPGBackend
import de.fraunhofer.aisec.codyze_backends.cpg.CPGOptions
import de.fraunhofer.aisec.codyze_backends.cpg.coko.CokoCpgBackend
import de.fraunhofer.aisec.codyze_core.Executor
import de.fraunhofer.aisec.codyze_core.wrapper.BackendConfiguration
import de.fraunhofer.aisec.codyze_core.wrapper.Backend
import org.koin.dsl.bind
import org.koin.dsl.module

val codyzeModule = module {
    factory { params -> CPGOptions(params.get()) } bind OptionGroup::class
    factory { params ->
        CPGConfiguration(
            source = params[0],
            useUnityBuild = params[1],
            typeSystemActiveInFrontend = params[2],
            debugParser = params[3],
            disableCleanup = params[4],
            codeInNodes = params[5],
            matchCommentsToNodes = params[6],
            processAnnotations = params[7],
            failOnError = params[8],
            useParallelFrontends = params[9],
            defaultPasses = params[10],
            additionalLanguages = params[11],
            symbols = params[12],
            passes = params[13],
            loadIncludes = params[14],
            includePaths = params[15],
            includeWhitelist = params[16],
            includeBlacklist = params[17],
            typestate = params[18]
        )
    } bind BackendConfiguration::class
    factory { params -> CPGBackend(params.get()) } bind Backend::class
    factory { params -> CokoCpgBackend(params.get()) } bind CokoBackend::class
}

val executorModule = module {
    // factoryOf(::MarkExecutor) bind Executor::class
    factory { CokoExecutor() } bind Executor::class
}
