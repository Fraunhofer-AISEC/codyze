package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.dsl

import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.CokoMarker
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.modelling.*

/**
 * Represents a group of functions that serve the same purpose in the API.
 *
 * @property definitions stores all definitions for the different functions
 */
// `Op` is defined here with an internal constructor because we only want the user to use the `op`
// function to make an `Op` object
@CokoMarker
class Op internal constructor() {
    val definitions = arrayListOf<Definition>()

    operator fun Definition.unaryPlus() {
        this@Op.definitions.add(this)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Op

        if (definitions != other.definitions) return false

        return true
    }

    override fun hashCode(): Int {
        return definitions.hashCode()
    }
}

/**
 * Create a [Op].
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
fun op(block: Op.() -> Unit) = Op().apply(block)

context(Op)
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
fun Signature.unordered(vararg unordered: Parameter) =
    this.apply { unorderedParameters.addAll(unordered) }
