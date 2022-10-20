package de.fraunhofer.aisec.codyze.backends.cpg.coko.ordering

import de.fraunhofer.aisec.cpg.analysis.fsm.DFA
import mu.KotlinLogging
import kotlin.reflect.KFunction

private val logger = KotlinLogging.logger {}

fun evaluateOrder(dfa: DFA, rule: KFunction<*>) {
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
}
