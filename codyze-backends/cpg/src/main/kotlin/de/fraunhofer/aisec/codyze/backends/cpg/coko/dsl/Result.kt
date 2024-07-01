package de.fraunhofer.aisec.codyze.backends.cpg.coko.dsl

import de.fraunhofer.aisec.codyze.backends.cpg.coko.dsl.Result.*

enum class Result {
    VALID,
    INVALID,
    OPEN;

    companion object {
        fun convert(from: Any?): Result {
            return when(from) {
                is Result -> from
                is Boolean -> if (from) VALID else INVALID
                else -> OPEN
            }
        }
    }
}

inline fun <T> Iterable<T>.allResult(predicate: (T) -> Result?): Result {
    for (element in this) {
        if (predicate(element) == OPEN) return OPEN
        else if (predicate(element) == INVALID) return INVALID
    }
    return VALID
}

inline fun <T> Iterable<T>.anyResult(predicate: (T) -> Result?): Result {
    var openFlag = false
    for (element in this) {
        if (predicate(element) == VALID) return VALID
        else if (predicate(element) == OPEN) openFlag = true
    }
    return if (openFlag) OPEN else INVALID
}

inline fun <T> Array<T>.allResult(predicate: (T) -> Result?): Result {
    for (element in this) {
        if (predicate(element) == OPEN) return OPEN
        else if (predicate(element) == INVALID) return INVALID
    }
    return VALID
}

inline fun <T> Array<T>.anyResult(predicate: (T) -> Result?): Result {
    var openFlag = false
    for (element in this) {
        if (predicate(element) == VALID) return VALID
        else if (predicate(element) == OPEN) openFlag = true
    }
    return if (openFlag) OPEN else INVALID
}

/** precedence order for ternary and: OPEN > INVALID > VALID */
fun Result.and(other: Result): Result {
    return if (this == OPEN || other == OPEN) OPEN
    else if (this == INVALID || other == INVALID) INVALID
    else VALID
}
