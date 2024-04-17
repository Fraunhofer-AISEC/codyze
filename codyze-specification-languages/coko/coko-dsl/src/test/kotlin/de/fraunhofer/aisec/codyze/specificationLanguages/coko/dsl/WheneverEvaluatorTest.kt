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
package de.fraunhofer.aisec.codyze.specificationLanguages.coko.dsl

import de.fraunhofer.aisec.codyze.backends.cpg.CPGConfiguration
import de.fraunhofer.aisec.codyze.backends.cpg.coko.CokoCpgBackend
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.dsl.host.CokoExecutor
import de.fraunhofer.aisec.cpg.passes.EdgeCachePass
import de.fraunhofer.aisec.cpg.passes.UnreachableEOGPass
import io.github.detekt.sarif4k.ResultKind
import org.junit.jupiter.api.Test
import kotlin.io.path.toPath
import kotlin.test.assertEquals

// TODO: should probably in codyze-backends or coko-core
class WheneverEvaluatorTest {

    private val sourceFiles = listOfNotNull(
        CokoCpgIntegrationTest::class.java.classLoader
            .getResource("concept/CipherTestFile.java"),
    ).map { it.toURI().toPath() }.also { assertEquals(1, it.size) }

    val cpgConfiguration =
        CPGConfiguration(
            source = sourceFiles,
            useUnityBuild = false,
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

    /**
     * Performs an end-to-end integration test of Codyze with the [CokoExecutor] and the [CokoCpgBackend] backend.
     */
    @Test
    fun `test coko with cpg backend and multiple spec files`() {
        val specFiles = listOfNotNull(
            CokoCpgIntegrationTest::class.java.classLoader
                .getResource("concept/bsi-tr-rules.codyze.kts"),
            CokoCpgIntegrationTest::class.java.classLoader
                .getResource("concept/jca-cipher.codyze.kts")
        ).map { it.toURI().toPath() }.also { assertEquals(2, it.size) }

        val cokoConfiguration =
            CokoConfiguration(
                goodFindings = true,
                pedantic = false,
                spec = specFiles,
                disabledSpecRules = emptyList(),
            )

        val backend = CokoCpgBackend(cpgConfiguration)
        val executor = CokoExecutor(cokoConfiguration, backend)

        val run = executor.evaluate()

        // assertions for the order rule
        assertEquals(1, run.results?.size, "Expected to find one result but was ${run.results?.size}")
        assertEquals(ResultKind.Fail, run.results?.firstOrNull()?.kind, "Result was not fail")
    }
}
