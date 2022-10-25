@file:Suppress("UNUSED")

package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.dsl

import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.CokoBackend
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.ordering.Order
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.ordering.OrderToken

context(CokoBackend)
/** For each of the nodes in [this], there is a path to at least one of the nodes in [that]. */
infix fun Op.follows(that: Op) = this@CokoBackend.evaluateFollows(ifOp = this, thenOp = that)

context(CokoBackend)
/* Ensures the order of nodes as specified in the user configured [Order] object */
inline fun order(baseNodes: OrderToken, block: Order.() -> Unit) = this@CokoBackend.evaluateOrder(order = Order().apply(block))
