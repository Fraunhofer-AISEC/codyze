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
package de.fraunhofer.aisec.codyze.plugin.aggregator

import de.fraunhofer.aisec.codyze.core.output.aggregator.Aggregate
import io.github.detekt.sarif4k.Run
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import kotlin.test.Test
import kotlin.test.assertContains

class AggregateTest {

    /**
     * Tests the aggregation of a simple SARIF run
     */
    @Test
    fun aggregateSimpleRun() {
        val aggregate = Aggregate()
        val run = runs.first { it.first == "pmd-report.sarif" }.second

        aggregate.addRun(run)

        val completeRun = aggregate.createRun()

        assertEquals("PMD", completeRun!!.tool.driver.name)
        assertEquals(0, completeRun.tool.extensions?.size ?: 0)
        assertEquals(3, completeRun.results?.size ?: 0)
    }

    /**
     * Tests the aggregation of a SARIF run containing extension tools
     */
    @Test
    fun aggregateExtensionsRun() {
        val aggregate = Aggregate()
        val run = runs.first { it.first == "codyze-report.sarif" }.second

        aggregate.addRun(run)

        val completeRun = aggregate.createRun()

        assertEquals("CodyzeMedina", completeRun!!.tool.driver.name)
        assertEquals(1, completeRun.tool.extensions?.size ?: 0)
        assertEquals("codyze", completeRun.tool.extensions!![0].name)

        assertEquals(6, completeRun.results?.size ?: 0)
    }

    /**
     * Tests the aggregation of a SARIF run containing optional objects
     */
    @Test
    fun aggregateMultipleRuns() {
        val aggregate = Aggregate()
        val run1 = runs.first { it.first == "codyze-report.sarif" }.second
        val run2 = runs.first { it.first == "pmd-report.sarif" }.second

        aggregate.addRun(run1)
        aggregate.addRun(run2)

        val completeRun = aggregate.createRun()

        assertEquals("CodyzeMedina", completeRun!!.tool.driver.name)
        assertEquals(2, completeRun.tool.extensions?.size ?: 0)
        assertContains(completeRun.tool.extensions!!.map { it.name }, "codyze")
        assertContains(completeRun.tool.extensions!!.map { it.name }, "PMD")

        assertEquals(9, completeRun.results?.size ?: 0)
    }

    companion object {
        private lateinit var runs: Set<Pair<String, Run>>

        @BeforeAll
        @JvmStatic
        fun createRuns() {
            val parser = Parser()
            ParserTest.loadResources()
            runs = ParserTest.exampleResults.mapNotNull {
                val run = parser.extractLastRun(it)
                if (run == null) null else it.name to run
            }.toSet()
        }
    }
}