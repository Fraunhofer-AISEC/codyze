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
package de.fraunhofer.aisec.codyze.plugins

import de.fraunhofer.aisec.codyze.core.output.aggregator.extractLastRun
import de.fraunhofer.aisec.codyze.core.plugin.Plugin
import io.github.detekt.sarif4k.Result
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.io.path.deleteExisting
import kotlin.io.path.toPath
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

abstract class PluginTest {
    abstract val plugin: Plugin
    abstract val resultFileName: String
    open val expectedSuccess: Boolean = true
    abstract val expectedResults: List<Result>

    val reportUri by lazy {
        // use an existing anchor in the resources directory
        PluginTest::class.java.classLoader.getResource("targets")!!.toURI()
            .resolve("..")
            .resolve("generatedReports")
            .resolve(resultFileName)
    }

    @Test
    fun testResults() {
        scanFiles()

        val run = extractLastRun(File(reportUri))
        assertNotNull(run)

        var results = run.results
        assertNotNull(results)
        assertEquals(expectedResults.size, results.size)
        // do not test the physical artifact location as it differs per system
        results = results.map {
            it.copy(
                locations = it.locations?.map
                    {
                            location ->
                        location.copy(physicalLocation = location.physicalLocation?.copy(artifactLocation = null))
                    }
            )
        }
        assertContentEquals(expectedResults, results)
    }

    @Test
    fun testInvocation() {
        scanFiles()

        val run = extractLastRun(File(reportUri))
        assertNotNull(run)

        if (!run.invocations.isNullOrEmpty()) {
            // We do not always expect success because tools like FindSecBugs report no success.
            // This is because of a known bug with lambdas being reported as missing references
            run.invocations!!.forEach { assertEquals(expectedSuccess, it.executionSuccessful) }
        }
    }

    @AfterEach
    fun cleanup() {
        reportUri.runCatching {
            this.toPath().deleteExisting()
        }
    }

    /**
     * Executes the respective executor with the correct Paths
     */
    abstract fun scanFiles()
}
