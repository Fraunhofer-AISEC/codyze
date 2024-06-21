/*
 * Copyright (c) 2024, Fraunhofer AISEC. All rights reserved.
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
package de.fraunhofer.aisec.codyze.specificationLanguage.cpg.native

import com.github.ajalt.clikt.parameters.groups.provideDelegate
import de.fraunhofer.aisec.codyze.backends.cpg.CPGBackend
import de.fraunhofer.aisec.codyze.core.backend.Backend
import de.fraunhofer.aisec.codyze.core.executor.ExecutorCommand

@Suppress("UNUSED")
class CPGQuerySubcommand : ExecutorCommand<CPGQueryExecutor>("runNativeQueries") {
    val executorOptions by CPGQueryOptionGroup()

    init {
        // allow only the backends that implement the [CokoBackend] interface as subcommands
        registerBackendOptions<CPGBackend>()
    }

    override fun getExecutor(goodFindings: Boolean, pedantic: Boolean, backend: Backend?) = with(executorOptions) {
        CPGQueryExecutor(CPGQueryConfiguration(true), backend as CPGBackend)
    }
}
