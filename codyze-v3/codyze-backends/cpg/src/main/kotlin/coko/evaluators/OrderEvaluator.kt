package de.fraunhofer.aisec.codyze_backends.cpg.coko.evaluators

import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.EvaluationResult
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.Evaluator
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.dsl.Order
import de.fraunhofer.aisec.codyze_backends.cpg.coko.CokoCpgBackend
import de.fraunhofer.aisec.codyze_backends.cpg.coko.ordering.toNfa
import kotlin.reflect.KFunction

context(CokoCpgBackend)

class OrderEvaluator(val order: Order) : Evaluator {
    private fun findInstancesForEntities(rule: KFunction<*>) {}

    override fun evaluate(rule: KFunction<*>): EvaluationResult {
        val dfa = order.toNode().toNfa().toDfa()
        val entities = findInstancesForEntities(rule)

        dfa.states
        //    if (markInstances.size > 1) {
        //        log.warn("Order statement contains more than one base. Not supported.")
        //        return ErrorValue.newErrorValue(
        //            "Order statement contains more than one base. Not supported."
        //        )
        //    }
        //    if (markInstances.isEmpty()) {
        //        log.warn("Order statement does not contain any ops. Invalid order.")
        //        return ErrorValue.newErrorValue(
        //            "Order statement does not contain any ops. Invalid order."
        //        )
        //    }
        return EvaluationResult(false)
    }
}
