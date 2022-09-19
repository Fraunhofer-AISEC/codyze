@file:Suppress("UNUSED")

package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.dsl

import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.Project
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.dsl.ordering.Order
import de.fraunhofer.aisec.cpg.graph.*
import de.fraunhofer.aisec.cpg.query.executionPath

/** For each of the nodes in [this], there is a path to at least one of the nodes in [that]. */
infix fun Collection<Node>.follows(that: Collection<Node>): Boolean {
    return this.all { from -> that.any { to -> executionPath(from, to).value } }
}

/* Ensures the order of nodes as specified in the user configured [Order] object */
inline fun Project.order(block: Order.() -> Unit): Boolean {
    val order = Order().apply(block)
    return false
    // TODO: evaluate the order!
}
