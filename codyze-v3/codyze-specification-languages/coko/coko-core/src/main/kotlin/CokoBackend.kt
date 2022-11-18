package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core

import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.dsl.Op
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.dsl.Order
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.modelling.Definition
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.modelling.Signature
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.ordering.OrderToken
import de.fraunhofer.aisec.codyze_core.wrapper.Backend

typealias Nodes = Collection<Any>

@DslMarker
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.CLASS,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.TYPEALIAS,
    AnnotationTarget.TYPE
)
annotation class CokoMarker

@Suppress("UNUSED")
@CokoMarker
/*
 * Receives a [cpg] translation result to identify matching nodes and evaluate the expressions.
 * All the functionality of the DSL are implemented as extension functions on [CokoBackend].
 */
interface CokoBackend : Backend {
    /** Get all [Nodes] that are associated with this [Op]. */
    fun Op.getAllNodes(): Nodes

    /**
     * Get all [Nodes] that are associated with this [Op] and fulfill the [Signature]s of the
     * [Definition]s.
     */
    fun Op.getNodes(): Nodes

    /** For each of the nodes in [this], there is a path to at least one of the nodes in [that]. */
    infix fun Op.follows(that: Op): Evaluator

    /* Ensures the order of nodes as specified in the user configured [Order] object */
    fun order(baseNodes: OrderToken? = null, block: Order.() -> Unit): Evaluator  // TODO: allow OrderFragment in baseNodes to allow the user to use '.use {}'

    /** Ensures that all calls to the [ops] have arguments that fit the parameters specified in [ops] */
    fun only(vararg ops: Op): Evaluator
}
