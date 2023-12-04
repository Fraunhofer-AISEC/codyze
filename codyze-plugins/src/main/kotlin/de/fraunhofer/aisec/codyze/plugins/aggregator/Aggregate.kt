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
package de.fraunhofer.aisec.codyze.plugins.aggregator

import io.github.detekt.sarif4k.Result
import io.github.detekt.sarif4k.Run
import io.github.detekt.sarif4k.Tool
import io.github.detekt.sarif4k.ToolComponent
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger { }

/**
 * A class containing information about the aggregated SARIF run.
 * Each external Tool will be listed as an extension while Codyze functions as the driver.
 */
class Aggregate {
    private lateinit var driver: ToolComponent
    private var extensions: List<ToolComponent> = listOf()
    private var results: List<Result> = listOf()

    private var containedRuns: Set<Run> = setOf()

    /**
     * Creates a new run from the information stored within the aggregate.
     */
    fun createRun(): Run? {
        // Prevent Exception from uninitialized driver
        if (containedRuns.isEmpty()) {
            logger.error { "Failed to create run from aggregate: No runs added yet" }
            return null
        }

        logger.info { "Creating single run from aggregate consisting of ${containedRuns.size} runs" }
        return Run(
            tool = Tool(driver, extensions),
            results = results
        )
    }

    /**
     * Adds a new run to the aggregate.The driver of the first run will be locked in as the driver for the aggregate.
     * @param run The new SARIF run to add
     * @return The aggregate after adding the run
     */
    fun addRun(run: Run): Aggregate {
        var modifiedRun: Run = run

        // We remove the ruleIndex and only keep ruleID or rule.id (one of both SHALL be present)
        // Rule IDs need not necessarily be unique, therefore a rare conflict occurring is not severe
        val newResults = run.results?.map {
            if (it.ruleID != null) {
                it.copy(ruleIndex = null, rule = null)
            } else {
                val rule = it.rule?.copy(index = null)
                it.copy(ruleIndex = null, rule = rule)
            }
        }

        modifiedRun = modifiedRun.copy(results = newResults)

        if (containedRuns.isEmpty()) {
            driver = modifiedRun.tool.driver
        } else {
            extensions += modifiedRun.tool.driver
        }
        extensions += modifiedRun.tool.extensions ?: listOf()
        results += modifiedRun.results ?: listOf()
        containedRuns += modifiedRun

        logger.info { "Added run from ${driver.name} to the aggregate" }
        return this
    }
}