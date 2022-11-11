package de.fraunhofer.aisec.codyze_backends.cpg.coko

import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.CokoBackend
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.Evaluator
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.Nodes
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.dsl.ConstructorOp
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.dsl.FunctionOp
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.dsl.Op
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.modelling.Definition
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.modelling.Signature
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.ordering.Order
import de.fraunhofer.aisec.codyze_backends.cpg.CPGBackend
import de.fraunhofer.aisec.codyze_backends.cpg.CPGConfiguration
import de.fraunhofer.aisec.codyze_backends.cpg.coko.dsl.*
import de.fraunhofer.aisec.codyze_backends.cpg.coko.evaluators.FollowsEvaluator
import de.fraunhofer.aisec.codyze_backends.cpg.coko.evaluators.OnlyEvaluator
import de.fraunhofer.aisec.codyze_backends.cpg.coko.evaluators.OrderEvaluator
import de.fraunhofer.aisec.codyze_core.wrapper.BackendConfiguration

class CokoCpgBackend(config: BackendConfiguration) :
    CPGBackend(config = config as CPGConfiguration), CokoBackend {

    /** Get all [Nodes] that are associated with this [Op]. */
    override fun Op.getAllNodes(): Nodes =
        when (this@Op) {
            is FunctionOp ->
                this@Op.definitions.map { def -> this@CokoCpgBackend.callFqn(def.fqn) }.flatten()
            is ConstructorOp -> this@CokoCpgBackend.constructor(this.classFqn)
        }

    /**
     * Get all [Nodes] that are associated with this [Op] and fulfill the [Signature]s of the
     * [Definition]s.
     */
    override fun Op.getNodes(): Nodes =
        when (this@Op) {
            is FunctionOp ->
                this@Op.definitions
                    .map { def ->
                        this@CokoCpgBackend.callFqn(def.fqn) {
                            def.signatures.any { sig ->
                                signature(*sig.parameters.toTypedArray()) &&
                                    sig.unorderedParameters.all { it?.flowsTo(arguments) ?: false }
                            }
                        }
                    }
                    .flatten()
            is ConstructorOp ->
                this@Op.signatures
                    .map { sig ->
                        this@CokoCpgBackend.constructor(this@Op.classFqn) {
                            signature(*sig.parameters.toTypedArray()) &&
                                sig.unorderedParameters.all { it?.flowsTo(arguments) ?: false }
                        }
                    }
                    .flatten()
        }

    override fun evaluateOrder(order: Order): Evaluator = OrderEvaluator(order)

    override fun evaluateFollows(ifOp: Op, thenOp: Op): Evaluator =
        FollowsEvaluator(ifOp = ifOp, thenOp = thenOp)

    override fun evaluateOnly(ops: List<Op>): Evaluator = OnlyEvaluator(ops)
}
