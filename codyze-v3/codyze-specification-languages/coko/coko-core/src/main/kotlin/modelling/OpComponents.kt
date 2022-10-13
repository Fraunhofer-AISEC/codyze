package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.modelling

import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.CokoMarker

/** Represents a parameter in the [Signature] */
typealias Parameter = Any?

@CokoMarker
/** Represents a group of parameters that all belong to the same index */
class ParameterGroup {
    val parameters = arrayListOf<Parameter>()

    operator fun Parameter.unaryPlus() {
        parameters.add(this)
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
 * @property parameters stores [Parameter]s of this signature in the correct order
 * @property unorderedParameters store all unordered [Parameter]s of this signature. These
 * [Parameter]s don't correspond to a fixed index in the signature but need to flow to the function.
 */
@CokoMarker
class Signature {
    val parameters = arrayListOf<Parameter>()

    val unorderedParameters = arrayListOf<Parameter>()

    operator fun Parameter.unaryPlus() {
        parameters.add(this)
    }
}
