package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_dsl.host

import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.Rule
import de.fraunhofer.aisec.cpg.TranslationManager
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation

class cpgEvaluator(val cpg: TranslationManager) {
    val rules = mutableListOf<KCallable<*>>()
    val types = mutableListOf<KClass<*>>()
    val implementations = mutableListOf<Any>()

    fun addSpec(scriptClass: KClass<*>) {
        for (type in scriptClass.nestedClasses) {
            if (type.isAbstract) types.add(type) else implementations.add(type)
        }
        for (member in scriptClass.members.filter { it.findAnnotation<Rule>() != null }) {
            rules.add(member)
        }
    }

    fun any(): String = ""

    fun variable(name: String): String = ""

    fun <T> call(func: KCallable<T>, vararg arguments: Any) = Unit

    fun call(full_name: String) = Unit

    fun evaluate() {
        for (rule in rules) {
            val parameterMap =
                rule.parameters.filter { it.kind == KParameter.Kind.VALUE }.associateWith { it.name }
            // rule.callBy()
        }
    }
}
