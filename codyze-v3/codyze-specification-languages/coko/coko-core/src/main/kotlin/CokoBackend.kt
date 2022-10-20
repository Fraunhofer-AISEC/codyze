package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core

import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.dsl.Op
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.ordering.Order
import de.fraunhofer.aisec.codyze_core.wrapper.BackendManager

typealias Nodes = Collection<Any>

interface CokoBackendManager : BackendManager {
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
