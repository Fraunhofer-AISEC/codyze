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
/**
 * Checks if the arguments of [CallExpression] have the same type and order as specified in [types].
 *
 * @param types specifies the order and types of the parameters of the function
 */
fun signature(vararg types: Type): Boolean {
    return types.size == arguments.size &&
        types
            .mapIndexed { i: Int, type: Type -> type.fqn == arguments[i].type.typeName }
            // check if all are `true`
            .all { it }
}

context(CallExpression)
// TODO: better description
/**
 * Checks if the [CallExpression] matches the signature specified with [parameters].
 *
 * @param parameters specifies the order of the parameters of the function
 */
fun signature(vararg parameters: Any?): Boolean {
    // filters out the signature if any parameter is null
    if (parameters.contains(null)) return false

    // we already checked that the array does not contain nulls but Kotlin does not realize that
    val notNullParams = parameters.filterNotNull()

    return if (notNullParams.all { it is Type }) {
        // There are situations where all parameters are `Type` objects but cast to `Object`
        // so `signature(varargs types: Type)` is not called.
        // This checks if `signature(varargs types: Type)` should be called instead.
        signature(*notNullParams.map { it as Type }.toTypedArray())
    } else {
        // checks if amount of parameters is the same as amount of arguments of this CallExpression
        notNullParams.size == arguments.size &&
            // checks if there is dataflow from all parameters to the arguments in the correct
            // position
            notNullParams.foldIndexed(true) { i: Int, acc: Boolean, any: Any ->
                acc && any flowsTo arguments[i]
            }
    }
}

context(CallExpression)
/**
 * Checks if [CallExpression] has no arguments
 *
 * Needed because of resolution ambiguity
 */
fun signature(): Boolean {
    return arguments.isEmpty()
}
