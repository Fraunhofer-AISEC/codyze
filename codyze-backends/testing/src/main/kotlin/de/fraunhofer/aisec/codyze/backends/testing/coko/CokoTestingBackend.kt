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
package de.fraunhofer.aisec.codyze.backends.testing.coko

import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.CokoBackend
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.Evaluator
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.Nodes
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.Op
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.Order
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.ordering.OrderToken

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
