/*
 * Copyright (c) 2022, Fraunhofer AISEC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fraunhofer.aisec.codyze.backends.cpg.coko

import de.fraunhofer.aisec.codyze.backends.cpg.CPGBackend
import de.fraunhofer.aisec.codyze.backends.cpg.CPGConfiguration
import de.fraunhofer.aisec.codyze.backends.cpg.coko.dsl.*
import de.fraunhofer.aisec.codyze.backends.cpg.coko.evaluators.FollowsEvaluator
import de.fraunhofer.aisec.codyze.backends.cpg.coko.evaluators.OnlyEvaluator
import de.fraunhofer.aisec.codyze.backends.cpg.coko.evaluators.OrderEvaluator
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.CokoBackend
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.Nodes
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.dsl.ConstructorOp
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.dsl.FunctionOp
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.dsl.Op
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.dsl.Order
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.modelling.Definition
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.modelling.Signature
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.ordering.OrderToken
import de.fraunhofer.aisec.codyze_core.wrapper.BackendConfiguration
import de.fraunhofer.aisec.cpg.graph.Node

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

    /** For each of the nodes in [this], there is a path to at least one of the nodes in [that]. */
    override infix fun Op.follows(that: Op) = FollowsEvaluator(ifOp = this, thenOp = that)

    /* Ensures the order of nodes as specified in the user configured [Order] object */
    override fun order(baseNodes: OrderToken?, block: Order.() -> Unit) =
        OrderEvaluator(
            baseNodes = baseNodes?.call()?.getAllNodes()?.filterIsInstance<Node>(),
            order = Order().apply(block)
        ) // TODO: use getNodes here instead?

    /**
     * Ensures that all calls to the [ops] have arguments that fit the parameters specified in [ops]
     */
    override fun only(vararg ops: Op) = OnlyEvaluator(ops.toList())
}
