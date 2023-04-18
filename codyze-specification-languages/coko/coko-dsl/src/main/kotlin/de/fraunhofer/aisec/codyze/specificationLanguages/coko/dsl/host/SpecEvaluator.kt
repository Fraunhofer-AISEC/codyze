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
package de.fraunhofer.aisec.codyze.specificationLanguages.coko.dsl.host

import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.CokoRule
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.EvaluationContext
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.Finding
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.Rule
import mu.KotlinLogging
import kotlin.reflect.*
import kotlin.reflect.full.*

private val logger = KotlinLogging.logger {}

/**
 * Evaluates the rules. It first collects all scripts and divides it in the models and
 * implementations. Then, it generates inputs for the rules and calls the rules with the found implementations.
 */
class SpecEvaluator {
    private val rulesAndInstances = mutableListOf<Pair<CokoRule, Any>>()
    val rules
        get() = rulesAndInstances.map { it.first }
    private val typesAndInstances = mutableListOf<Pair<KClass<*>, Any>>()
    val types
        get() = typesAndInstances.map { it.first }
    private val implementationsAndInstances = mutableListOf<Pair<KClass<*>, Any>>()
    val implementations
        get() = implementationsAndInstances.map { it.first }

    fun addSpec(scriptClass: KClass<*>, scriptInstance: Any) {
        for (type in scriptClass.nestedClasses) {
            if (type.isAbstract) {
                typesAndInstances.add(type to scriptInstance)
            } else {
                implementationsAndInstances.add(type to scriptInstance)
            }
        }
        // TODO: does the isInstance filter work?
        for (member in scriptClass.functions.filter { it.findAnnotation<Rule>() != null }
            .filterIsInstance<CokoRule>()) {
            rulesAndInstances.add(member to scriptInstance)
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    fun evaluate(): Map<CokoRule, List<Finding>> {
        val results = mutableMapOf<CokoRule, MutableList<Finding>>()
        for ((index, value) in rulesAndInstances.withIndex()) {
            val (rule, ruleInstance) = value
            logger.info { "Start evaluating rule `${rule.name}`" }
            val parameterMap =
                mutableMapOf(
                    rule.instanceParameter!! to ruleInstance
                ) // rule.instanceParameter should never be null in a script because a script is
            // compiled into a subclass of [CokoScript] which means that anything defined in a
            // script will always need a CokoScript instance as a receiver

            val valueParameterMap =
                rule.valueParameters.associateWith { param ->
                    // TODO: check for all implementations!
                    implementationsAndInstances
                        .filter { (it, _) -> it.createType().isSubtypeOf(param.type) }
                        .map { (it, paramInstance) ->
                            val primaryConstructor =
                                checkNotNull(it.primaryConstructor) {
                                    "Could not create an instance of ${it.qualifiedName} to pass to rule " +
                                        "${rule.name} because it does not have a primary constructor. Aborting."
                                }
                            // TODO: how do we access primaryConstructor.arity ? -> then we would
                            // not need the try..catch
                            try {
                                primaryConstructor.call(paramInstance)
                            } catch (e: IllegalArgumentException) {
                                logger.debug { "Called constructor '$primaryConstructor' without paramInstance ($e)" }
                                primaryConstructor.call()
                            }
                        }[0]
                }

            parameterMap.putAll(valueParameterMap)
            logger.debug { "Obtained parameters for calling rule `${rule.name}`: $parameterMap" }
            logger.debug { "${ruleInstance::class.java.classLoader}" }

            val ruleEvaluator = rule.callBy(parameterMap)
            logger.debug { "Obtained evaluator for rule `${rule.name}`" }
            val ruleResult = ruleEvaluator.evaluate(EvaluationContext(rule = rule, parameterMap = valueParameterMap))
            logger.info {
                " (${index + 1}/${rules.size}): ${rule.name} generated " +
                    if (ruleResult.size == 1) "1 finding" else "${ruleResult.size} findings"
            }

            results.getOrPut(rule) { mutableListOf() } += ruleResult
        }
        return results
    }
}
