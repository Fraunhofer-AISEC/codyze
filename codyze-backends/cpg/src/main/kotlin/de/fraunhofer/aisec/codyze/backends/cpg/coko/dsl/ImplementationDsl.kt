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
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.modelling.*
import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.*
import de.fraunhofer.aisec.cpg.graph.declarations.ValueDeclaration
import de.fraunhofer.aisec.cpg.graph.statements.expressions.*
import de.fraunhofer.aisec.cpg.query.dataFlow
import de.fraunhofer.aisec.cpg.query.executionPath

//
// all functions/properties defined here must use CokoBackend
// when they should be available in Coko
//
val CokoBackend.cpg: TranslationResult
    get() = this.backendData as TranslationResult

/** Get all [Nodes] that are associated with this [Op]. */
context(CokoBackend)
fun Op.cpgGetAllNodes(): Collection<CallExpression> =
    when (this@Op) {
        is FunctionOp ->
            this@Op.definitions.flatMap { def -> this@CokoBackend.cpgCallFqn(def.fqn) }
        is ConstructorOp -> this@CokoBackend.cpgConstructor(this.classFqn)
        is GroupingOp -> this@Op.ops.flatMap { it.cpgGetAllNodes() }
        is ConditionalOp -> {
            val resultNodes = resultOp.cpgGetAllNodes()
            val conditionNodes = conditionOp.cpgGetAllNodes()
            resultNodes.filter { resultNode ->
                conditionNodes.any { conditionNode ->
                    dataFlow(conditionNode, resultNode).value
                }
            }
        }
    }

/**
 * Get all [Nodes] that are associated with this [Op] and fulfill the [Signature]s of the
 * [Definition]s.
 */
context(CokoBackend)
fun Op.cpgGetNodes(): Collection<CallExpression> =
    when (this@Op) {
        is FunctionOp ->
            this@Op.definitions
                .flatMap { def ->
                    this@CokoBackend.cpgCallFqn(def.fqn) {
                        def.signatures.any { sig ->
                            cpgSignature(*sig.parameters.toTypedArray()) &&
                                sig.unorderedParameters.all { it?.cpgFlowsTo(arguments) ?: false }
                        }
                    }
                }
        is ConstructorOp ->
            this@Op.signatures
                .flatMap { sig ->
                    this@CokoBackend.cpgConstructor(this@Op.classFqn) {
                        cpgSignature(*sig.parameters.toTypedArray()) &&
                            sig.unorderedParameters.all { it?.cpgFlowsTo(arguments) ?: false }
                    }
                }
        is GroupingOp -> this@Op.ops.flatMap { it.cpgGetNodes() }
        is ConditionalOp -> {
            val resultNodes = resultOp.cpgGetNodes()
            val conditionNodes = conditionOp.cpgGetNodes()
            resultNodes.filter { resultNode ->
                conditionNodes.any { conditionNode ->
                    // TODO: Is it correct to use the EOG relationship here?
                    val result = executionPath(conditionNode, resultNode)
                    result.value
                }
            }
        }
    }

/** Returns a list of [ValueDeclaration]s with the matching name. */
fun CokoBackend.cpgVariable(name: String): List<ValueDeclaration> {
    return cpg.allChildren { it.name.lastPartsMatch(name) }
}

/** Returns a list of [ValueDeclaration]s with the matching [fqn]. */
fun CokoBackend.cpgVariableFqn(fqn: String): List<ValueDeclaration> {
    return cpg.allChildren { it.name.toString() == fqn }
}

/** Returns a list of [ConstructExpression]s with the matching [classFqn] and fulfilling [predicate]. */
fun CokoBackend.cpgConstructor(
    classFqn: String,
    predicate: (@CokoMarker CallExpression).() -> Boolean = { true }
): List<ConstructExpression> {
    return cpg.calls.filterIsInstance<ConstructExpression>().filter {
        it.type.typeName == classFqn && predicate(it)
    }
}

/** Returns a list of [CallExpression]s with the matching [name] and fulfilling [predicate]. */
fun CokoBackend.cpgCall(
    name: String,
    predicate: (@CokoMarker CallExpression).() -> Boolean = { true }
): List<CallExpression> {
    return cpg.calls { it.name.lastPartsMatch(name) && predicate(it) }
}

/**
 * Returns a list of [CallExpression]s with the matching [fqn] (fully-qualified name) and fulfilling
 * [predicate].
 */
fun CokoBackend.cpgCallFqn(
    fqn: String,
    predicate: (@CokoMarker CallExpression).() -> Boolean = { true }
): List<CallExpression> {
    return cpg.calls { it.name.toString() == fqn && predicate(it) }
}

/** Returns a list of [MemberExpression]s with the matching something. */
fun CokoBackend.cpgMemberExpr(
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
infix fun Any.cpgFlowsTo(that: Node): Boolean =
    this.cpgFlowsTo(listOf(that))

// it should only be available in the context of a CallExpression
context(CallExpression)
/**
 * Checks if there's a data flow path from "this" to any of the elements in [that].
 * - If this is a String, we evaluate it as a regex.
 * - If this is a Collection, we check if at least one of the elements flows to [that]
 * - If this is a [Node], we use the DFG of the CPG.
 */
infix fun Any.cpgFlowsTo(that: Collection<Node>): Boolean =
    if (this is Wildcard) {
        true
    } else {
        when (this) {
            is String -> that.any {
                val regex = Regex(this)
                regex.matches((it as? Expression)?.evaluate()?.toString().orEmpty()) || regex.matches(it.code.orEmpty())
            }
            is Iterable<*> -> this.any { it?.cpgFlowsTo(that) ?: false }
            is Array<*> -> this.any { it?.cpgFlowsTo(that) ?: false }
            is Node -> that.any { dataFlow(this, it).value }
            is ParameterGroup -> this.parameters.all { it?.cpgFlowsTo(that) ?: false }
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
fun CallExpression.cpgSignature(vararg parameters: Any?, hasVarargs: Boolean = false): Boolean {
    // checks if amount of parameters is the same as amount of arguments of this CallExpression
    return cpgCheckArgsSize(parameters, hasVarargs) &&
        // checks if the CallExpression matches with the parameters
        parameters.withIndex().all { (i: Int, parameter: Any?) ->
            when (parameter) {
                // if any parameter is null, signature returns false
                null -> false
                is ParamWithType ->
                    // if `parameter` is a `ParamWithType` object we want to check the type and
                    // if there is dataflow
                    cpgCheckType(parameter.type, i) &&
                        parameter.param cpgFlowsTo arguments[i]
                // checks if the type of the argument is the same
                is Type -> cpgCheckType(parameter, i)
                // check if any of the Nodes of the Op flow to the argument
                is Op -> parameter.cpgGetNodes() cpgFlowsTo arguments[i]
                // check if any of the Nodes of the DataItem flow to the argument
                is DataItem<*> -> parameter.cpgGetNodes() cpgFlowsTo arguments[i]
                // checks if there is dataflow from the parameter to the argument in the same position
                else -> parameter cpgFlowsTo arguments[i]
            }
        }
}

/** Checks the [type] against the type of the argument at [index] for the Call Expression */
private fun CallExpression.cpgCheckType(type: Type, index: Int) = arguments[index].type.typeName.endsWith(type.fqn)

/** Checks if the number of parameters matches the number of arguments of [CallExpression] */
private fun CallExpression.cpgCheckArgsSize(parameters: Array<*>, hasVarargs: Boolean) =
    if (hasVarargs) {
        parameters.size <= arguments.size
    } else {
        parameters.size == arguments.size
    }
