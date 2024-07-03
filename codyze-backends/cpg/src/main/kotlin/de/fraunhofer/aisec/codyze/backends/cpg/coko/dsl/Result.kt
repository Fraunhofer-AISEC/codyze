/*
 * Copyright (c) 2024, Fraunhofer AISEC. All rights reserved.
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
package de.fraunhofer.aisec.codyze.backends.cpg.coko.dsl

import de.fraunhofer.aisec.codyze.backends.cpg.coko.dsl.Result.*

/**
 * A data class that serves as a ternary value for the analysis result.
 *
 * OPEN is used where we cannot deduce either VALID or INVALID results because of lack of information.
 */
enum class Result {
    VALID,
    INVALID,
    OPEN;

    companion object {
        fun convert(from: Any?): Result {
            return when (from) {
                is Result -> from
                is Boolean -> if (from) VALID else INVALID
                else -> OPEN
            }
        }
    }
}

/** returns VALID if all Results are VALID, otherwise returns OPEN if any result is OPEN, otherwise returns INVALID */
inline fun <T> Iterable<T>.allResult(predicate: (T) -> Result?): Result {
    var invalidFlag = false
    for (element in this) {
        if (predicate(element) == OPEN) {
            return OPEN
        } else if (predicate(element) == INVALID) {
            invalidFlag = true
        }
    }
    return if (invalidFlag) INVALID else VALID
}

/** returns VALID if any Result is VALID, otherwise returns OPEN if any result is OPEN, otherwise returns INVALID */
inline fun <T> Iterable<T>.anyResult(predicate: (T) -> Result?): Result {
    var openFlag = false
    for (element in this) {
        if (predicate(element) == VALID) {
            return VALID
        } else if (predicate(element) == OPEN) {
            openFlag = true
        }
    }
    return if (openFlag) OPEN else INVALID
}

/** returns VALID if all Results are VALID, otherwise returns OPEN if any result is OPEN, otherwise returns INVALID */
inline fun <T> Array<T>.allResult(predicate: (T) -> Result?): Result {
    var invalidFlag = false
    for (element in this) {
        if (predicate(element) == OPEN) {
            return OPEN
        } else if (predicate(element) == INVALID)invalidFlag = true
    }
    return if (invalidFlag) INVALID else VALID
}

/** returns VALID if any Result is VALID, otherwise returns OPEN if any result is OPEN, otherwise returns INVALID */
inline fun <T> Array<T>.anyResult(predicate: (T) -> Result?): Result {
    var openFlag = false
    for (element in this) {
        if (predicate(element) == VALID) {
            return VALID
        } else if (predicate(element) == OPEN) {
            openFlag = true
        }
    }
    return if (openFlag) OPEN else INVALID
}

/** precedence order for ternary and: OPEN > INVALID > VALID */
fun Result.and(other: Result): Result {
    return if (this == OPEN || other == OPEN) {
        OPEN
    } else if (this == INVALID || other == INVALID) {
        INVALID
    } else {
        VALID
    }
}
