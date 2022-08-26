package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_dsl.host

import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.Rule
import de.fraunhofer.aisec.cpg.TranslationManager
import kotlin.reflect.*
import kotlin.reflect.full.*

class CPGEvaluator(val cpg: TranslationManager) {
    val rules = mutableListOf<Pair<KCallable<*>, Any>>()
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

    fun any(): String = ""

    fun variable(name: String): String = ""

    fun <T> call(func: KCallable<T>, vararg arguments: Any) {
        println("get all nodes for call to ${func.name} with arguments: [$arguments]")
    }

    fun call(full_name: String) {
        println("get all nodes for call to $full_name")
    }

    fun evaluate() {
        for ((rule, ruleInstance) in rules) {
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
            rule.callBy(parameterMap)
            println("Rule could be evaluated 🎉")
        }
    }
}
