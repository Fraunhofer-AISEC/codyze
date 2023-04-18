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
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.nio.file.Path
import java.util.stream.Stream
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
                .getResource("IntegrationTests/CokoCpg/orderFull.codyze.kts"),
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
    fun `test coko with cpg backend and dependent spec files`() {
        val specFiles = listOfNotNull(
            CokoCpgIntegrationTest::class.java.classLoader
                .getResource("IntegrationTests/CokoCpg/followedByTwoFiles/followedByImplementations.codyze.kts"),
            CokoCpgIntegrationTest::class.java.classLoader
                .getResource("IntegrationTests/CokoCpg/followedByTwoFiles/followedByModels.codyze.kts")
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
     * Uses three spec files which are given through `specFiles`. One of the three files imports another script file.
     * The order of the files in `specFiles` is permuted to verify that the order in which the spec files are evaluated
     * does not have an impact on the results.
     */
    @ParameterizedTest(name = "{index} {1}")
    @MethodSource("threeFiles")
    fun `test coko with cpg backend and permutation of three dependent spec files`(
        specFiles: List<Path>,
        fileNames: List<String>
    ) {
        assertEquals(3, specFiles.size)

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
        assertEquals(run.results?.size, 16)
    }

    /**
     * Performs an end-to-end integration test of Codyze with the [CokoExecutor] and the [CokoCpgBackend] backend.
     * Uses four spec files which are given through `specFiles`. There are two files which each import another file.
     * The order of the files in `specFiles` is permuted to verify that the order in which the spec files are evaluated
     * does not have an impact on the results.
     */
    @ParameterizedTest(name = "{index} {1}")
    @MethodSource("fourFiles")
    fun `test coko with cpg backend and permutation of four dependent spec files`(
        specFiles: List<Path>,
        fileNames: List<String>
    ) {
        assertEquals(4, specFiles.size)
        
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
        assertEquals(run.results?.size, 16)
    }

    /**
     * Performs an end-to-end integration test of Codyze with the [CokoExecutor] and the [CokoCpgBackend] backend.
     * Uses four spec fileswhich are given through `specFiles`. There are three files which each import another file.
     * Two of these three files import the same file.
     * The order of the files in `specFiles` is permuted to verify that the order in which the spec files are evaluated
     * does not have an impact on the results.
     */
    @Disabled("Too many permutations (120) of the specFiles order")
    @ParameterizedTest(name = "{index} {1}")
    @MethodSource("fiveFiles")
    fun `test coko with cpg backend and permutation of five dependent spec files`(
        specFiles: List<Path>,
        fileNames: List<String>
    ) {
        assertEquals(5, specFiles.size)

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
        assertEquals(run.results?.size, 16)
    }

    /**
     * Performs an end-to-end integration test of Codyze with the [CokoExecutor] and the [CokoCpgBackend] backend.
     * And checks that only 'fail' findings are reported.
     */
    @Test
    fun `test coko with cpg backend without good findings`() {
        val specFiles = listOfNotNull(
            CokoCpgIntegrationTest::class.java.classLoader.getResource("IntegrationTests/CokoCpg/orderFull.codyze.kts"),
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

    companion object {
        @JvmStatic
        fun threeFiles(): Stream<Arguments> {
            val stream = Stream.builder<Arguments>()
            val fileMap = mapOf(
                '1' to CokoCpgIntegrationTest::class.java.classLoader
                    .getResource("IntegrationTests/CokoCpg/orderFull.codyze.kts"),
                '2' to CokoCpgIntegrationTest::class.java.classLoader
                    .getResource("IntegrationTests/CokoCpg/followedByTwoFiles/followedByImplementations.codyze.kts"),
                '3' to CokoCpgIntegrationTest::class.java.classLoader
                    .getResource("IntegrationTests/CokoCpg/followedByTwoFiles/followedByModels.codyze.kts"),
            )
            val permutations = fileMap.permutate()
            for (p in permutations) {
                val (specFiles, fileNames) = p.map { Path(it.path) }.map { it to it.fileName }.unzip()
                stream.add(
                    Arguments.of(
                        specFiles,
                        fileNames
                    )
                )
            }
            return stream.build()
        }

        @JvmStatic
        fun fourFiles(): Stream<Arguments> {
            val stream = Stream.builder<Arguments>()
            val fileMap = mapOf(
                '1' to CokoCpgIntegrationTest::class.java.classLoader
                    .getResource("IntegrationTests/CokoCpg/orderTwoFiles/orderRule.codyze.kts"),
                '2' to CokoCpgIntegrationTest::class.java.classLoader
                    .getResource("IntegrationTests/CokoCpg/followedByTwoFiles/followedByImplementations.codyze.kts"),
                '3' to CokoCpgIntegrationTest::class.java.classLoader
                    .getResource("IntegrationTests/CokoCpg/followedByTwoFiles/followedByModels.codyze.kts"),
                '4' to CokoCpgIntegrationTest::class.java.classLoader
                    .getResource("IntegrationTests/CokoCpg/orderTwoFiles/orderImplementations.codyze.kts"),
            )
            val permutations = fileMap.permutate()
            for (p in permutations) {
                val (specFiles, fileNames) = p.map { Path(it.path) }.map { it to it.fileName }.unzip()
                stream.add(
                    Arguments.of(
                        specFiles,
                        fileNames
                    )
                )
            }
            return stream.build()
        }

        @JvmStatic
        fun fiveFiles(): Stream<Arguments> {
            val stream = Stream.builder<Arguments>()
            val fileMap = mapOf(
                '1' to CokoCpgIntegrationTest::class.java.classLoader
                    .getResource("IntegrationTests/CokoCpg/orderTwoFiles/orderRule.codyze.kts"),
                '2' to CokoCpgIntegrationTest::class.java.classLoader
                    .getResource("IntegrationTests/CokoCpg/followedByThreeFiles/followedByImplementations.codyze.kts"),
                '3' to CokoCpgIntegrationTest::class.java.classLoader
                    .getResource("IntegrationTests/CokoCpg/followedByThreeFiles/followedByRule.codyze.kts"),
                '4' to CokoCpgIntegrationTest::class.java.classLoader
                    .getResource("IntegrationTests/CokoCpg/followedByThreeFiles/followedByInterfaces.codyze.kts"),
                '5' to CokoCpgIntegrationTest::class.java.classLoader
                    .getResource("IntegrationTests/CokoCpg/orderTwoFiles/orderImplementations.codyze.kts"),
            )
            val permutations = fileMap.permutate()
            for (p in permutations) {
                val (specFiles, fileNames) = p.map { Path(it.path) }.map { it to it.fileName }.unzip()
                stream.add(
                    Arguments.of(
                        specFiles,
                        fileNames
                    )
                )
            }
            return stream.build()
        }

        private fun <E> Map<Char, E?>.permutate(): List<List<E>> {
            val key = String(this.keys.toCharArray())
            val keyPermutations = key.permute()

            val permutations = mutableListOf<List<E>>()

            for (p in keyPermutations) {
                permutations.add(p.mapNotNull { this[it] })
            }

            return permutations
        }

        private fun String.permute(result: String = ""): List<String> =
            if (isEmpty()) listOf(result) else flatMapIndexed { i, c -> removeRange(i, i + 1).permute(result + c) }
    }
}
