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

import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.CokoMarker
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.modelling.*

@CokoMarker sealed interface Op

/**
 * Represents a group of functions that serve the same purpose in the API.
 *
 * Two [Op]s will be considered equal if they have the same [definitions]. This means that structure
 * of the [Op]s have to be equal as well as the [Definition.fqn]s but not the actual [Parameter]s
 * that are stored in the [Signature]s.
 *
 * @property definitions stores all definitions for the different functions
 */
// `Op` is defined here with an internal constructor because we only want the user to use the `op`
// function to make an `Op` object
class FunctionOp internal constructor() : Op {
    val definitions = arrayListOf<Definition>()

    fun add(definition: Definition) {
        this.definitions.removeIf { it === definition }
        this.definitions.add(definition)
    }

    /**
     * Two [Op]s will be considered equal if they have the same [definitions]. This means that
     * structure of the [Op]s have to be equal as well as the [Definition.fqn]s but not the actual
     * [Parameter]s that are stored in the [Signature]s.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FunctionOp

        if (definitions != other.definitions) return false

        return true
    }

    override fun hashCode(): Int {
        return definitions.hashCode()
    }

    override fun toString(): String {
        return definitions.joinToString()
    }
}

/**
 * Represents the constructor of a class.
 *
 * @property signatures stores all different signatures of the constructor.
 */
class ConstructorOp internal constructor(val classFqn: String) : Op {
    val signatures = arrayListOf<Signature>()

    fun add(signature: Signature) {
        this.signatures.removeIf { it === signature }
        this.signatures.add(signature)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ConstructorOp) return false

        if (signatures != other.signatures) return false

        return true
    }

    override fun hashCode(): Int {
        return signatures.hashCode()
    }

    override fun toString(): String {
        return signatures.joinToString(prefix = classFqn, separator = ", $classFqn")
    }
}

/**
 * Create a [FunctionOp].
 *
 * A full example:
 * ```kt
 * op {
 *   definition("my.fully.qualified.name") {
 *      signature {
 *          - arg1
 *          - arg2
 *          - arg3
 *      }
 *      signature(arg1, arg2)
 *      signature {
 *          - arg2
 *          - arg3
 *      }
 *   }
 *   definition("my.other.function") {
 *      signature(arg2, arg1)
 *   }
 * }
 * ```
 * This would model the functions:
 * ```kt
 * my.fully.qualified.name(arg1,arg2, arg3)
 * my.fully.qualified.name(arg1,arg2)
 * my.fully.qualified.name(arg2, arg3)
 *
 * my.other.function(arg2, arg1)
 * ```
 *
 * @param block defines the [Definition]s of this [Op]
 */
fun op(block: FunctionOp.() -> Unit) = FunctionOp().apply(block)

/** Create a [ConstructorOp]. */
fun constructor(classFqn: String, block: ConstructorOp.() -> Unit) =
    ConstructorOp(classFqn).apply(block)

/**
 * Create a [Definition] which can be added to the [Op].
 *
 * A minimal example
 * ```kt
 * function {
 *   definition("my.fully.qualified.name") {}
 * }
 * ```
 *
 * @param fqn the fully qualified name of the function this [Definition] is representing
 * @param block defines the [Signature]s of this [Definition]
 */
inline fun FunctionOp.definition(fqn: String, block: Definition.() -> Unit) =
    Definition(fqn).apply(block).also { this.add(it) }

/**
 * Create a [Signature] which can be added to the [Definition]. The [Parameter]s are defined in the
 * [block].
 *
 * @param unordered are all [Parameter]s for which the order is irrelevant and that only need to
 */
inline fun Definition.signature(
    unordered: Array<out Parameter> = emptyArray(),
    block: Signature.() -> Unit
) = Signature().apply(block).apply { unorderedParameters.addAll(unordered) }.also { this.add(it) }

/**
 * Create a [Signature] which can be added to the [Definition]. The [Parameter]s are passed through
 * the vararg.
 */
fun Definition.signature(vararg parameters: Parameter) = signature { parameters.forEach { - it } }

/** Create a [ParameterGroup] which can be added to the [Signature]. */
inline fun Signature.group(block: ParameterGroup.() -> Unit) = ParameterGroup().apply(block).also { this.add(it) }

/** Create a [ParameterGroup] which can be added to the [Signature]. */
fun Signature.group(vararg parameters: Parameter) = group { parameters.forEach { - it } }

context(Definition)
/** Add unordered [Parameter]s to the [Signature]. */
fun Signature.unordered(vararg unordered: Parameter) =
    this.apply { unorderedParameters.addAll(unordered) }

/**
 * Create a [Signature] which can be added to the [ConstructorOp]. The [Parameter]s are defined in
 * the [block].
 */
inline fun ConstructorOp.signature(block: Signature.() -> Unit) = Signature().apply(block).also { this.add(it) }

/**
 * Create a [Signature] which can be added to the [ConstructorOp]. The [Parameter]s are passed
 * through the vararg.
 */
fun ConstructorOp.signature(vararg parameters: Parameter) = signature { parameters.forEach { - it } }
