package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_dsl.host

import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.dsl.Rule
import kotlin.reflect.*
import kotlin.reflect.full.*
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}
/**
 * Evaluates the rules. It first collects all scripts and divides it in the models and
 * implementations. Then, it generates inputs for the rules and calls the rules.
 */
class SpecEvaluator {
    val rules = mutableListOf<Pair<KCallable<*>, Any>>() // TODO: change to KFunction?
    val types = mutableListOf<Pair<KClass<*>, Any>>()
    val implementations = mutableListOf<Pair<KClass<*>, Any>>()

    fun addSpec(scriptClass: KClass<*>, scriptInstance: Any) {
        for (type in scriptClass.nestedClasses) {
            if (type.isAbstract) types.add(type to scriptInstance)
            else implementations.add(type to scriptInstance)
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

            parameterMap.putAll(
                rule.valueParameters.associateWith { param ->
                    // TODO: check for all implementations!
                    implementations
                        .filter { (it, _) -> it.createType().isSubtypeOf(param.type) }
                        .map { (it, paramInstance) ->
                            checkNotNull(it.primaryConstructor) {
                                    "Could not create an instance of ${it.qualifiedName} to pass to rule ${rule.name} because it does not have a primary constructor. Aborting."
                                }
                                .call(paramInstance)
                        }[0]
                }
            )

            val ruleResult = rule.callBy(parameterMap)
            logger.info {
                " (${index+1}/${rules.size}): ${rule.name} -> ${if (ruleResult == true) "🎉" else "💩"}"
            }
        }
    }
}