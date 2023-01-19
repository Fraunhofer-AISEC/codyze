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

import de.fraunhofer.aisec.codyze.core.backend.Backend

/**
 * This interface should be implemented as a data class to contain all the configuration options of an [Executor].
 * An [ExecutorConfiguration] should also contain the shared codyze options passed to [ExecutorCommand.getExecutor].
 *
 * When using Codyze as a CLI program, the [ExecutorCommand] is responsible to instantiate the
 * respective [Executor]. However, to facilitate the usage of Codyze as a library, an [Executor] should have a
 * configuration object and a [Backend] as its two constructor arguments.
 */
interface ExecutorConfiguration {
    fun normalize(): ExecutorConfiguration
}
