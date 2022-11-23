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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ParameterGroup

        if (parameters != other.parameters) return false

        return true
    }

    override fun hashCode(): Int {
        return parameters.hashCode()
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Definition

        if (fqn != other.fqn) return false
        if (signatures != other.signatures) return false

        return true
    }

    override fun hashCode(): Int {
        var result = fqn.hashCode()
        result = 31 * result + signatures.hashCode()
        return result
    }
}

/**
 * Represents a signature of a function.
 *
 * Two [Signature]s are considered equal if they have the same amount of [parameters] and
 * [unorderedParameters]. The [equals] function will not check the content of [parameters] and
 * [unorderedParameters].
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

    /**
     * Two [Signature]s are considered equal if they have the same amount of [parameters] and
     * [unorderedParameters]. The [equals] function will not check the content of [parameters] and
     * [unorderedParameters].
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Signature

        if (parameters.size != other.parameters.size) return false
        if (unorderedParameters.size != other.unorderedParameters.size) return false

        return true
    }

    override fun hashCode(): Int {
        var result = parameters.size.hashCode()
        result = 31 * result + unorderedParameters.size.hashCode()
        return result
    }
}
