package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core

import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.declarations.FieldDeclaration
import de.fraunhofer.aisec.cpg.graph.evaluate
import de.fraunhofer.aisec.cpg.graph.statements.expressions.CallExpression
import de.fraunhofer.aisec.cpg.graph.statements.expressions.Expression
import de.fraunhofer.aisec.cpg.helpers.SubgraphWalker
import kotlin.reflect.KCallable
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class CPGEvaluator(val cpg: TranslationResult) {
    fun variable(name: String): List<Node> {
        return SubgraphWalker.flattenAST(cpg).filterIsInstance<FieldDeclaration>()
    }

    fun call(name: String, predicate: CallExpression.() -> Boolean): List<CallExpression> {
        // TODO: CPGv5
        // return cpg.callsByName(name).filter(predicate)
        return SubgraphWalker.flattenAST(cpg).filter { node ->
            (node as? CallExpression)?.invokes?.any { it.name == name } == true && predicate(node)
        } as List<CallExpression>
    }

    fun callFqn(fqn: String, predicate: CallExpression.() -> Boolean): List<CallExpression> {
        // TODO: CPGv5
        // return cpg.calls.filter { it.fqn == fqn && predicate(it) }
        return SubgraphWalker.flattenAST(cpg).filter { node ->
            (node as? CallExpression)?.fqn == fqn && predicate(node)
        } as List<CallExpression>
    }

    infix fun Any.flowsTo(that: Node): Boolean =
        if (this is Wildcard) {
            true
        } else {
            when (this) {
                is String ->
                    Regex(this).matches((that as? Expression)?.evaluate()?.toString() ?: "")
                // TODO: CPGv5
                // is Node -> dataFlow(this, that).value
                else -> false
            }
        }

    infix fun Any.flowsTo(that: Collection<Node>): Boolean =
        if (this is Wildcard) {
            true
        } else {
            when (this) {
                is String -> {
                    val thisRegex = Regex(this)
                    // TODO: For cpgv5
                    // cpg.exists<Expression>(mustSatisfy = {thisRegex.matches(it.evaluate() as
                    // String)})
                    that
                        .map { (it as? Expression)?.evaluate()?.toString() ?: "" }
                        .any { thisRegex.matches(it) }
                }
                // TODO: CPGv5
                // is Node -> that.any { dataFlow(this, it).value }
                else -> this in that.map { (it as Expression).evaluate() }
            }
        }
}
