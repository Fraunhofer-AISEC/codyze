package de.fraunhofer.aisec.codyze.analysis.resolution

import de.fraunhofer.aisec.cpg.graph.HasInitializer
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.declarations.VariableDeclaration
import de.fraunhofer.aisec.cpg.graph.statements.expressions.*
import java.util.*
import org.slf4j.LoggerFactory

/** A simple intra-procedural resolution of constant values. */
class SimpleConstantResolver : ConstantResolver {
    /**
     * Given a VariableDeclaration, this method attempts to resolve its constant value.
     *
     * Approach:
     *
     * 1. from CPG vertex representing the function argument ('crymlin.byID((long)
     * v.id()).out("ARGUMENTS").has("argumentIndex", argumentIndex)') create all paths to vertex
     * with variable declaration ('variableDeclarationVertex') in theory 'crymlin.byID((long)
     * v.id()).repeat(in("EOG").simplePath()) .until(hasId(variableDeclarationVertex.id())).path()'
     *
     * 2. traverse this path from 'v' ---> 'variableDeclarationVertex'
     *
     * 3. for each assignment, i.e. BinaryOperator{operatorCode: "="}
     *
     * 4. check if -{"LHS"}-> v -{"REFERS_TO"}-> variableDeclarationVertex
     *
     * 5. then determine value RHS
     *
     * 6. done
     *
     * 7. {no interjacent assignment} determine value of variableDeclarationVertex (e.g. from its
     * initializer)
     *
     * 8. {no initializer with value e.g. function argument} continue traversing the graph
     *
     * @param declRefExpr The DeclaredReferenceExpression that will be resolved.
     */
    override fun resolveConstantValues(
        declRefExpr: DeclaredReferenceExpression
    ): Set<ConstantValue> {
        // TODO(oxisto): refersTo is singular, so why is this a set?
        val result: MutableSet<ConstantValue> = HashSet()
        val `val` =
            resolveConstantValueOfFunctionArgument(
                declRefExpr.refersTo as? HasInitializer,
                declRefExpr
            )
        `val`.ifPresent { e: ConstantValue -> result.add(e) }
        return result
    }

    private fun resolveConstantValueOfFunctionArgument(
        declaration: HasInitializer?,
        declRefExpr: Node
    ): Optional<ConstantValue> {
        if (declaration == null) {
            return Optional.empty()
        }

        var retVal: Optional<ConstantValue> = Optional.empty()

        log.debug("Vertex for function call: {}", declRefExpr.code)
        log.debug("Vertex of variable declaration: {}", (declaration as Node).code)

        var workList = HashSet<Node>()
        val seen = HashSet<Node>()

        workList.add(declRefExpr)

        while (workList.isNotEmpty()) {
            val nextWorklist = HashSet<Node>()

            // loop through the worklist
            for (tVertex in workList) {
                if (seen.contains(tVertex)) {
                    continue
                }
                seen.add(tVertex)

                if (
                    tVertex is BinaryOperator && tVertex.operatorCode == "=" && tVertex.lhs != null
                ) {
                    val lhs = tVertex.lhs

                    // this is an assignment that may set the value of our operand
                    if (lhs is DeclaredReferenceExpression && lhs.refersTo == declaration) {
                        log.debug(
                            "   LHS of this node is interesting. Will evaluate RHS: {}",
                            tVertex.code
                        )

                        val rhs = tVertex.rhs

                        if (rhs is Literal<*>) {
                            val literalValue = rhs.value
                            val constantValue = ConstantValue.tryOf(literalValue)
                            if (constantValue.isPresent) {
                                return constantValue
                            }
                            log.warn(
                                "Unknown literal type encountered: {} (value: {})",
                                literalValue!!.javaClass,
                                literalValue
                            )
                        } else if (rhs is ExpressionList && rhs.prevEOG.isNotEmpty()) {
                            // C/C++ assigns last expression in list.
                            val lastExpressionInList = rhs.prevEOG.first()
                            if (lastExpressionInList is Literal<*>) {
                                // If last expression is Literal --> assign its value immediately.
                                val literalValue = lastExpressionInList.value
                                val constantValue = ConstantValue.tryOf(literalValue)
                                if (constantValue.isPresent) {
                                    return constantValue
                                }
                                log.warn(
                                    "Unknown literal type encountered: {} (value: {})",
                                    literalValue!!.javaClass,
                                    literalValue
                                )
                            } else if (lastExpressionInList is DeclaredReferenceExpression) {
                                // Get declaration of the variable used as last item in expression
                                // list
                                val refersTo = lastExpressionInList.prevDFG.iterator()
                                if (refersTo.hasNext()) {
                                    val v = refersTo.next()
                                    if (v is VariableDeclaration) {
                                        val resolver = SimpleConstantResolver()
                                        val constantValue =
                                            resolver.resolveConstantValueOfFunctionArgument(
                                                v,
                                                lastExpressionInList
                                            )
                                        if (constantValue.isPresent) {
                                            return constantValue
                                        }
                                    } else if (v is Literal<*>) {
                                        val literalValue = v.value
                                        val constantValue = ConstantValue.tryOf(literalValue)
                                        if (constantValue.isPresent) {
                                            return constantValue
                                        }
                                    } else {
                                        log.warn(
                                            "Last expression in ExpressionList does not have a VariableDeclaration. Cannot resolve its value: {}",
                                            lastExpressionInList.code
                                        )
                                    }
                                } else {
                                    log.warn(
                                        "Last expression in ExpressionList has no incoming DFG. Cannot resolve its value: {}",
                                        lastExpressionInList.code
                                    )
                                }
                            }
                        }
                        log.error("Value of operand set in assignment expression")
                        return Optional.empty()
                    }
                }
                if (tVertex !== declaration) { // stop once we are at the declaration
                    for (vertex in tVertex.prevEOG) {
                        if (!seen.contains(vertex)) {
                            nextWorklist.add(vertex)
                        }
                    }
                }
            }
            workList = nextWorklist
        }

        // we arrived at the declaration of the variable. See if we have an initializer
        declaration.initializer?.let {
            // TODO(oxisto): refarctor this to avoid copy/paste and handle NewExpression (forward to
            // initalizer)
            if (it is Literal<*>) {
                retVal = ConstantValue.tryOf(it.value)
            } else if (it is ConstructExpression) {
                var args = it.arguments
                if (args.size == 1) {
                    val init = args.first()
                    if (init is Literal<*>) {
                        retVal = ConstantValue.tryOf(init.value)
                    } else {
                        log.warn(
                            "Cannot evaluate ConstructExpression, it is a {}",
                            init.javaClass.simpleName
                        )
                    }
                } else if (args.isEmpty()) {
                    log.warn("No Argument to ConstructExpression")
                } else {
                    log.warn(
                        "More than one Arguments to ConstructExpression found, not using one of them."
                    )
                    retVal = Optional.empty()
                }
            } else if (it is InitializerListExpression) {
                val initializers = it.initializers.iterator()
                if (initializers.hasNext()) {
                    val init = initializers.next()
                    if (init is Literal<*>) {
                        val initValue = init.value
                        retVal = ConstantValue.tryOf(initValue)
                    } else {
                        log.warn(
                            "Cannot evaluate initializer, it is a {}",
                            init.javaClass.simpleName
                        )
                    }
                } else {
                    log.warn("No initializer found")
                }
                if (initializers.hasNext()) {
                    log.warn("More than one initializer found, using none of them")
                    retVal = Optional.empty()
                }
            } else if (it is ExpressionList) {
                // get the last initializer according to C++17 standard
                val init = it.expressions.last()
                if (init != null) {
                    if (init is Literal<*>) {
                        retVal = ConstantValue.tryOf(init.value)
                    } else {
                        log.warn(
                            "Cannot evaluate initializer, it is a {}",
                            init.javaClass.simpleName
                        )
                    }
                } else {
                    log.warn("No initializer found")
                }
            } else {
                log.warn("Unknown initializer: {}", it.javaClass.simpleName)
            }
        }

        return retVal
    }

    companion object {
        private val log = LoggerFactory.getLogger(SimpleConstantResolver::class.java)
    }
}
