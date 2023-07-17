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
package de.fraunhofer.aisec.codyze.backends.cpg

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class CPGConfigurationTest {

    @Test
    fun `test normalize`() {
        val expectedCpgConfiguration = CPGConfiguration(
            source = listOf(),
            useUnityBuild = true,
            typeSystemActiveInFrontend = false,
            debugParser = false,
            disableCleanup = false,
            codeInNodes = true,
            matchCommentsToNodes = true,
            processAnnotations = false,
            failOnError = false,
            useParallelFrontends = false,
            defaultPasses = true,
            additionalLanguages = setOf(),
            symbols = mapOf(),
            passes = listOf(),
            loadIncludes = true,
            includePaths = listOf(),
            includeAllowlist = listOf(),
            includeBlocklist = listOf()
        )

        val cpgConfiguration = CPGConfiguration(
            source = listOf(),
            useUnityBuild = true,
            typeSystemActiveInFrontend = false,
            debugParser = false,
            disableCleanup = false,
            codeInNodes = true,
            matchCommentsToNodes = true,
            processAnnotations = false,
            failOnError = false,
            useParallelFrontends = false,
            defaultPasses = true,
            additionalLanguages = setOf(),
            symbols = mapOf(),
            passes = listOf(),
            loadIncludes = false,
            includePaths = listOf(),
            includeAllowlist = listOf(),
            includeBlocklist = listOf()
        )

        val actualCPGConfiguration = cpgConfiguration.normalize()

        assertEquals(expectedCpgConfiguration, actualCPGConfiguration)
    }

    @Test
    fun `test normalize with nothing to normalize`() {
        val expectedCpgConfiguration = CPGConfiguration(
            source = listOf(),
            useUnityBuild = false,
            typeSystemActiveInFrontend = false,
            debugParser = false,
            disableCleanup = false,
            codeInNodes = true,
            matchCommentsToNodes = true,
            processAnnotations = false,
            failOnError = false,
            useParallelFrontends = false,
            defaultPasses = true,
            additionalLanguages = setOf(),
            symbols = mapOf(),
            passes = listOf(),
            loadIncludes = true,
            includePaths = listOf(),
            includeAllowlist = listOf(),
            includeBlocklist = listOf()
        )

        val actualCPGConfiguration = expectedCpgConfiguration.normalize()

        assertEquals(expectedCpgConfiguration, actualCPGConfiguration)
    }
}