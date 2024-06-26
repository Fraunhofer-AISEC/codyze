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
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import kotlin.io.path.toPath
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ConceptTranslationTest {

    private val sourceFiles = listOfNotNull(
        CokoCpgIntegrationTest::class.java.classLoader
            .getResource("IntegrationTests/CokoCpg/Main.java"),
        CokoCpgIntegrationTest::class.java.classLoader
            .getResource("IntegrationTests/CokoCpg/SimpleOrder.java")
    ).map { it.toURI().toPath() }.also { assertEquals(2, it.size) }

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

    @Test
    fun `test simple concept translation`() {
        val specFiles = listOfNotNull(
            CokoCpgIntegrationTest::class.java.classLoader
                .getResource("concept/bsi-tr.concepts"),
        ).map { it.toURI().toPath() }

        val backend = CokoCpgBackend(cpgConfiguration)
        val specEvaluator = CokoExecutor.compileScriptsIntoSpecEvaluator(backend, specFiles)

        val expectedInterfaceToExpectedMembers = mapOf(
            "Cypher" to listOf("algo", "mode", "keySize", "tagSize"),
            "InitializationVector" to listOf("size"),
            "Encryption" to listOf("cypher", "iv", "encrypt", "decrypt")
        )

        assertEquals(expectedInterfaceToExpectedMembers.size, specEvaluator.types.size)

        for ((expectedInterface, members) in expectedInterfaceToExpectedMembers) {
            val actualInterfaces = specEvaluator.types.filter { it.simpleName?.contains(expectedInterface) ?: false }
            assertEquals(
                1,
                actualInterfaces.size,
                "Found none or more than one actual interface representing the concept \"$expectedInterface\""
            )
            val actualInterface = actualInterfaces.first()

            assertTrue(actualInterface.members.map { it.name }.containsAll(members))
        }
    }

    @Test
    fun `test concept translation with op pointer`() {
        val specFiles = listOfNotNull(
            CokoCpgIntegrationTest::class.java.classLoader
                .getResource("concept/some.concepts"),
        ).map { it.toURI().toPath() }

        val backend = CokoCpgBackend(cpgConfiguration)
        val specEvaluator = CokoExecutor.compileScriptsIntoSpecEvaluator(backend, specFiles)

        val expectedInterfaceToExpectedMembers = mapOf(
            "Logging" to listOf("log", "info", "warn", "error"),
            "Database" to listOf("init", "insert"),
            "UserContext" to listOf("user")
        )

        assertEquals(3, specEvaluator.types.size)
        for ((expectedInterface, members) in expectedInterfaceToExpectedMembers) {
            val actualInterfaces = specEvaluator.types.filter { it.simpleName?.contains(expectedInterface) ?: false }
            assertEquals(
                1,
                actualInterfaces.size,
                "Found none or more than one actual interface representing the concept \"$expectedInterface\""
            )
            val actualInterface = actualInterfaces.first()
            assertTrue(actualInterface.members.map { it.name }.containsAll(members))

            if (expectedInterface == "Logging") {
                val log = actualInterface.members.firstOrNull { it.name == "log" }
                assertNotNull(log)
                assertFalse(log.isAbstract)
            }
        }
    }

    @Test
    fun `test concept in combination with coko scripts`() {
        val specFiles = listOfNotNull(
            CokoCpgIntegrationTest::class.java.classLoader
                .getResource("concept/followedBy.concepts"),
            CokoCpgIntegrationTest::class.java.classLoader
                .getResource("concept/followedByImplementations.codyze.kts"),
            CokoCpgIntegrationTest::class.java.classLoader
                .getResource("concept/followedByRule.codyze.kts"),
        ).map { it.toURI().toPath() }

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
        assertEquals(1, run.results?.size)
    }
}
