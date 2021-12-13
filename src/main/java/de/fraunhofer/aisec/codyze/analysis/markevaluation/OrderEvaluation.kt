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

/**
 * This is mostly copy and paste from [OrderNFAEvaluator].
 * Should start the analysis.
 */
class OrderEvaluation {
    private val log = LoggerFactory.getLogger(OrderEvaluation::class.java)

    fun evaluate(rule: MRule, markContextHolder: MarkContextHolder, orderExpression: OrderExpression, contextID: Int, ctx: AnalysisContext): ConstantValue {
        val instanceContext = markContextHolder.getContext(contextID).instanceContext

        // extract all used markvars from the expression
        val markInstances = mutableSetOf<String>()
        ExpressionHelper.collectMarkInstances(orderExpression.exp, markInstances)

        if (markInstances.size > 1) {
            log.warn("Order statement contains more than one base. Not supported.")
            return ErrorValue.newErrorValue("Order statement contains more than one base. Not supported.")
        }
        if (markInstances.isEmpty()) {
            log.warn("Order statement does not contain any ops. Invalid order")
            return ErrorValue.newErrorValue("Order statement does not contain any ops. Invalid order")
        }

        val markVar = markInstances.first()

        val variableDecl = instanceContext.getNode(markVar)
        if (variableDecl == null) {
            log.warn("No instance for markvar $markVar set in the instancecontext. Invalid evaluation.")
            return ErrorValue.newErrorValue("No instance for markvar $markVar set in the instancecontext. Invalid evaluation.")
        }

        // collect all instances used in this order
        val entityReferences = mutableSetOf<String>()
        ExpressionHelper.collectMarkInstances(orderExpression.exp, entityReferences)

        val referencedNodes = mutableSetOf<Long?>()
        for (alias in entityReferences) {
            val node = instanceContext.getNode(alias)
            if (node == null) {
                log.error("Alias $alias is not referenced in this rule ${rule.name}")
                return ErrorValue.newErrorValue("Alias $alias is not referenced in this rule ${rule.name}")
            }
            referencedNodes.add(node.id)
        }

        val dfa = FSMBuilder().sequenceToDFA(orderExpression)

        // Cache which node belongs to which Op/Entity.
        // A node can _only_ belong to one entity/op!
        val nodesToOp = mutableMapOf<Node, String>()
        for ((_, value) in rule.entityReferences) {
            val ent = value.second ?: continue
            for (op in ent.ops) {
                op.allNodes.forEach { v: Node -> nodesToOp[v] = op.name }
            }
        }
        if (nodesToOp.isEmpty()) {
            log.info("no nodes match this rule. Skipping rule.")
            return ErrorValue.newErrorValue("no nodes match this rule. Skipping rule.")
        }

        val thisPositionOfNode: Map<Node, Int> = mapOf()

        val dfaEvaluator = CodyzeDFAOrderEvaluator(referencedNodes, nodesToOp, thisPositionOfNode, rule, markContextHolder, ctx)
        val isOrderValid = dfaEvaluator.evaluateOrder(dfa, variableDecl)
        val of = ConstantValue.of(isOrderValid)
        if (markContextHolder.isCreateFindingsDuringEvaluation) {
            markContextHolder.getContext(contextID).isFindingAlreadyAdded = true
        }
        return of
    }
}