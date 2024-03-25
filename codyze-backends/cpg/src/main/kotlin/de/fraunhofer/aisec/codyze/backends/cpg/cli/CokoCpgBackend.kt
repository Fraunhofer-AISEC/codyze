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
package de.fraunhofer.aisec.codyze.backends.cpg.cli

import com.github.ajalt.clikt.parameters.groups.provideDelegate
import de.fraunhofer.aisec.codyze.backends.cpg.CPGConfiguration
import de.fraunhofer.aisec.codyze.backends.cpg.CPGOptionGroup
import de.fraunhofer.aisec.codyze.backends.cpg.coko.CokoCpgBackend
import de.fraunhofer.aisec.codyze.core.backend.BackendCommand
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.CokoBackend

/**
 * The [CliktCommand] to add the cokoCpg backend to the codyze-cli.
 */
class CokoCpgBackend : BackendCommand<CokoBackend>("cokoCpg") {
    val backendOptions by CPGOptionGroup()
    override val backend = CokoBackend::class

    override fun getBackend() = with(backendOptions) {
        CokoCpgBackend(
            CPGConfiguration(
                source = source,
                useUnityBuild = useUnityBuild,
                debugParser = debugParser,
                disableCleanup = disableCleanup,
                codeInNodes = codeInNodes,
                matchCommentsToNodes = matchCommentsToNodes,
                processAnnotations = processAnnotations,
                failOnError = failOnError,
                useParallelFrontends = useParallelFrontends,
                defaultPasses = defaultPasses,
                additionalLanguages = additionalLanguages,
                symbols = symbols,
                passes = passes,
                loadIncludes = loadIncludes,
                includePaths = includePaths,
                includeAllowlist = includeAllowlist,
                includeBlocklist = includeBlocklist,
            ).normalize()
        )
    }
}
