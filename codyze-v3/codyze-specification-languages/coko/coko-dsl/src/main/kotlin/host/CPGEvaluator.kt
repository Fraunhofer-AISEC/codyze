package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_dsl.host

import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.Rule
import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.evaluate
import de.fraunhofer.aisec.cpg.graph.statements.expressions.CallExpression
import de.fraunhofer.aisec.cpg.graph.statements.expressions.Expression
import de.fraunhofer.aisec.cpg.helpers.SubgraphWalker
import kotlin.reflect.*
import kotlin.reflect.full.*
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class CPGEvaluator(val cpg: TranslationResult) {
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

    fun call(name: String, vararg args: Any): List<CallExpression> {
        return SubgraphWalker.flattenAST(cpg).filter { node ->
            (node as? CallExpression)?.invokes?.any { it.name == name } == true
        } as List<CallExpression>
        // TODO: Once we have a version of CPGv5, use the following line instead:
        // return cpg.callsByName(full_name)
    }

    fun matchValues(expected: Any, actual: Expression): Boolean {
        if(expected.javaClass == Any().javaClass || "*" == expected) {
            // Any() and * are placeholders. We don't care about the value, so everything matches.
            return true
        }
        val actualEvaluated = actual.evaluate()
        if (expected is String) {
            return Regex(expected).matches(actualEvaluated as String)
        }
        // TODO: Add more options here.
        return false
    }

    fun callFqn(fqn: String, vararg args: Any): List<CallExpression> {
        var result =
            SubgraphWalker.flattenAST(cpg).filter { node -> (node as? CallExpression)?.fqn == fqn }
                as List<CallExpression>
        // Check the respective args
        args.onEachIndexed { i, arg ->
            result = result.filter { matchValues(arg, it.arguments[i]) }
        }

        return result
    }

    fun callFqnUnordered(fqn: String, vararg args: Any): List<CallExpression> {
        var result =
            SubgraphWalker.flattenAST(cpg).filter { node -> (node as? CallExpression)?.fqn == fqn }
                    as List<CallExpression>
        // Check the respective args
        result =
            result.filter { ic ->
                args.all { arg -> ic.arguments.any { matchValues(arg, it) } }
            }

        return result
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
