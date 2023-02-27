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
package de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.modelling

import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.CokoMarker

/** Represents a parameter in the [Signature] */
typealias Parameter = Any?

/** Represents a group of parameters that all belong to the same index */
@CokoMarker
class ParameterGroup {
    val parameters = arrayListOf<Parameter>()

    operator fun Parameter.unaryMinus() {
        add(this)
    }

    fun add(parameter: Parameter) {
        parameters.add(parameter)
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

    override fun toString(): String {
        return parameters.joinToString(prefix = "{", postfix = "}")
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

    fun add(signature: Signature) {
        this.signatures.removeIf { it === signature }
        this.signatures.add(signature)
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

    override fun toString(): String {
        return signatures.filterNot { it.parameters.contains(null) || it.unorderedParameters.contains(null) }
            .joinToString(
                prefix = fqn,
                separator = ", $fqn"
            )
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

    operator fun Parameter.unaryMinus() {
        add(this)
    }

    fun add(parameter: Parameter) {
        parameters.add(parameter)
    }

    fun add(parameterGroup: ParameterGroup) {
        parameters.removeIf { it === parameterGroup }
        parameters.add(parameterGroup)
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

    override fun toString(): String {
        val unorderedString = if (unorderedParameters.isNotEmpty()) {
            " unordered: ${unorderedParameters.joinToString()}"
        } else {
            ""
        }
        return parameters.joinToString(prefix = "(", postfix = ")") + unorderedString
    }
}
