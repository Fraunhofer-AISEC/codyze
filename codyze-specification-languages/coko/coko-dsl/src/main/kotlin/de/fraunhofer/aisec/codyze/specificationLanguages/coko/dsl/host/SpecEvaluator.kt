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

import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.EvaluationContext
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.Evaluator
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.Rule
import mu.KotlinLogging
import kotlin.reflect.*
import kotlin.reflect.full.*

private val logger = KotlinLogging.logger {}

/**
 * Evaluates the rules. It first collects all scripts and divides it in the models and
 * implementations. Then, it generates inputs for the rules and calls the rules.
 */
class SpecEvaluator {
    val rules = mutableListOf<Pair<KFunction<*>, Any>>()
    val types = mutableListOf<Pair<KClass<*>, Any>>()
    val implementations = mutableListOf<Pair<KClass<*>, Any>>()

    fun addSpec(scriptClass: KClass<*>, scriptInstance: Any) {
        for (type in scriptClass.nestedClasses) {
            if (type.isAbstract) {
                types.add(type to scriptInstance)
            } else {
                implementations.add(type to scriptInstance)
            }
        }
        for (member in scriptClass.functions.filter { it.findAnnotation<Rule>() != null }) {
            rules.add(member to scriptInstance)
        }
    }

    fun evaluate() {
        for ((index, value) in rules.withIndex()) {
            val (rule, ruleInstance) = value
            val parameterMap =
                mutableMapOf(
                    rule.instanceParameter!! to ruleInstance
                ) // rule.instanceParameter should never be null in a script because a script is
            // compiled into a subclass of [CokoScript] which means that anything defined in a
            // script will always need a CokoScript instance as a receiver

            val valueParameterMap =
                rule.valueParameters.associateWith { param ->
                    // TODO: check for all implementations!
                    implementations
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
                                primaryConstructor.call()
                            }
                        }[0]
                }

            parameterMap.putAll(valueParameterMap)

            val rawRuleResult = rule.callBy(parameterMap)
            val ruleResult =
                (rawRuleResult as? Evaluator)?.evaluate(
                    EvaluationContext(rule = rule, parameterMap = valueParameterMap)
                )
                    ?: rawRuleResult
            logger.info {
                " (${index + 1}/${rules.size}): ${rule.name} -> ${if (ruleResult == true) "🎉" else "💩"}"
            }
        }
    }
}
