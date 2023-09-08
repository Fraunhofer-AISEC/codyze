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

package de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.modelling

import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.Op
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.TransformationResult

sealed interface DataItem<E> : ConditionLeaf {
    val transformation: (BackendDataItem) -> TransformationResult<E, String>
}

sealed interface CanChangeTransformation<E> : DataItem<E> {
    infix fun <T> withTransformation(
        newTransformation: (BackendDataItem) -> TransformationResult<T, String>
    ): DataItem<T>
}

data class ReturnValueItem<E>(
    val op: Op,
    override val transformation: (BackendDataItem) -> TransformationResult<E, String> = {
        TransformationResult.failure("No transformation given from user")
    }
) : CanChangeTransformation<E> {
    override fun toString(): String = "Return value of $op"

    override infix fun <T> withTransformation(
        newTransformation: (BackendDataItem) -> TransformationResult<T, String>
    ): ReturnValueItem<T> =
        ReturnValueItem(op = op, transformation = newTransformation)
}

data class ArgumentItem<E>(
    val op: Op,
    val index: Int,
    override val transformation: (BackendDataItem) -> TransformationResult<E, String> = {
        TransformationResult.failure("No transformation given from user")
    }
) : CanChangeTransformation<E> {
    init {
        // TODO: Do we count starting at 0 or 1?
        require(index >= 0) { "The number of the argument cannot be negative" }
    }

    override fun <T> withTransformation(
        newTransformation: (BackendDataItem) -> TransformationResult<T, String>
    ): ArgumentItem<T> =
        ArgumentItem(op = op, index = index, transformation = newTransformation)

    override fun toString(): String = "${(index + 1).ordinal()} argument of $op"

    @Suppress("MagicNumber")
    fun Int.ordinal(): String {
        return "$this" + when {
            (this % 100 in 11..13) -> "th"
            (this % 10) == 1 -> "st"
            (this % 10) == 2 -> "nd"
            (this % 10) == 3 -> "rd"
            else -> "th"
        }
    }
}

data class Value<E> internal constructor(val value: E) : DataItem<E> {
    override val transformation: (BackendDataItem) -> TransformationResult<E, String>
        get() = { TransformationResult.success(value) }
}

fun <E>value(value: DataItem<E>): DataItem<E> = value
fun <E>value(value: E): DataItem<E> = Value(value)
