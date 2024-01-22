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

import io.github.detekt.sarif4k.*
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger { }

/**
 * A static class containing information about an aggregated SARIF run consisting of multiple separate runs.
 * Each external Tool will be listed as an extension while Codyze functions as the driver.
 * However, each SARIF report will be reduced to fields that are present and handled in this class.
 */
object Aggregate {
    // The driver is null iff containedRuns is empty
    private var driver: ToolComponent? = null
    private var extensions: List<ToolComponent> = listOf()
    private var results: List<Result> = listOf()
    private var invocations: List<Invocation> = listOf()
    private var containedRuns: Set<Run> = setOf()

    /**
     * Creates a new run from the information stored within the aggregate.
     */
    fun createRun(): Run? {
        val currentDriver = driver

        // Prevent Exception from uninitialized driver
        if (currentDriver == null) {
            logger.error { "Failed to create run from aggregate: No driver added yet" }
            return null
        }

        logger.info { "Creating single run from aggregate consisting of ${containedRuns.size} runs" }
        return Run(
            tool = Tool(currentDriver, extensions),
            results = results,
            invocations = listOf(createInvocation())
        )
    }

    /**
     * Adds a new run to the aggregate.
     * The driver of the first run will be locked in as the driver for the aggregate.
     * @param run The new SARIF run to add
     * @return The aggregate after adding the run
     */
    fun addRun(run: Run) {
        val modifiedRun: Run = modifyResults(run)

        val originalDriver = modifiedRun.tool.driver

        // Here we hardcode Codyze as the only possible driver for the aggregate
        // otherwise the driver of the aggregate would depend on the order of subcommands
        if (driver == null && originalDriver.product == "Codyze") {
            driver = originalDriver
        } else {
            extensions += originalDriver
        }
        extensions += modifiedRun.tool.extensions.orEmpty()
        results += modifiedRun.results.orEmpty()
        invocations += modifiedRun.invocations.orEmpty()
        containedRuns += modifiedRun

        logger.info { "Added run from ${originalDriver.name} to the aggregate" }
    }

    /**
     * Resets the information stored within the aggregate
     */
    fun reset() {
        driver = null
        extensions = listOf()
        results = listOf()
        containedRuns = setOf()
    }

    /**
     * Modifies the results object in a way that allows unique reference to the corresponding rule object
     * within the aggregate.
     * For this we need to populate the rule property in each result with the correct toolComponentReference
     * or create the property from scratch.
     * The properties result.ruleID and result.ruleIndex will be moved into the rule object if they exist.
     * @param run The run which should have its results modified
     * @return The run after applying the modifications
     */
    private fun modifyResults(run: Run): Run {
        val newResults = run.results?.map { result ->
            var newResult = result.copy(ruleIndex = null, ruleID = null)
            val oldRule = result.rule
            if (oldRule != null) {
                // rule property exists: keep id and index unchanged.
                // ToolComponent must be set, otherwise it defaults to driver
                val component = oldRule.toolComponent
                // here we move result.ruleID and result.ruleIndex into the rule object if necessary
                var newRule = oldRule.copy(
                    id = oldRule.id ?: result.ruleID,
                    index = oldRule.index ?: result.ruleIndex
                )
                if (component != null) {
                    // reference to component exists: fix index if necessary
                    val oldIndex = component.index
                    if (oldIndex != null) {
                        val newComponent = component.copy(index = oldIndex + 1 + extensions.size)
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
                val oldIndex = result.ruleIndex
                val rule = if (oldIndex != null) {
                    driverRules?.get(oldIndex.toInt())
                } else {
                    driverRules?.firstOrNull { it.id == result.ruleID }
                }

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

    /**
     * Creates a new Invocation object from all invocations contained in the aggregate.
     * The resulting invocation only indicates a successful execution if all contained invocations do so.
     * On failed execution, the resulting invocation indicates which tool failed.
     * @return An invocation created from the information in the aggregate
     */
    private fun createInvocation(): Invocation {
        var executionSuccessful = true
        val notifications: MutableList<Notification> = mutableListOf()

        for (run in containedRuns) {
            // First build the toolName with extensions
            val toolName = getToolName(run)
            // Then create an error message for each failed tool invocation
            val unsuccessfulInvocations = run.invocations?.filter { !it.executionSuccessful }.orEmpty()
            for (inv in unsuccessfulInvocations) {
                executionSuccessful = false
                val reason = if (inv.exitCodeDescription != null) " (${inv.exitCodeDescription})" else ""
                val message = "Tool $toolName failed execution$reason"
                notifications += Notification(level = Level.Error, message = Message(text = message))
            }
        }

        // We do not define exitCodeDescription or executionSuccessful based on Codyze
        // as it doesn't produce an invocation object
        return Invocation(executionSuccessful = executionSuccessful, toolExecutionNotifications = notifications)
    }

    /**
     * Gets the name of driver + extensions of a run
     * @param run
     * @return The name in the format "driver (+ ex1, ex2, ...)"
     */
    private fun getToolName(run: Run): String {
        var toolName = run.tool.driver.name
        val extensions = run.tool.extensions
        if (extensions != null) {
            toolName += " (+ "
            for (extension in extensions) {
                toolName += extension.name
                if (extension != extensions.last()) {
                    toolName += ", "
                }
            }
            toolName += ")"
        }
        return toolName
    }
}
