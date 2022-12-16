/*
 * Copyright (c) 2022, Fraunhofer AISEC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fraunhofer.aisec.codyze.cli

import de.fraunhofer.aisec.codyze.backends.cpg.cli.BaseCpgBackend
import de.fraunhofer.aisec.codyze.backends.cpg.cli.CokoCpgBackend
import de.fraunhofer.aisec.codyze.core.wrapper.BackendCommand
import de.fraunhofer.aisec.codyze.core.wrapper.ExecutorCommand
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.dsl.cli.CokoSubcommand
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Every [Backend] must provide [BackendCommand] to be selectable in the CLI.
 */
val backendCommands = module {
    factoryOf(::BaseCpgBackend) bind(BackendCommand::class)
    factoryOf(::CokoCpgBackend) bind(BackendCommand::class)
}

/**
 * Each [Executor] must provide a [ExecutorCommand] to be selectable in the CLI.
 */
val executorCommands = module {
    factoryOf(::CokoSubcommand) bind(ExecutorCommand::class)
}
