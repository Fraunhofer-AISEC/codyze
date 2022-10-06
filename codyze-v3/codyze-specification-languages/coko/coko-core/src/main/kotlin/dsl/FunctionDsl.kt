package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.dsl

import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.Project
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.modelling.Definition
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.modelling.Function
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.modelling.Parameter
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.modelling.Signature

context(Project)
/**
 * Create a [Function].
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
 *          arg3
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
 * @param block defines the [Definition]s of this [Function]
 */
inline fun function(block: Function.() -> Unit): Function {
    val function = Function()
    function.block()
    return function
}

context(Function)
/**
 * Create a [Definition] which can be added to the [Function].
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
 */
inline fun signature(block: Signature.() -> Unit) = Signature().apply(block)

context(Definition)
/**
 * Create a [Signature] which can be added to the [Definition]. The [Parameter]s are passed through
 * the vararg.
 */
fun signature(vararg parameters: Parameter) = signature { parameters.forEach { +it } }

context(Project)
/** Get all [Nodes] that are associated with this [Function]. */
fun Function.getAllNodes(): Nodes =
    this@Function.definitions.map { def -> this@Project.callFqn(def.fqn) }.flatten()

context(Project)
/**
 * Get all [Nodes] that are associated with this [Function] and fulfill the [Signature]s of the
 * [Definition]s.
 */
fun Function.getNodes(): Nodes =
    this@Function.definitions
        .map { def ->
            this@Project.callFqn(def.fqn) {
                def.signatures.all { sig -> signature(*sig.parameters.toTypedArray()) }
            }
        }
        .flatten()
