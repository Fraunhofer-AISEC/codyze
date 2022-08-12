package de.fraunhofer.aisec.codyze

import com.github.ajalt.clikt.core.CliktCommand
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_dsl.host.CokoExecutor
import de.fraunhofer.aisec.codyze.specification_languages.mark.MarkExecutor
import de.fraunhofer.aisec.codyze.specification_languages.nwt.NwtExecutor
import de.fraunhofer.aisec.codyze_core.Executor
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

// Constructor DSL
val executorModule = module {
    factoryOf(::MarkExecutor) bind Executor::class
    factoryOf(::NwtExecutor) bind Executor::class
    factoryOf(::CokoExecutor) bind Executor::class
}

val subcommandModule = module {
    singleOf(::Analyze) bind CliktCommand::class
    singleOf(::Interactive) bind CliktCommand::class
    singleOf(::LSP) bind CliktCommand::class
}
