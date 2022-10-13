@file:Suppress("UNUSED")

package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.dsl

import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.Project
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.modelling.getNodes
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.ordering.Order
import de.fraunhofer.aisec.cpg.query.executionPath

context(Project)
/** For each of the nodes in [this], there is a path to at least one of the nodes in [that]. */
infix fun Op.follows(that: Op): Boolean {
    val thisNodes = getNodes()
    val thatNodes = that.getNodes()
    return thisNodes.all { from -> thatNodes.any { to -> executionPath(from, to).value } }
}

context(Project)
/* Ensures the order of nodes as specified in the user configured [Order] object */
inline fun order(block: Order.() -> Unit): Boolean {
    val order = Order().apply(block)
    val nfa = order.toNfa()
    val dfa = nfa.toDfa()
    return false
    // TODO: evaluate the order!
}
