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

package de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl

/**
 * This class works similar to the [Result] class in the Kotlin standard library.
 * The difference is that the [TransformationFailure] can store information other than [Throwable]s to be able to
 * describe the reasons for failure in more detail.
 */
sealed interface TransformationResult<E, T> {
    val isFailure: Boolean
    val isSuccess: Boolean

    fun getValueOrNull(): E?
    fun getFailureReasonOrNull(): T?
    override fun toString(): String

    companion object {
        /** Creates a [TransformationFailure] object with the given [reason]. */
        fun <E,T> failure(reason: T): TransformationResult<E, T> = TransformationFailure(reason)

        /** Creates a [TransformationSuccess] object with the given [value]. */
        fun <E,T> success(value: E): TransformationResult<E,T> = TransformationSuccess(value)
    }
}

/**
 * This class indicates that the operation was a success and the result is stored in [value].
 */
private data class TransformationSuccess<E,T>(val value: E) : TransformationResult<E,T> {
    override val isFailure: Boolean
        get() = false
    override val isSuccess: Boolean
        get() = true

    override fun getValueOrNull(): E? = value

    override fun getFailureReasonOrNull(): T? = null
}

/**
 * This class indicates that the operation was not successful and the reasons are stored in [reason].
 */
private data class TransformationFailure<E, T>(val reason: T) : TransformationResult<E,T> {
    override val isFailure: Boolean
        get() = true
    override val isSuccess: Boolean
        get() = false

    override fun getValueOrNull(): E? = null

    override fun getFailureReasonOrNull(): T = reason
}
