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
package de.fraunhofer.aisec.codyze.plugin.plugins

import de.fraunhofer.aisec.codyze.core.output.aggregator.extractLastRun
import de.fraunhofer.aisec.codyze.plugins.Plugin
import io.github.detekt.sarif4k.Result
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

abstract class PluginTest {
    abstract val plugin: Plugin
    abstract val resultFileName: String
    abstract val expectedResults: List<Result>

    @Test
    fun testExample() {
        scanFiles()

        val run = extractLastRun(File("src/test/resources/generatedReports/$resultFileName"))
        assertNotNull(run)

        if (!run.invocations.isNullOrEmpty()) {
            run.invocations!!.forEach { assertTrue { it.executionSuccessful } }
        }

        val results = run.results
        assertNotNull(results)
        assertEquals(expectedResults.size, results.size)
        assertContentEquals(expectedResults, results)
    }

    @AfterEach
    fun cleanup() {
        File("src/test/resources/generatedReports/$resultFileName").delete()
    }

    /**
     * Executes the respective executor with the correct Paths
     */
    abstract fun scanFiles()
}