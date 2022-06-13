package de.fraunhofer.aisec.codyze

import com.github.ajalt.clikt.core.CliktCommand
import de.fraunhofer.aisec.codyze_core.Executor
// import de.fraunhofer.aisec.codyze.specification_languages.mark.MarkExecutor
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module


// Constructor DSL
val executorModule = module {
    // factoryOf(::MarkExecutor) bind Executor::class
}

val subcommandModule = module {
    singleOf(::Analyze) bind CliktCommand::class
    singleOf(::Interactive) bind CliktCommand::class
    singleOf(::LSP) bind CliktCommand::class
}