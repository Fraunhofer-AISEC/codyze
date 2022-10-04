@file:Suppress("UNUSED")

package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.dsl

import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.CokoMarker
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.Project
import de.fraunhofer.aisec.cpg.graph.*
import de.fraunhofer.aisec.cpg.graph.declarations.ValueDeclaration
import de.fraunhofer.aisec.cpg.graph.statements.expressions.CallExpression
import de.fraunhofer.aisec.cpg.graph.statements.expressions.Expression
import de.fraunhofer.aisec.cpg.graph.statements.expressions.MemberExpression
import de.fraunhofer.aisec.cpg.query.dataFlow
import de.fraunhofer.aisec.cpg.query.exists

/** Returns a list of [ValueDeclaration]s with the matching name. */
fun Project.variable(name: String): List<ValueDeclaration> {
    return cpg.allChildren { it.name == name }
}

/** Returns a list of [CallExpression]s with the matching [name] and fulfilling [predicate]. */
fun Project.call(
    name: String,
    predicate: (@CokoMarker CallExpression).() -> Boolean = { true }
): List<CallExpression> {
    return cpg.calls { it.name == name && predicate(it) }
}

/**
 * Returns a list of [CallExpression]s with the matching [fqn] (fully-qualified name) and fulfilling
 * [predicate].
 */
fun Project.callFqn(
    fqn: String,
    predicate: (@CokoMarker CallExpression).() -> Boolean = { true }
): List<CallExpression> {
    return cpg.calls { it.fqn == fqn && predicate(it) }
}

/** Returns a list of [MemberExpression]s with the matching something. */
fun Project.memberExpr(
    predicate: (@CokoMarker MemberExpression).() -> Boolean
): List<MemberExpression> {
    return cpg.allChildren(predicate)
}

context(
    CallExpression
) // this extension function should only be available in the context of a CallExpression
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
            is String -> Regex(this).matches((that as? Expression)?.evaluate()?.toString() ?: "")
            is Collection<*> -> this.any { it?.flowsTo(that) ?: false }
            is Node -> dataFlow(this, that).value
            else -> false
        }
    }

context(
    Project,
    CallExpression
) // this extension function needs Project as a context + it should only be available in the context
// of a CallExpression
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
        when (this) {
            is String -> {
                val thisRegex = Regex(this)
                cpg.exists<Expression>(mustSatisfy = { thisRegex.matches(it.evaluate() as String) })
                    .first
            }
            is Collection<*> -> this.any { it?.flowsTo(that) ?: false }
            is Node -> that.any { dataFlow(this, it).value }
            else -> this in that.map { (it as Expression).evaluate() }
        }
    }

context(CallExpression)
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
 * If a parameter is a [Pair]< [Any], [Type]> object, the function will check if the argument has
 * the same type and if the [Any] object flows to the argument
 *
 * @param hasVarargs specifies if the function has a variable number of arguments at the end which
 * are not important to the analysis
 */
fun signature(vararg parameters: Any?, hasVarargs: Boolean = false): Boolean {
    // filter out all null in `parameters`
    val notNullParams = parameters.filterNotNull()

    // filters out the signature if any parameter is null
    return notNullParams.size == parameters.size &&
        // checks if amount of parameters is the same as amount of arguments of this CallExpression
        checkArgsSize(parameters, hasVarargs) &&
        notNullParams.foldIndexed(true) { i: Int, acc: Boolean, parameter: Any ->
            acc &&
                when (parameter) {
                    is Pair<*, *> ->
                        // if `parameter` is a `Pair<Any,Type>` object we want to check the type and
                        // if there is dataflow
                        if (parameter.second is Type)
                            checkType(parameter.second as Type, i) &&
                                parameter.first != null &&
                                parameter.first!! flowsTo arguments[i]
                        else parameter flowsTo arguments[i]
                    // checks if the type of the argument is the same
                    is Type -> checkType(parameter, i)
                    // checks if there is dataflow from the parameter to the argument in the same
                    // position
                    else -> parameter flowsTo arguments[i]
                }
        }
}

context(CallExpression)
/** Checks the [type] against the type of the argument at [index] for the Call Expression */
private fun checkType(type: Type, index: Int): Boolean {
    return type.fqn == arguments[index].type.typeName
}

context(CallExpression)
/** Checks if the number of parameters matches the number of arguments of [CallExpression] */
private fun checkArgsSize(parameters: Array<*>, hasVarargs: Boolean): Boolean {
    return if (hasVarargs) parameters.size <= arguments.size else parameters.size == arguments.size
}