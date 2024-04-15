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
package de.fraunhofer.aisec.codyze.core.executor

import com.github.ajalt.clikt.core.*
import de.fraunhofer.aisec.codyze.core.backend.Backend
import de.fraunhofer.aisec.codyze.core.backend.BackendCommand
import de.fraunhofer.aisec.codyze.core.backend.BackendOptions
import org.koin.java.KoinJavaComponent.getKoin

/**
 * This abstract class must be implemented by all [Executor]s that want to be selectable in the codyze-cli.
 * Remember to add the newly created [ExecutorCommand] to the dependency injection.
 */
abstract class ExecutorCommand<T : Executor>(cliName: String? = null) :
    NoOpCliktCommand(hidden = true, name = cliName) {

    /** Use the global context set in [CodyzeCli] */
    private val usedExecutors by findOrSetObject { mutableListOf<ExecutorCommand<*>>() }

    abstract fun getExecutor(goodFindings: Boolean, pedantic: Boolean, backend: Backend?): T

    /**
     * This should be called in each [ExecutorCommand] to register possible backends.
     * The registered [BackendOptions] are filtered such that only [BackendOptions] providing
     * a backend of type [T] are registered.
     */
    inline fun <reified T> registerBackendOptions() {
        subcommands(getKoin().getAll<BackendCommand<*>>().filter { it.backend == T::class })
    }

    override fun run() {
        usedExecutors += this
    }
}
