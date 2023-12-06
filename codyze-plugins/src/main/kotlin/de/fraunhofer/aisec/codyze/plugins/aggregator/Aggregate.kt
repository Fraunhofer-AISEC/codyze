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

import io.github.detekt.sarif4k.*
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

        // TODO: for uniqueness, we should use the rule property in the result
        //  this is a reportingDescriptorReference which contains information about the toolComponent
        //  https://docs.oasis-open.org/sarif/sarif/v2.1.0/errata01/os/sarif-v2.1.0-errata01-os-complete.html#_Toc141790895
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

        // TODO: we cannot directly use the "invocation" properties of the components as they cannot be assigned to toolComponents
        //  but we could build our own invocation from the invocations we got (e.g. successful only if all invocations were successful)

        // TODO: search the SARIF specs for "result management system", e.g. fingerprints

        // TODO: check each possible property in a run for whether/how they can be used in the aggregate
        // TODO: when we modify information (e.g. invocation) we need to check for references (invocationIndex in resultProvenance)!

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

    /**
     * This modifies the results object in a way that allows unique reference to the corresponding rule object within the aggregate.
     * For this we need to populate the rule property in each result with the correct toolComponentReference or create the property from scratch.
     * The properties result.ruleID and result.ruleIndex will be moved into the rule object if they exist.
     * @param run The run which should have its results modified
     * @return The run after applying the modifications
     */
    private fun modifyResults(run: Run): Run {
        var newResults = run.results?.map {
            result ->
            var newResult = result.copy(ruleIndex = null, ruleID = null)
            if (result.rule != null) {
                // rule property exists: keep id and index unchanged. ToolComponent must be set, otherwise it defaults to driver
                val component = result.rule!!.toolComponent
                // here we move result.ruleID and result.ruleIndex into the rule object if necessary
                var newRule = result.rule!!.copy(
                    id = result.rule!!.id ?: result.ruleID,
                    index = result.rule!!.index ?: result.ruleIndex
                )
                if (component != null) {
                    // reference to component exists: fix index if necessary
                    if (component.index != null) {
                        val newComponent = component.copy(index = component.index!! + 1 + extensions.size)
                        newRule = newRule.copy(toolComponent = newComponent)
                    }
                    newResult = newResult.copy(rule = newRule)
                } else {
                    // no reference to component: create new reference to the old driver (may now be an extension)
                    val newComponent = ToolComponentReference(
                        guid = run.tool.driver.guid,
                        index = if (containedRuns.isEmpty()) null else 1 + extensions.size.toLong(),
                        name = run.tool.driver.name
                    )
                    newRule = newRule.copy(toolComponent = newComponent)
                    newResult = newResult.copy(rule = newRule)
                }
            } else {
                // rule property does not exist: create property that references the driver (no toolComponent-index)
                val driverRules = run.tool.driver.rules
                val rule = if (result.ruleIndex != null) driverRules?.get(result.ruleIndex!!.toInt()) else driverRules?.firstOrNull { it.id == result.ruleID }

                // if no rule information is available at all, we can keep the result object unchanged
                if (rule != null) {
                    val componentReference = ToolComponentReference(
                        guid = run.tool.driver.guid,
                        index = null,
                        name = run.tool.driver.name,
                    )
                    val ruleReference = ReportingDescriptorReference(
                        guid = rule.guid,
                        id = result.ruleID,
                        index = result.ruleIndex,
                        toolComponent = componentReference
                    )
                    newResult = newResult.copy(rule = ruleReference)
                }
            }
            newResult
        }

        return run.copy(results = newResults)
    }
}