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
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.io.path.*
import kotlin.test.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CokoCpgIntegrationTest {

    private val sourceFiles = listOfNotNull(
        CokoCpgIntegrationTest::class.java.classLoader
            .getResource("IntegrationTests/CokoCpg/Main.java"),
        CokoCpgIntegrationTest::class.java.classLoader
            .getResource("IntegrationTests/CokoCpg/SimpleOrder.java")
    ).map { Path(it.path) }.also { assertEquals(2, it.size) }

    val cpgConfiguration =
        CPGConfiguration(
            source = sourceFiles,
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
            passes = listOf(UnreachableEOGPass(), EdgeCachePass()),
        )

    /**
     * Performs an end-to-end integration test of Codyze with the [CokoExecutor] and the [CokoCpgBackend] backend.
     */
    @Test
    fun `test coko with cpg backend and multiple spec files`() {
        val specFiles = listOfNotNull(
            CokoCpgIntegrationTest::class.java.classLoader
                .getResource("IntegrationTests/CokoCpg/orderRule.codyze.kts"),
            CokoCpgIntegrationTest::class.java.classLoader
                .getResource("IntegrationTests/CokoCpg/followedByFull.codyze.kts")
        ).map { Path(it.path) }.also { assertEquals(2, it.size) }

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
        assertEquals(run.results?.size, 16)
    }

    /**
     * Performs an end-to-end integration test of Codyze with the [CokoExecutor] and the [CokoCpgBackend] backend.
     * And uses two spec files where one imports the other.
     */
    @Test
    fun `test coko with cpg backend and dependend spec files`() {
        val specFiles = listOfNotNull(
            CokoCpgIntegrationTest::class.java.classLoader
                .getResource("IntegrationTests/CokoCpg/followedBy.codyze.kts"),
            CokoCpgIntegrationTest::class.java.classLoader
                .getResource("IntegrationTests/CokoCpg/followedByModels.codyze.kts")
        ).map { Path(it.path) }.also { assertEquals(2, it.size) }

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
        assertEquals(run.results?.size, 1)
    }

    /**
     * Performs an end-to-end integration test of Codyze with the [CokoExecutor] and the [CokoCpgBackend] backend.
     * Uses three spec files. Where the first one imports the second one and the last one is standalone.
     */
    @Test
    fun `test coko with cpg backend and multiple and multiple dependend spec files`() {
        val specFiles = listOfNotNull(
            CokoCpgIntegrationTest::class.java.classLoader
                .getResource("IntegrationTests/CokoCpg/followedBy.codyze.kts"),
            CokoCpgIntegrationTest::class.java.classLoader
                .getResource("IntegrationTests/CokoCpg/followedByModels.codyze.kts"),
            CokoCpgIntegrationTest::class.java.classLoader
                .getResource("IntegrationTests/CokoCpg/orderRule.codyze.kts"),
        ).map { Path(it.path) }.also { assertEquals(3, it.size) }

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
        assertEquals(run.results?.size, 16)
    }

    /**
     * Performs an end-to-end integration test of Codyze with the [CokoExecutor] and the [CokoCpgBackend] backend.
     * Uses three spec files. Where the second one imports the third one and the first one is standalone.
     */
    @Disabled("Produces an argument type mismatch error. Probably a bug in the SpecEvaluator...")
    @Test
    fun `test coko with cpg backend and multiple and multiple dependend spec files two`() {
        val specFiles = listOfNotNull(
            CokoCpgIntegrationTest::class.java.classLoader
                .getResource("IntegrationTests/CokoCpg/orderRule.codyze.kts"),
            CokoCpgIntegrationTest::class.java.classLoader
                .getResource("IntegrationTests/CokoCpg/followedBy.codyze.kts"),
            CokoCpgIntegrationTest::class.java.classLoader
                .getResource("IntegrationTests/CokoCpg/followedByModels.codyze.kts"),
        ).map { Path(it.path) }.also { assertEquals(3, it.size) }

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
        assertEquals(run.results?.size, 16)
    }

    /**
     * Performs an end-to-end integration test of Codyze with the [CokoExecutor] and the [CokoCpgBackend] backend.
     * And checks that only 'fail' findings are reported.
     */
    @Test
    fun `test coko with cpg backend without good findings`() {
        val specFiles = listOfNotNull(
            CokoCpgIntegrationTest::class.java.classLoader.getResource("IntegrationTests/CokoCpg/orderRule.codyze.kts"),
        ).map { Path(it.path) }.also { assertEquals(1, it.size) }

        val cokoConfiguration =
            CokoConfiguration(
                goodFindings = false,
                pedantic = false,
                spec = specFiles,
                disabledSpecRules = emptyList(),
            )

        val backend = CokoCpgBackend(cpgConfiguration)
        val executor = CokoExecutor(cokoConfiguration, backend)

        val run = executor.evaluate()
        assertEquals(7, run.results?.size)
    }
}
