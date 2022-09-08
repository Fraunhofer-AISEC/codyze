package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core

import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.evaluate
import de.fraunhofer.aisec.cpg.graph.statements.expressions.CallExpression
import de.fraunhofer.aisec.cpg.graph.statements.expressions.Expression
import de.fraunhofer.aisec.cpg.helpers.SubgraphWalker
import kotlin.reflect.KCallable
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class CPGEvaluator(val cpg: TranslationResult) {
    fun variable(name: String): String = name

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

    fun callFqn(fqn: String, predicate: CallExpression.() -> Boolean): List<CallExpression> {
        val result =
            SubgraphWalker.flattenAST(cpg).filter { node -> (node as? CallExpression)?.fqn == fqn }
                as List<CallExpression>

        return result.filter(predicate)
    }

    infix fun Any.flowsTo(that: Any): Boolean =
        if (this is Wildcard) {
            true
        } else if (that is Collection<*>) {
            this in that.map { (it as Expression).evaluate() }
        } else {
            if (this is String) {
                Regex(this).matches((that as Expression).evaluate() as String)
            } else {
                false
            }
        }
}
