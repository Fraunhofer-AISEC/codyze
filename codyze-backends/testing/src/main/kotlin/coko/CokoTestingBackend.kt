package de.fraunhofer.aisec.codyzeBackends.testing.coko

import de.fraunhofer.aisec.codyze.specificationLanguages.coko.cokoCore.CokoBackend
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.cokoCore.Nodes
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.cokoCore.dsl.Op
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.cokoCore.dsl.Order
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.cokoCore.modelling.Definition
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.cokoCore.modelling.Signature
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.cokoCore.ordering.OrderToken
import de.fraunhofer.aisec.codyzeBackends.cpg.coko.evaluators.FollowsEvaluator
import de.fraunhofer.aisec.codyzeBackends.cpg.coko.evaluators.OnlyEvaluator
import de.fraunhofer.aisec.codyzeBackends.cpg.coko.evaluators.OrderEvaluator
import de.fraunhofer.aisec.codyzeBackends.testing.TestingConfiguration
import de.fraunhofer.aisec.codyzeCore.wrapper.BackendConfiguration

class CokoTestingBackend(config: BackendConfiguration) : CokoBackend {
    override val graph: Any by lazy { emptyList<Int>() }
    private val config = config as TestingConfiguration

    /** Get all [Nodes] that are associated with this [Op]. */
    override fun Op.getAllNodes(): Nodes = emptyList()

    /**
     * Get all [Nodes] that are associated with this [Op] and fulfill the [Signature]s of the
     * [Definition]s.
     */
    override fun Op.getNodes(): Nodes = emptyList()

    /** For each of the nodes in [this], there is a path to at least one of the nodes in [that]. */
    override infix fun Op.follows(that: Op) = FollowsEvaluator(ifOp = this, thenOp = that)

    /* Ensures the order of nodes as specified in the user configured [Order] object */
    override fun order(baseNodes: OrderToken, block: Order.() -> Unit) =
        OrderEvaluator(order = Order().apply(block))

    /** Ensures that all calls to the [ops] have arguments that fit the parameters specified in [ops] */
    override fun only(vararg ops: Op) = OnlyEvaluator(ops.toList())
}
