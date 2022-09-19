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
