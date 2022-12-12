package de.fraunhofer.aisec.codyze.backends.testing.coko

import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.Nodes
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.Op
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.Order
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.ordering.OrderToken
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.CokoBackend
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.Evaluator

/**
 * A non-functional [CokoBackend] solely for testing purposes.
 */
class CokoTestingBackend : CokoBackend {
    override val graph: Any by lazy { emptyList<Int>() }

    override fun Op.getAllNodes(): Nodes = emptyList()

    override fun Op.getNodes(): Nodes = emptyList()

    override infix fun Op.follows(that: Op) = FollowsEvaluator(ifOp = this, thenOp = that)

    override fun order(baseNodes: OrderToken?, block: Order.() -> Unit): Evaluator =
        OrderEvaluator(order = Order().apply(block))

    override fun only(vararg ops: Op) = OnlyEvaluator(ops.toList())
}
