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
package de.fraunhofer.aisec.codyze.specificationLanguages.coko.dsl.cli

import com.github.ajalt.clikt.parameters.groups.provideDelegate
import de.fraunhofer.aisec.codyze.core.backend.Backend
import de.fraunhofer.aisec.codyze.core.executor.ExecutorCommand
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.CokoBackend
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.dsl.CokoConfiguration
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.dsl.host.CokoExecutor

@Suppress("UNUSED")
class CokoSubcommand : ExecutorCommand<CokoExecutor>("runCoko") {
    val executorOptions by CokoOptionGroup()

    init {
        // allow only the backends that implement the [CokoBackend] interface as subcommands
        registerBackendOptions<CokoBackend>()
    }

    override fun getExecutor(goodFindings: Boolean, pedantic: Boolean, backend: Backend?) = with(executorOptions) {
        CokoExecutor(
            CokoConfiguration(
                goodFindings = goodFindings,
                pedantic = pedantic,
                spec = spec,
                disabledSpecRules = disabledSpecRules,
            ),
            backend as CokoBackend
        )
    }
}
