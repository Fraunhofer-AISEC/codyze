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

import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.Condition
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.Op
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.modelling.CallConditionComponent
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.modelling.ConditionComponent

/**
 * An evaluator that describes what should happen around the code that the [premise] represents.
 */
abstract class WheneverEvaluator(protected val premise: ConditionComponent) : Evaluator {
    protected val ensures: MutableList<ConditionComponent> = mutableListOf()
    protected val callAssertions: MutableList<CallAssertion> = mutableListOf()

    /**
     * Specifies a condition that has to be ensured around the [premise].
     */
    fun ensure(block: Condition.() -> ConditionComponent) {
        ensures.add(Condition().run(block))
    }

    /**
     * Specifies that [op] has to be called in the given [location] in regard to the [premise].
     */
    fun call(op: Op, location: CallLocationBuilder.() -> CallLocation? = { null }) {
        callAssertions.add(CallAssertion(op, CallLocationBuilder().run(location)))
    }
}

class CallLocationBuilder {
    infix fun Direction.within(scope: Scope) = CallLocation(this@Direction, scope)
}
data class CallLocation(val direction: Direction, val scope: Scope)

enum class Direction {
    Afterwards, Before, Somewhere
}

enum class Scope {
    Function, Block
}

class CallAssertion(op: Op, val location: CallLocation? = null) : CallConditionComponent(op) {
    override fun toString(): String =
        "Call $op ${
            if (location != null) {
                "${location.direction} in ${location.scope}."
            } else {
                ""
            }
        }"
}

class ListBuilder {
    operator fun <E>get(vararg things: E): List<E> = things.toList()
}
