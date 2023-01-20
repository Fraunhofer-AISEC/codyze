/*
 * Copyright (c) 2023, Fraunhofer AISEC. All rights reserved.
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
package de.fraunhofer.aisec.codyze.backends.cpg

import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.Evaluator
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.Rule
import de.fraunhofer.aisec.cpg.passes.EdgeCachePass
import de.fraunhofer.aisec.cpg.passes.UnreachableEOGPass
import java.nio.file.Path

fun createCpgConfiguration(vararg sourceFile: Path) =
    CPGConfiguration(
        source = listOf(*sourceFile),
        useUnityBuild = false,
        typeSystemActiveInFrontend = true,
        debugParser = false,
        disableCleanup = false,
        codeInNodes = true,
        matchCommentsToNodes = false,
        processAnnotations = false,
        failOnError = false,
        useParallelFrontends = false,
        defaultPasses = true,
        additionalLanguages = setOf(),
        symbols = mapOf(),
        includeBlocklist = listOf(),
        includePaths = listOf(),
        includeAllowlist = listOf(),
        loadIncludes = false,
        passes = listOf(EdgeCachePass(), UnreachableEOGPass()),
    )

fun dummyRule(): Evaluator = TODO()
