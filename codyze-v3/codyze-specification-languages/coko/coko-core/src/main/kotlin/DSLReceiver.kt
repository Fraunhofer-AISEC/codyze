package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core

import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.dsl.Wildcard
import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.*
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.declarations.ValueDeclaration
import de.fraunhofer.aisec.cpg.graph.evaluate
import de.fraunhofer.aisec.cpg.graph.statements.expressions.CallExpression
import de.fraunhofer.aisec.cpg.graph.statements.expressions.Expression
import de.fraunhofer.aisec.cpg.query.dataFlow
import de.fraunhofer.aisec.cpg.query.executionPath
import de.fraunhofer.aisec.cpg.query.exists

/**
 * All the functions of the dsl are implemented here. Receives a [cpg] translation result to
 * identify matching nodes and evaluate the expressions.
 */
class DSLReceiver(val cpg: TranslationResult) {
    /** For each of the nodes in [this], there is a path to at least one of the nodes in [that]. */
    infix fun Collection<Node>.follows(that: Collection<Node>): Boolean {
        return this.all { from -> that.any { to -> executionPath(from, to).value } }
    }

    /** Returns a list of [ValueDeclaration]s with the matching name. */
    fun variable(name: String): List<ValueDeclaration> {
        return cpg.allChildren { it.name == name }
    }

    /** Returns a list of [CallExpression]s with the matching [name] and fulfilling [predicate]. */
    fun call(
        name: String,
        predicate: CallExpression.() -> Boolean = { true }
    ): List<CallExpression> {
        return cpg.calls { it.name == name && predicate(it) }
    }

    /**
     * Returns a list of [CallExpression]s with the matching [fqn] (fully-qualified name) and
     * fulfilling [predicate].
     */
    fun callFqn(
        fqn: String,
        predicate: CallExpression.() -> Boolean = { true }
    ): List<CallExpression> {
        return cpg.calls { it.fqn == fqn && predicate(it) }
    }

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
                is String ->
                    Regex(this).matches((that as? Expression)?.evaluate()?.toString() ?: "")
                is Collection<*> -> this.any { it?.flowsTo(that) ?: false }
                is Node -> dataFlow(this, that).value
                else -> false
            }
        }

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
                    cpg.exists<Expression>(
                            mustSatisfy = { thisRegex.matches(it.evaluate() as String) }
                        )
                        .first
                }
                is Collection<*> -> this.any { it?.flowsTo(that) ?: false }
                is Node -> that.any { dataFlow(this, it).value }
                else -> this in that.map { (it as Expression).evaluate() }
            }
        }
}
