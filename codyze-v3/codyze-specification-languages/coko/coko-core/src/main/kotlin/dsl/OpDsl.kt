package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.dsl

import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.CokoMarker
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.modelling.*

sealed interface Op

/**
 * Represents a group of functions that serve the same purpose in the API.
 *
 * Two [Op]s will be considered equal if they have the same [definitions].
 * This means that structure of the [Op]s have to be equal as well as the [Definition.fqn]s but not the actual [Parameter]s that are stored in the [Signature]s.
 *
 * @property definitions stores all definitions for the different functions
 */
// `Op` is defined here with an internal constructor because we only want the user to use the `op`
// function to make an `Op` object
@CokoMarker
class FunctionOp internal constructor(): Op {
    val definitions = arrayListOf<Definition>()

    operator fun Definition.unaryPlus() {
        this@FunctionOp.definitions.add(this)
    }

    /**
     * Two [Op]s will be considered equal if they have the same [definitions].
     * This means that structure of the [Op]s have to be equal as well as the [Definition.fqn]s but not the actual [Parameter]s that are stored in the [Signature]s.
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
}

/**
 * Represents the constructor of a class.
 *
 * @property signatures stores all different signatures of the constructor.
 */
class ConstructorOp internal constructor(val classFqn: String): Op {
    val signatures = arrayListOf<Signature>()

    operator fun Signature.unaryPlus() {
        this@ConstructorOp.signatures.add(this)
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


}

/**
 * Create a [FunctionOp].
 *
 * A full example:
 * ```kt
 * function {
 *   +definition("my.fully.qualified.name") {
 *      +signature {
 *          +arg1
 *          +arg2
 *          +arg3
 *      }
 *      +signature(arg1, arg2)
 *      +signature {
 *          +arg2
 *          +arg3
 *      }
 *   }
 *   +definition("my.other.function") {
 *      +signature(arg2, arg1)
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

/**
 * Create a [ConstructorOp].
 */
fun constructor(classFqn: String, block: ConstructorOp.() -> Unit) = ConstructorOp(classFqn).apply(block)

context(FunctionOp)
/**
 * Create a [Definition] which can be added to the [Op].
 *
 * A minimal example
 * ```kt
 * function {
 *   +definition("my.fully.qualified.name") {}
 * }
 * ```
 *
 * @param fqn the fully qualified name of the function this [Definition] is representing
 * @param block defines the [Signature]s of this [Definition]
 */
inline fun definition(fqn: String, block: Definition.() -> Unit) = Definition(fqn).apply(block)

context(Definition)
/**
 * Create a [Signature] which can be added to the [Definition]. The [Parameter]s are defined in the
 * [block].
 *
 * @param unordered are all [Parameter]s for which the order is irrelevant and that only need to
 */
inline fun signature(unordered: Array<out Parameter> = emptyArray(), block: Signature.() -> Unit) =
    Signature().apply(block).apply { unorderedParameters.addAll(unordered) }

context(Definition)
/**
 * Create a [Signature] which can be added to the [Definition]. The [Parameter]s are passed through
 * the vararg.
 */
fun signature(vararg parameters: Parameter) = signature { parameters.forEach { +it } }

context(Signature)
/** Create a [ParameterGroup] which can be added to the [Signature]. */
inline fun group(block: ParameterGroup.() -> Unit) = ParameterGroup().apply(block)

context(Definition)
/** Add unordered [Parameter]s to the [Signature]. */
fun Signature.unordered(vararg unordered: Parameter) = this.apply { unorderedParameters.addAll(unordered) }

context(ConstructorOp)
/**
* Create a [Signature] which can be added to the [ConstructorOp]. The [Parameter]s are defined in the
* [block].
*/
inline fun signature(block: Signature.() -> Unit) =
    Signature().apply(block)

context(ConstructorOp)
/**
 * Create a [Signature] which can be added to the [ConstructorOp]. The [Parameter]s are passed through
 * the vararg.
 */
fun signature(vararg parameters: Parameter) = signature { parameters.forEach { +it } }
