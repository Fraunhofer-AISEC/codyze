package de.fraunhofer.aisec.codyze.backends.cpg.coko.dsl

import de.fraunhofer.aisec.codyze.backends.cpg.coko.Nodes
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.CokoBackend
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.modelling.DataItem
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.modelling.ReturnValueItem
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.modelling.Value
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.declarations.VariableDeclaration
import de.fraunhofer.aisec.cpg.graph.evaluate
import de.fraunhofer.aisec.cpg.graph.literals
import de.fraunhofer.aisec.cpg.graph.statements.expressions.DeclaredReferenceExpression
import de.fraunhofer.aisec.cpg.graph.variables

/**
 * Get all [Nodes] that are associated with this [DataItem].
 */
context(CokoBackend)
fun DataItem.cpgGetAllNodes(): Nodes =
    when (this@DataItem) {
        is ReturnValueItem -> op.cpgGetAllNodes().flatMap { it.getVariableInNextDFGOrThis() }
        is Value -> this@DataItem.getNodes()
    }

/**
 * Get all [Nodes] that are associated with this [DataItem].
 */
context(CokoBackend)
fun DataItem.cpgGetNodes(): Nodes {
    return when (this@DataItem) {
        is ReturnValueItem -> op.cpgGetNodes().flatMap { it.getVariableInNextDFGOrThis() }
        is Value -> this@DataItem.getNodes()
    }
}

context(CokoBackend)
private fun Value.getNodes(): Nodes =
    cpg.literals.filter {
            node ->
        node.value == this.value
    } + cpg.variables.filter {
            node ->
        node.evaluate() == this.value
    }

/**
 * Returns all [VariableDeclaration]s and [DeclaredReferenceExpression]s that have a DFG edge from [this].
 * If there are none, returns [this].
 */
private fun Node.getVariableInNextDFGOrThis(): Nodes =
    this.nextDFG
        .filter {
                next ->
            next is DeclaredReferenceExpression || next is VariableDeclaration
        }.ifEmpty { listOf(this) }
