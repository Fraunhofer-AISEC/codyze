package de.fraunhofer.aisec.codyze.backends.cpg.coko

import de.fraunhofer.aisec.codyze.backends.cpg.CPGConfiguration
import de.fraunhofer.aisec.codyze.backends.cpg.CPGManager
import de.fraunhofer.aisec.codyze.backends.cpg.coko.evaluators.FollowsEvaluator
import de.fraunhofer.aisec.codyze.backends.cpg.coko.evaluators.OrderEvaluator
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.CokoBackendManager
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.EvaluationContext
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.dsl.Op
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.Evaluator
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.Nodes
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.modelling.Definition
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.modelling.Signature
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.ordering.Order

class CokoCpgManager(config: CPGConfiguration): CPGManager(config = config), CokoBackendManager {

    context(EvaluationContext)
    /** Get all [Nodes] that are associated with this [Op]. */
    override fun Op.getAllNodes(): Nodes =
        this@Op.definitions.map { def -> this@EvaluationContext.callFqn(def.fqn) }.flatten()

    context(EvaluationContext)
    /**
     * Get all [Nodes] that are associated with this [Op] and fulfill the [Signature]s of the
     * [Definition]s.
     */
    override fun Op.getNodes(): Nodes =
        this@Op.definitions
            .map { def ->
                this@EvaluationContext.callFqn(def.fqn) {
                    def.signatures.any { sig ->
                        signature(*sig.parameters.toTypedArray()) &&
                                sig.unorderedParameters.all { it?.flowsTo(arguments) ?: false }
                    }
                }
            }
            .flatten()

    context(EvaluationContext)
    override fun evaluateOrder(order: Order): Evaluator = OrderEvaluator(order)

    context(EvaluationContext)
    override fun evaluateFollows(ifOp: Op, thenOp: Op): Evaluator = FollowsEvaluator(ifOp=ifOp, thenOp=thenOp)
}