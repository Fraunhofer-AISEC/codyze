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

import io.github.detekt.sarif4k.Notification
import io.github.detekt.sarif4k.Run
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import kotlin.test.Test
import kotlin.test.assertContains

class AggregateTest {

    /**
     * Tests the aggregation of a SARIF run containing extension tools
     */
    @Test
    fun aggregateExtensionsRun() {
        val run = runs.first { it.first == "codyze-report.sarif" }.second

        Aggregate.addRun(run)

        val completeRun = Aggregate.createRun()

        assertEquals("CokoExecutor", completeRun!!.tool.driver.name)
        assertEquals(1, completeRun.tool.extensions?.size ?: 0)
        assertEquals(1, completeRun.results?.size ?: 0)
        assertNotNull(completeRun.invocations)
        assertTrue(completeRun.invocations!!.first().executionSuccessful)
        assertEquals(listOf<Notification>(), completeRun.invocations!!.first().toolExecutionNotifications)
    }

    /**
     * Tests the aggregation of a SARIF run containing an optional plugin
     */
    @Test
    fun aggregateMultipleRuns() {
        val run3 = runs.first { it.first == "pmd-report.sarif" }.second
        val run1 = runs.first { it.first == "codyze-report.sarif" }.second

        Aggregate.addRun(run1)
        Aggregate.addRun(run3)

        val completeRun = Aggregate.createRun()

        assertEquals("CokoExecutor", completeRun!!.tool.driver.name)
        assertEquals(2, completeRun.tool.extensions?.size ?: 0)
        assertContains(completeRun.tool.extensions!!.map { it.name }, "CPG Coko Backend")
        assertContains(completeRun.tool.extensions!!.map { it.name }, "PMD")

        assertEquals(4, completeRun.results?.size ?: 0)
    }

    @AfterEach
    fun resetAggregate() {
        Aggregate.reset()
    }

    companion object {
        private lateinit var runs: Set<Pair<String, Run>>

        @BeforeAll
        @JvmStatic
        fun createRuns() {
            ParserTest.loadResources()
            runs = ParserTest.exampleResults.mapNotNull {
                val run = extractLastRun(it)
                if (run == null) null else it.name to run
            }.toSet()
        }
    }
}
