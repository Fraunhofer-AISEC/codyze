package de.fraunhofer.aisec.codyze.analysis.markevaluation

import de.fraunhofer.aisec.codyze.analysis.AnalysisContext
import de.fraunhofer.aisec.codyze.analysis.ErrorValue
import de.fraunhofer.aisec.codyze.analysis.MarkContextHolder
import de.fraunhofer.aisec.codyze.analysis.resolution.ConstantValue
import de.fraunhofer.aisec.codyze.markmodel.MRule
import de.fraunhofer.aisec.cpg.analysis.fsm.FSMBuilder
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.mark.markDsl.OrderExpression
import org.slf4j.LoggerFactory
import java.util.stream.IntStream

/** This class starts the order evaluation based on the [CodyzeDFAOrderEvaluator]. */
class OrderEvaluation {
    private val log = LoggerFactory.getLogger(OrderEvaluation::class.java)

    /**
     * Collects the mark instances of the [orderExpression], maps CPG nodes to the string
     * representation of the MARK-Operators and starts the order evaluation.
     */
    fun evaluateDFA(
        rule: MRule,
        markContextHolder: MarkContextHolder,
        orderExpression: OrderExpression,
        contextID: Int,
        ctx: AnalysisContext
    ): ConstantValue {
        // extract all used markvars from the expression
        val markInstances = mutableSetOf<String>()
        ExpressionHelper.collectMarkInstances(orderExpression.exp, markInstances)

        if (markInstances.size > 1) {
            log.warn("Order statement contains more than one base. Not supported.")
            return ErrorValue.newErrorValue(
                "Order statement contains more than one base. Not supported."
            )
        }
        if (markInstances.isEmpty()) {
            log.warn("Order statement does not contain any ops. Invalid order.")
            return ErrorValue.newErrorValue(
                "Order statement does not contain any ops. Invalid order."
            )
        }

        val markVar = markInstances.first()

        val instanceContext = markContextHolder.getContext(contextID).instanceContext
        val variableDecl = instanceContext.getNode(markVar)
        if (variableDecl == null) {
            log.warn(
                "No instance for markvar $markVar set in the instance context. Invalid evaluation."
            )
            return ErrorValue.newErrorValue(
                "No instance for markvar $markVar set in the instance context. Invalid evaluation."
            )
        }

        // collect all instances used in this order
        val entityReferences = mutableSetOf<String>()
        ExpressionHelper.collectMarkInstances(orderExpression.exp, entityReferences)

        val referencedNodes = mutableSetOf<Long>()
        for (alias in entityReferences) {
            val node = instanceContext.getNode(alias)
            if (node == null) {
                log.error("Alias $alias is not referenced in this rule ${rule.name}")
                return ErrorValue.newErrorValue(
                    "Alias $alias is not referenced in this rule ${rule.name}"
                )
            }

            val id = node.id

            if (id == null) {
                return ErrorValue.newErrorValue("Node $node does not have an identifier")
            } else {
                referencedNodes.add(id)
            }
        }

        val dfa = FSMBuilder().sequenceToDFA(orderExpression)

        // Cache which node belongs to which Op/Entity.
        // A node can _only_ belong to one entity/op!
        val nodesToOp = mutableMapOf<Node, String>()
        // Check if we need to identify the "this position" for the node.
        val thisPositionOfNode = mutableMapOf<Node, Int>()
        for ((_, value) in rule.entityReferences) {
            val ent = value.second ?: continue
            for (op in ent.ops) {
                for (v in op.allNodes) {
                    nodesToOp[v] = op.name
                    val opstmt = op.nodesToStatements[v]
                    if (opstmt?.size == 1) {
                        val params = opstmt.iterator().next().call.params
                        val thisPositions =
                            IntStream.range(0, params.size)
                                .filter { i: Int -> "this" == params[i].getVar() }
                                .toArray()
                        if (thisPositions.size == 1) {
                            thisPositionOfNode[v] = thisPositions[0]
                        }
                    }
                }
            }
        }
        if (nodesToOp.isEmpty()) {
            log.info("No nodes match this rule. Skipping rule.")
            return ErrorValue.newErrorValue("No nodes match this rule. Skipping rule.")
        }

        val dfaEvaluator =
            CodyzeDFAOrderEvaluator(
                referencedNodes,
                nodesToOp,
                thisPositionOfNode,
                rule,
                markContextHolder,
                ctx
            )
        val isOrderValid = dfaEvaluator.evaluateOrder(dfa, variableDecl.containingFunction!!)

        val of = ConstantValue.of(isOrderValid)
        if (markContextHolder.isCreateFindingsDuringEvaluation) {
            markContextHolder.getContext(contextID).isFindingAlreadyAdded = true
        }
        return of
    }
}
