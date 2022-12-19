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
package de.fraunhofer.aisec.codyze.core.wrapper

import com.github.ajalt.clikt.core.NoOpCliktCommand
import com.github.ajalt.clikt.core.subcommands
import de.fraunhofer.aisec.codyze.core.Executor
import de.fraunhofer.aisec.codyze.core.config.Configuration
import org.koin.java.KoinJavaComponent

/** Contains all options that are shared among all Codyze subcommands. */
abstract class ExecutorCommand<T : Executor>(cliName: String? = null) :
    NoOpCliktCommand(hidden = true, name = cliName) {

    abstract fun getExecutor(codyzeConfiguration: Configuration, backend: Backend): T

    /**
     * This should be called in each [ExecutorCommand] to register possible backends.
     * The registered [BackendOptions] are filtered such that only [BackendOptions] providing
     * a backend of type [T] are registered.
     */
    inline fun <reified T> registerBackendOptions() {
        subcommands(KoinJavaComponent.getKoin().getAll<BackendCommand<*>>().filter { it.backend == T::class })
    }
}
