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

sealed interface TransformationResult<E> {
    val isFailure: Boolean
    val isSuccess: Boolean

    fun getValueOrNull(): E?
    fun getMessageOrNull(): String?
    override fun toString(): String

    companion object {
        fun <E> failure(message: String): TransformationResult<E> = TransformationFailure(message)

        fun <E> success(value: E): TransformationResult<E> = TransformationSuccess(value)
    }
}

private data class TransformationSuccess<E>(val value: E) : TransformationResult<E> {
    override val isFailure: Boolean
        get() = false
    override val isSuccess: Boolean
        get() = true

    override fun getValueOrNull(): E? = value

    override fun getMessageOrNull(): String? = null
}

private data class TransformationFailure<E>(val message: String) : TransformationResult<E> {
    override val isFailure: Boolean
        get() = true
    override val isSuccess: Boolean
        get() = false

    override fun getValueOrNull(): E? = null

    override fun getMessageOrNull(): String = message
}
