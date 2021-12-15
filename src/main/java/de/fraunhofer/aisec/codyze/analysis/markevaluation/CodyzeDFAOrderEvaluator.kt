package de.fraunhofer.aisec.codyze.analysis.markevaluation

import de.fraunhofer.aisec.codyze.analysis.AnalysisContext
import de.fraunhofer.aisec.codyze.analysis.Finding
import de.fraunhofer.aisec.codyze.analysis.MarkContextHolder
import de.fraunhofer.aisec.codyze.analysis.markevaluation.Evaluator.log
import de.fraunhofer.aisec.codyze.analysis.utils.Utils
import de.fraunhofer.aisec.codyze.markmodel.MRule
import de.fraunhofer.aisec.cpg.analysis.fsm.DFA
import de.fraunhofer.aisec.cpg.analysis.fsm.DFAOrderEvaluator
import de.fraunhofer.aisec.cpg.graph.Node
import java.io.File

class CodyzeDFAOrderEvaluator(
    referencedVertices: Set<Long?>,
    nodesToOp: Map<Node, String>,
    thisPositionOfNode: Map<Node, Int>,
    val rule: MRule,
    val markContextHolder: MarkContextHolder,
    val ctx: AnalysisContext
) : DFAOrderEvaluator(referencedVertices, nodesToOp, thisPositionOfNode) {

    override fun actionMissingTransitionForNode(node: Node, fsm: DFA?) {
        val region = Utils.getRegionByNode(node)
        val outgoing = mutableListOf<String>()
        if (fsm?.currentState?.isAcceptingState == true) {
            outgoing.add("END")
        }

        val possibleNextEdges =
            fsm?.currentState?.outgoingEdges?.map { e ->
                if (e.base != null) "${e.base}.${e.op}" else e.op
            }
        if (possibleNextEdges != null) {
            // We are in an accepting state, so doing nothing should be an option
            outgoing.addAll(possibleNextEdges.sorted())
        }

        val f =
            Finding(
                if (rule.errorMessage != null) rule.errorMessage else rule.name,
                rule.statement.action,
                "Violation against Order: ${node.code} (${nodesToOp[node]}) is not allowed. Expected one of: " +
                    outgoing.sorted().joinToString(", ") +
                    " (${rule.errorMessage})",
                File(node.file!!).toURI(),
                region.startLine,
                region.endLine,
                region.startColumn,
                region.endColumn
            )
        if (markContextHolder.isCreateFindingsDuringEvaluation) {
            ctx.findings.add(f)
        }
        log.info("Finding: {}", f)
    }

    override fun actionNonAcceptingTermination(base: String, fsm: DFA) {
        if (fsm.executionTrace.size == 1)
            return // We have not really started yet, so no output here.

        val baseDeclName = base.split("|")[1].split(".")[0]
        val node = fsm.executionTrace.last().second
        val region = Utils.getRegionByNode(node)

        val possibleNextEdges =
            fsm.currentState
                ?.outgoingEdges
                ?.map { e -> if (e.base != null) "${e.base}.${e.op}" else e.op }
                ?.sorted()

        val f =
            Finding(
                if (rule.errorMessage != null) rule.errorMessage else rule.name,
                rule.statement.action,
                "Violation against Order: Base $baseDeclName is not correctly terminated. Expected one of [" +
                    possibleNextEdges?.joinToString(", ") +
                    "] to follow the correct last call on this base. (${rule.errorMessage})",
                File(node.file!!).toURI(),
                region.startLine,
                region.endLine,
                region.startColumn,
                region.endColumn
            )
        if (markContextHolder.isCreateFindingsDuringEvaluation) {
            ctx.findings.add(f)
        }
        log.info("Finding: {}", f)
    }
}
