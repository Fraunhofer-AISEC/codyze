package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_dsl.host

import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.Rule
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.Wildcard
import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.declarations.FieldDeclaration
import de.fraunhofer.aisec.cpg.graph.evaluate
import de.fraunhofer.aisec.cpg.graph.statements.expressions.CallExpression
import de.fraunhofer.aisec.cpg.graph.statements.expressions.Expression
import de.fraunhofer.aisec.cpg.helpers.SubgraphWalker
import kotlin.reflect.*
import kotlin.reflect.full.*
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class SpecCollector {
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

            val ruleResult = rule.callBy(parameterMap)
            logger.info { " : ${rule.name} -> ${if (ruleResult == true) "🎉" else "💩"}" }
        }
    }
}
