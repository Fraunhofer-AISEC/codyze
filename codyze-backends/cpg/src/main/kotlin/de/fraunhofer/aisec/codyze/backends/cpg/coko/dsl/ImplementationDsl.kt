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
@file:Suppress("TooManyFunctions")

package de.fraunhofer.aisec.codyze.backends.cpg.coko.dsl

import de.fraunhofer.aisec.codyze.backends.cpg.coko.Nodes
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.CokoBackend
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.CokoMarker
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.*
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.modelling.Definition
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.modelling.ParameterGroup
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.modelling.Signature
import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.*
import de.fraunhofer.aisec.cpg.graph.declarations.ValueDeclaration
import de.fraunhofer.aisec.cpg.graph.statements.expressions.CallExpression
import de.fraunhofer.aisec.cpg.graph.statements.expressions.ConstructExpression
import de.fraunhofer.aisec.cpg.graph.statements.expressions.Expression
import de.fraunhofer.aisec.cpg.graph.statements.expressions.MemberExpression
import de.fraunhofer.aisec.cpg.query.dataFlow

//
// all functions/properties defined here must use CokoBackend
// when they should be available in Coko
//
val CokoBackend.cpg: TranslationResult
    get() = this.backendData as TranslationResult

/** Get all [Nodes] that are associated with this [Op]. */
context(CokoBackend)
fun Op.getAllNodes(): Nodes =
    when (this@Op) {
        is FunctionOp ->
            this@Op.definitions.map { def -> this@CokoBackend.callFqn(def.fqn) }.flatten()
        is ConstructorOp -> this@CokoBackend.constructor(this.classFqn)
    }

/**
 * Get all [Nodes] that are associated with this [Op] and fulfill the [Signature]s of the
 * [Definition]s.
 */
context(CokoBackend)
fun Op.getNodes(): Nodes =
    when (this@Op) {
        is FunctionOp ->
            this@Op.definitions
                .map { def ->
                    this@CokoBackend.callFqn(def.fqn) {
                        def.signatures.any { sig ->
                            signature(*sig.parameters.toTypedArray()) &&
                                    sig.unorderedParameters.all { it?.flowsTo(arguments) ?: false }
                        }
                    }
                }
                .flatten()
        is ConstructorOp ->
            this@Op.signatures
                .map { sig ->
                    this@CokoBackend.constructor(this@Op.classFqn) {
                        signature(*sig.parameters.toTypedArray()) &&
                                sig.unorderedParameters.all { it?.flowsTo(arguments) ?: false }
                    }
                }
                .flatten()
    }

/** Returns a list of [ValueDeclaration]s with the matching name. */
fun CokoBackend.variable(name: String): List<ValueDeclaration> {
    return cpg.allChildren { it.name == name }
}

fun CokoBackend.constructor(
    classFqn: String,
    predicate: (@CokoMarker CallExpression).() -> Boolean = { true }
): List<ConstructExpression> {
    return cpg.calls.filterIsInstance<ConstructExpression>().filter {
        it.type.typeName == classFqn && predicate(it)
    }
}

/** Returns a list of [CallExpression]s with the matching [name] and fulfilling [predicate]. */
fun CokoBackend.call(
    name: String,
    predicate: (@CokoMarker CallExpression).() -> Boolean = { true }
): List<CallExpression> {
    return cpg.calls { it.name == name && predicate(it) }
}

/**
 * Returns a list of [CallExpression]s with the matching [fqn] (fully-qualified name) and fulfilling
 * [predicate].
 */
fun CokoBackend.callFqn(
    fqn: String,
    predicate: (@CokoMarker CallExpression).() -> Boolean = { true }
): List<CallExpression> {
    return cpg.calls { it.fqn == fqn && predicate(it) }
}

/** Returns a list of [MemberExpression]s with the matching something. */
fun CokoBackend.memberExpr(
    predicate: (@CokoMarker MemberExpression).() -> Boolean
): List<MemberExpression> {
    return cpg.allChildren(predicate)
}

// this extension function should only be available in the context of a CallExpression
context(CallExpression)
        /**
         * Checks if there's a data flow path from "this" to [that].
         * - If this is a String, we evaluate it as a regex.
         * - If this is a Collection, we check if at least one of the elements flows to [that]
         * - If this is a [Node], we use the DFG of the CPG.
         */
        infix fun Any.flowsTo(that: Node): Boolean =
    if (this is Wildcard) {
        true
    } else {
        when (this) {
            is String -> Regex(this).matches((that as? Expression)?.evaluate()?.toString().orEmpty())
            is Iterable<*> -> this.any { it?.flowsTo(that) ?: false }
            is Array<*> -> this.any { it?.flowsTo(that) ?: false }
            is Node -> dataFlow(this, that).value
            is ParameterGroup -> this.parameters.all { it?.flowsTo(that) ?: false }
            else -> this == (that as? Expression)?.evaluate()
        }
    }

// it should only be available in the context of a CallExpression
context(CallExpression)
        /**
         * Checks if there's a data flow path from "this" to any of the elements in [that].
         * - If this is a String, we evaluate it as a regex.
         * - If this is a Collection, we check if at least one of the elements flows to [that]
         * - If this is a [Node], we use the DFG of the CPG.
         */
        infix fun Any.flowsTo(that: Collection<Node>): Boolean =
    if (this is Wildcard) {
        true
    } else {
        when (this@flowsTo) {
            is String -> that.any { Regex(this).matches((it as? Expression)?.evaluate()?.toString().orEmpty()) }
            is Iterable<*> -> this.any { it?.flowsTo(that) ?: false }
            is Array<*> -> this.any { it?.flowsTo((that)) ?: false }
            is Node -> that.any { dataFlow(this, it).value }
            is ParameterGroup -> this.parameters.all { it?.flowsTo(that) ?: false }
            else -> this in that.map { (it as Expression).evaluate() }
        }
    }

context(CokoBackend)
// TODO: better description
// TODO: in mark there is "..." to symbolize that the last arguments don't matter
// TODO: how to model return value assignments
/**
 * Checks if the [CallExpression] matches the signature specified with [parameters]. Returns false
 * if there are nulls in [parameters],
 *
 * @param parameters specifies the order of the parameters of the function.
 *
 * If a parameter is a [Type] object, the function will check if the argument has the same type.
 *
 * If a parameter is a [ParamWithType] object, the function will check if the argument has
 * the same type and if the [Any] object flows to the argument
 *
 * @param hasVarargs specifies if the function has a variable number of arguments at the end which
 * are not important to the analysis
 */
@Suppress("UnsafeCallOnNullableType")
fun CallExpression.signature(vararg parameters: Any?, hasVarargs: Boolean = false): Boolean {
    // checks if amount of parameters is the same as amount of arguments of this CallExpression
    return checkArgsSize(parameters, hasVarargs) &&
            // checks if the CallExpression matches with the parameters
            parameters.withIndex().all { (i: Int, parameter: Any?) ->
                when (parameter) {
                    // if any parameter is null, signature returns false
                    null -> false
                    is ParamWithType ->
                        // if `parameter` is a `ParamWithType` object we want to check the type and
                        // if there is dataflow
                        checkType(parameter.type, i) &&
                                parameter.param flowsTo arguments[i]
                    // checks if the type of the argument is the same
                    is Type -> checkType(parameter, i)
                    // check if any of the Nodes from the Op flow to the argument
                    is Op -> parameter.getNodes() flowsTo arguments[i]
                    // checks if there is dataflow from the parameter to the argument in the same position
                    else -> parameter flowsTo arguments[i]
                }
            }
}

/** Checks the [type] against the type of the argument at [index] for the Call Expression */
private fun CallExpression.checkType(type: Type, index: Int) = arguments[index].type.typeName.endsWith(type.fqn)

/** Checks if the number of parameters matches the number of arguments of [CallExpression] */
private fun CallExpression.checkArgsSize(parameters: Array<*>, hasVarargs: Boolean) =
    if (hasVarargs) {
        parameters.size <= arguments.size
    } else {
        parameters.size == arguments.size
    }
