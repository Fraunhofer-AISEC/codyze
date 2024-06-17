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
package de.fraunhofer.aisec.codyze.specificationLanguages.coko.core

import de.fraunhofer.aisec.codyze.core.backend.Backend
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.Condition
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.Op
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.Order
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.modelling.ConditionComponent
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.ordering.OrderToken
import kotlin.reflect.KFunction

typealias CokoRule = KFunction<Evaluator>

@DslMarker
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.CLASS,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.TYPEALIAS,
    AnnotationTarget.TYPE
)
annotation class CokoMarker

/*
 * All the functionality of the DSL are implemented as member/extension functions on [CokoBackend].
 */
@Suppress("UNUSED")
@CokoMarker
interface CokoBackend : Backend {
    /** For each of the nodes in [this], there is a path to at least one of the nodes in [that]. */
    infix fun Op.followedBy(that: Op): Evaluator

    /** Ensures the order of nodes as specified in the user configured [Order] object */
    fun order(
        baseNodes: OrderToken,
        block: Order.() -> Unit
    ): Evaluator

    /** Ensures the order of nodes as specified in the user configured [Order] object */
    fun order(
        baseNodes: Op,
        block: Order.() -> Unit
    ): Evaluator

    /**
     * Ensures that all calls to the [ops] have arguments that fit the parameters specified in [ops]
     */
    fun only(vararg ops: Op): Evaluator

    /** Ensures that there are no calls to the [ops] which have arguments that fit the parameters specified in [ops] */
    fun never(vararg ops: Op): Evaluator

    /** Verifies that the [assertionBlock] is ensured when [premise] is found */
    fun whenever(
        premise: Condition.() -> ConditionComponent,
        assertionBlock: WheneverEvaluator.() -> Unit
    ): WheneverEvaluator

    fun whenever(premise: ConditionComponent, assertionBlock: WheneverEvaluator.() -> Unit): WheneverEvaluator

    /** Verifies that the argument at [argPos] of [targetOp] stems from a call to [originOp] */
    fun argumentOrigin(
        targetOp: KFunction<Op>,
        argPos: Int,
        originOp: KFunction<Op>,
    ): Evaluator
}
