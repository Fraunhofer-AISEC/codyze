package de.fraunhofer.aisec.codyze.backends.cpg.coko.evaluators

import de.fraunhofer.aisec.codyze.backends.cpg.coko.ordering.evaluateOrder
import de.fraunhofer.aisec.codyze.backends.cpg.coko.ordering.toNfa
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.EvaluationContext
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.EvaluationResult
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.Evaluator
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.ordering.Order
import kotlin.reflect.KFunction

context(EvaluationContext)
class OrderEvaluator(val order: Order): Evaluator {
    override fun evaluate(rule: KFunction<*>): EvaluationResult {
        val dfa = order.toNode().toNfa().toDfa()
        val entities = findInstancesForEntities(rule);

        evaluateOrder(dfa=dfa, rule=rule)
        return EvaluationResult(false)
    }
}

fun findInstancesForEntities(rule: KFunction<*>) {

}
