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
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.ordering.OrderFragment
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.ordering.TerminalOrderNode

@CokoMarker sealed interface Op : OrderFragment, ConditionComponent {
    val ownerClassFqn: String

    val returnValue: ReturnValueItem<Any>
        get() = ReturnValueItem(this)

    val arguments: Arguments
        get() = Arguments(this)

    override fun toNode(): TerminalOrderNode = TerminalOrderNode(
        baseName = ownerClassFqn,
        opName = hashCode().toString()
    )
}

class Arguments(val op: Op) {
    operator fun get(index: Int): ArgumentItem<Any> = ArgumentItem(op, index)
}

/**
 * Represents a group of functions that serve the same purpose in the API.
 *
 * Two [FunctionOp]s will be considered equal if they have the same [definitions]. This means that structure
 * of the [FunctionOp]s have to be equal as well as the [Definition.fqn]s but not the actual [Parameter]s
 * that are stored in the [Signature]s.
 *
 * @property definitions stores all definitions for the different functions
 */
// `FunctionOp` is defined here with an internal constructor because we only want the user to use the `op`
// function to make an `FunctionOp` object
class FunctionOp internal constructor(
    override val ownerClassFqn: String = "",
) : Op {
    val definitions = mutableSetOf<Definition>()

    fun add(definition: Definition) {
        this.definitions.removeIf { it === definition }
        this.definitions.add(definition)
    }

    operator fun String.invoke(block: Definition.() -> Unit) = definition(this, block)

    /**
     * Two [FunctionOp]s will be considered equal if they have the same [definitions]. This means that
     * structure of the [FunctionOp]s have to be equal as well as the [Definition.fqn]s but not the actual
     * [Parameter]s that are stored in the [Signature]s.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FunctionOp

        return definitions == other.definitions
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
class ConstructorOp internal constructor(
    val classFqn: String,
    override val ownerClassFqn: String = "",
) : Op {
    val signatures = mutableSetOf<Signature>()

    fun add(signature: Signature) {
        this.signatures.removeIf { it === signature }
        this.signatures.add(signature)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ConstructorOp) return false

        return signatures == other.signatures
    }

    override fun hashCode(): Int {
        return signatures.hashCode()
    }

    override fun toString(): String {
        return signatures.joinToString(prefix = classFqn, separator = ", $classFqn")
    }
}

/** An [Op] that contains other [Op]s */
data class GroupingOp(val ops: Set<Op>, override val ownerClassFqn: String = "") : Op {
    override fun toString(): String {
        return ops.joinToString()
    }
}

context(Any) // This is needed to have access to the owner of this function
// -> in which class is the function defined that created this [OP]
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
 *   "my.other.function" { // the call to 'definition' can be omitted
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
 * @param block defines the [Definition]s of this [FunctionOp]
 */
fun op(block: FunctionOp.() -> Unit): FunctionOp {
    val functionOp = FunctionOp(this@Any::class.java.name).apply(block)
    check(functionOp.definitions.isNotEmpty()) { "Coko does not support empty OPs" }
    return functionOp
}

context(Any) // This is needed to have access to the owner of this function
// -> in which class is the function defined that created this [OP]
/** Create a [ConstructorOp]. */
fun constructor(classFqn: String, block: ConstructorOp.() -> Unit): ConstructorOp {
    return ConstructorOp(classFqn, this@Any::class.java.name).apply(block)
}

context(Any)
fun opGroup(vararg ops: Op): GroupingOp = GroupingOp(ops.toSet(), this@Any::class.java.name)

/**
 * Create a [Definition] which can be added to the [FunctionOp].
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
fun Definition.signature(vararg parameters: Parameter) = signature { parameters.forEach { this.add(it) } }

/** Create a [ParameterGroup] which can be added to the [Signature]. */
inline fun Signature.group(block: ParameterGroup.() -> Unit) = ParameterGroup().apply(block).also { this.add(it) }

/** Create a [ParameterGroup] which can be added to the [Signature]. */
fun Signature.group(vararg parameters: Parameter) = group { parameters.forEach { this.add(it) } }

context(Signature)
/** Add unordered [Parameter]s to the [Signature]. */
fun unordered(vararg unordered: Parameter) =
    this@Signature.apply { unorderedParameters.addAll(unordered) }

/**
 * Create a [Signature] which can be added to the [ConstructorOp]. The [Parameter]s are defined in
 * the [block].
 */
inline fun ConstructorOp.signature(block: Signature.() -> Unit) = Signature().apply(block).also { this.add(it) }

/**
 * Create a [Signature] which can be added to the [ConstructorOp]. The [Parameter]s are passed
 * through the vararg.
 */
fun ConstructorOp.signature(vararg parameters: Parameter) = signature { parameters.forEach { this.add(it) } }
