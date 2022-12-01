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
@file:Suppress("MagicNumber")

package de.fraunhofer.aisec.codyze.cli

import com.github.ajalt.clikt.parameters.groups.OptionGroup
import de.fraunhofer.aisec.codyze.backends.cpg.CPGConfiguration
import de.fraunhofer.aisec.codyze.backends.cpg.CPGOptionGroup
import de.fraunhofer.aisec.codyze.backends.cpg.coko.CokoCpgBackend
import de.fraunhofer.aisec.codyze.core.Executor
import de.fraunhofer.aisec.codyze.core.wrapper.BackendConfiguration
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.CokoBackend
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.dsl.host.CokoExecutor
import org.koin.dsl.bind
import org.koin.dsl.module

val codyzeModule = module {
    factory { params -> CPGOptionGroup(params.get()) } bind OptionGroup::class
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
            includeBlocklist = params[17],
            typestate = params[18]
        )
    } bind BackendConfiguration::class
    factory { params -> CokoCpgBackend(params.get()) } bind CokoBackend::class
}

val executorModule = module {
    factory { CokoExecutor() } bind Executor::class
}
