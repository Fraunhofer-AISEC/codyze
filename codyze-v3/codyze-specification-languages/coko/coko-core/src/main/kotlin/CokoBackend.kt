package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core

import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.dsl.Op
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.ordering.Order
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.modelling.Definition
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.modelling.Signature
import de.fraunhofer.aisec.codyze_core.wrapper.Backend

typealias Nodes = Collection<Any>

@Suppress("UNUSED")
@CokoMarker
/*
 * Receives a [cpg] translation result to identify matching nodes and evaluate the expressions.
 * All the functionality of the DSL are implemented as extension functions on [CokoBackend].
 */
interface CokoBackend : Backend {
        /** Get all [Nodes] that are associated with this [Op]. */
        fun Op.getAllNodes(): Nodes

        /**
         * Get all [Nodes] that are associated with this [Op] and fulfill the [Signature]s of the
         * [Definition]s.
         */
        fun Op.getNodes(): Nodes

        fun evaluateOrder(order: Order): Evaluator

        fun evaluateFollows(ifOp: Op, thenOp: Op): Evaluator
}
