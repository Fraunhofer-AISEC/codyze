package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.modelling

import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.CokoMarker

/**
 * Goal:
 * ```
 *  fun init() =
 *      function {
 *          +definition("javax.crypto.Cipher.init") {
 *              +signature(...)
 *              +signature {
 *                  +...
 *                  +...
 *              }
 *          }
 *          +definition("some.other.name") {
 *
 *          }
 *
 *      }
 * ```
 */
typealias Parameter = Any?

/**
 * Represents a group of functions that serve the same purpose in the API.
 *
 * @property definitions stores all definitions for the different functions
 */
@CokoMarker
class Op {
    val definitions = arrayListOf<Definition>()

    operator fun Definition.unaryPlus() {
        this@Op.definitions.add(this)
    }
}

/**
 * Represents the definitions of a function with the fully qualified name [fqn].
 *
 * @property signatures stores all possible [Signature]s for this function
 */
@CokoMarker
class Definition(val fqn: String) {
    val signatures = arrayListOf<Signature>()

    operator fun Signature.unaryPlus() {
        this@Definition.signatures.add(this)
    }
}

/**
 * Represents a signature of a function.
 *
 * @property parameters stores all [Parameter]s of this signature
 */
@CokoMarker
class Signature {
    val parameters = arrayListOf<Parameter>()

    operator fun Parameter.unaryPlus() {
        parameters.add(this)
    }
}
