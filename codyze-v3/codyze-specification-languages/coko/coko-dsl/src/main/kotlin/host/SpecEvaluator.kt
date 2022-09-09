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
                mutableMapOf<KParameter, Any?>(
                    rule.parameters[0] to ruleInstance
                ) // TODO: ruleInstance might be null!

            parameterMap.putAll(
                rule.parameters
                    .filter { it.kind == KParameter.Kind.VALUE }
                    .associateWith { param ->
                        // TODO: check for all implementations!
                        implementations
                            .filter { (it, _) -> it.createType().isSubtypeOf(param.type) }
                            .map { (it, paramInstance) ->
                                it.primaryConstructor?.call(paramInstance)
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
