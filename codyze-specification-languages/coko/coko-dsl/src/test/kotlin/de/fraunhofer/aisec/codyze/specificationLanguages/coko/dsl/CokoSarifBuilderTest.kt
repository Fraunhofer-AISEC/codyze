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
package de.fraunhofer.aisec.codyze.specificationLanguages.coko.dsl

import de.fraunhofer.aisec.codyze.backends.cpg.CPGConfiguration
import de.fraunhofer.aisec.codyze.backends.cpg.coko.CokoCpgBackend
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.dsl.host.CokoExecutor
import de.fraunhofer.aisec.cpg.passes.EdgeCachePass
import de.fraunhofer.aisec.cpg.passes.UnreachableEOGPass
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.io.path.Path

class CokoSarifBuilderTest {

    private val cpgConfiguration =
        CPGConfiguration(
            source = emptyList(),
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
            passes = listOf(UnreachableEOGPass::class, EdgeCachePass::class),
        )

    @Test
    fun `test empty rules list causes empty reportingDescriptors list`() {
        val backend = CokoCpgBackend(cpgConfiguration)
        val csb = CokoSarifBuilder(rules = emptyList(), backend = backend)

        assertTrue(csb.reportingDescriptors.isEmpty())
    }

    @Test
    fun `test rules with empty tags`() {
        val specFiles = listOfNotNull(
            CokoSarifBuilderTest::class.java.classLoader
                .getResource("sarif/ruletagsempty.codyze.kts")
        ).map { Path(it.path) }

        val backend = CokoCpgBackend(cpgConfiguration)
        val specEvaluator = CokoExecutor.compileScriptsIntoSpecEvaluator(backend = backend, specFiles = specFiles)
        val csb = CokoSarifBuilder(rules = specEvaluator.rules, backend = backend)

        val alternative = csb.reportingDescriptors.first().properties?.tags?.let { assertTrue(it.isEmpty()) }
        assertNotNull(alternative)
    }

    @Test
    fun `test rules with some tags`() {
        val specFiles = listOfNotNull(
            CokoSarifBuilderTest::class.java.classLoader
                .getResource("sarif/ruletagsnonempty.codyze.kts")
        ).map { Path(it.path) }

        val backend = CokoCpgBackend(cpgConfiguration)
        val specEvaluator = CokoExecutor.compileScriptsIntoSpecEvaluator(backend = backend, specFiles = specFiles)
        val csb = CokoSarifBuilder(rules = specEvaluator.rules, backend = backend)

        val alternative = csb.reportingDescriptors.first().properties?.tags?.let { assertTrue(it.isNotEmpty()) }
        assertNotNull(alternative)
    }
}
