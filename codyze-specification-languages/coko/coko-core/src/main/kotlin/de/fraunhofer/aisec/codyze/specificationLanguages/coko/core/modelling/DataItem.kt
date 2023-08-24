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

sealed interface DataItem : ConditionComponent

sealed interface HasTransformation<E> : DataItem {
    val transformation: (BackendDataItem) -> TransformationResult<E>
    infix fun <T> withTransformation(
        newTransformation: (BackendDataItem) -> TransformationResult<T>
    ): HasTransformation<T>
}

data class ReturnValueItem<E>(
    val op: Op,
    override val transformation: (BackendDataItem) -> TransformationResult<E> = {
        TransformationResult.failure("No transformation given from user")
    }
) : HasTransformation<E> {
    override fun toString(): String = "Return value of $op"

    override infix fun <T> withTransformation(
        newTransformation: (BackendDataItem) -> TransformationResult<T>
    ): ReturnValueItem<T> =
        ReturnValueItem(op = op, transformation = newTransformation)
}

data class ArgumentItem<E>(
    val op: Op,
    val number: Int,
    override val transformation: (BackendDataItem) -> TransformationResult<E> = {
        TransformationResult.failure("No transformation given from user")
    }
) : HasTransformation<E> {
    init {
        require(number >= 0) { "The number of the argument cannot be negative" }
    }

    override fun <T> withTransformation(
        newTransformation: (BackendDataItem) -> TransformationResult<T>
    ): ArgumentItem<T> =
        ArgumentItem(op = op, number = number, transformation = newTransformation)

    override fun toString(): String = "${number.ordinal()} argument of $op"

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

data class Value internal constructor(val value: Any) : DataItem

fun value(value: Any): DataItem =
    when (value) {
        is DataItem -> value
        else -> Value(value)
    }
