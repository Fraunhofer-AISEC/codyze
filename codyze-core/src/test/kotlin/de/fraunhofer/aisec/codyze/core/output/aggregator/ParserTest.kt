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
package de.fraunhofer.aisec.codyze.core.output.aggregator

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.io.File
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isRegularFile

class ParserTest {

    /**
     * Tests the parsing of a valid SARIF result file
     */
    @Test
    fun parseValid() {
        val file = exampleResults.first { it.name == "pmd-report.sarif" }

        val run = extractLastRun(file)

        assertNotNull(run)
        run!!

        assertEquals("PMD", run.tool.driver.name)
        assertEquals(3, run.tool.driver.rules!!.size)

        assertEquals(3, run.results!!.size)
        assertEquals("CloseResource", run.results!![0].ruleID)
        assertEquals("CloseResource", run.results!![1].ruleID)
        assertEquals("ControlStatementBraces", run.results!![2].ruleID)
    }

    /**
     * Tests the parsing of a file that does not exist
     */
    @Test
    fun parseNotExisting() {
        val file = File("fake-results.sarif")

        val run = extractLastRun(file)
        assertNull(run)
    }

    /**
     * Tests the parsing of a file that is not in valid SARIF format
     */
    @Test
    fun parseInvalidType() {
        val file = exampleResults.first { it.name == "pmd-report.txt" }

        val run = extractLastRun(file)
        assertNull(run)
    }

    companion object {
        lateinit var exampleResults: Set<File>

        @BeforeAll
        @JvmStatic
        fun loadResources() {
            val resultsDirectory: URL? = ParserTest::class.java.classLoader.getResource("externalReports")

            assertNotNull(resultsDirectory)

            exampleResults = Files.walk(Path.of(resultsDirectory!!.toURI()))
                .filter { it.isRegularFile() }
                .map { it.toFile() }
                .toList().toSet()
        }
    }
}
