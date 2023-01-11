/*
 * Copyright (c) 2022, Fraunhofer AISEC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fraunhofer.aisec.codyze.backends.cpg.coko.ordering

import de.fraunhofer.aisec.cpg.analysis.fsm.DFA
import de.fraunhofer.aisec.cpg.analysis.fsm.DFAOrderEvaluator
import de.fraunhofer.aisec.cpg.analysis.fsm.Edge
import de.fraunhofer.aisec.cpg.graph.Node
import mu.KotlinLogging

val logger = KotlinLogging.logger {}

/**
 * Codyze-specific implementation of the [DFAOrderEvaluator]. Its main purpose is to collect the
 * findings in case of violations to the order.
 */
class CodyzeDfaOrderEvaluator(
    val createFinding: (cpgNode: Node, message: String) -> Unit,
    consideredBases: Set<Node>,
    nodeToRelevantMethod: Map<Node, Set<String>>,
    thisPositionOfNode: Map<Node, Int> = mapOf(),
    eliminateUnreachableCode: Boolean = true
) : DFAOrderEvaluator(consideredBases, nodeToRelevantMethod, thisPositionOfNode, eliminateUnreachableCode) {
    private fun getPossibleNextEdges(edges: Set<Edge>?) = edges?.map { e ->
        if (e.base != null) "${e.base!!.split("$").last()}.${e.op}" else e.op
    }?.sorted()

    /**
     * Collects a finding if the [node] makes an operation which violates the desired order.
     */
    override fun actionMissingTransitionForNode(node: Node, fsm: DFA, interproceduralFlow: Boolean) {
        val possibleNextEdges = getPossibleNextEdges(fsm.currentState?.outgoingEdges)

        var message =
            "Violation against Order: \"${node.code}\". Op \"${nodeToRelevantMethod[node]}\" is not allowed. " +
                    "Expected one of: " + possibleNextEdges?.joinToString(", ")

        if (possibleNextEdges?.isEmpty() == true && fsm.currentState?.isAcceptingState == true) {
            message = "Violation against Order: \"${node.code}\". Op \"${nodeToRelevantMethod[node]}\" is not allowed. " +
                    "No other calls are allowed on this base."
        }
        createFinding(node, message)
    }

    /**
     * Collects the finding in the AnalysisContext because the DFA finished analyzing the function
     * but the [base] did not terminate in an accepting state (i.e., some operations are missing).
     */
    override fun actionNonAcceptingTermination(base: String, fsm: DFA, interproceduralFlow: Boolean) {
        if (fsm.executionTrace.size == 1) {
            return // We have not really started yet, so no output here.
        }

        val baseDeclName = base.split("|")[1].split(".").first()
        val node = fsm.executionTrace.last().cpgNode

        val possibleNextEdges = getPossibleNextEdges(fsm.currentState?.outgoingEdges)

        val defaultMessage = "Violation against Order: Base $baseDeclName is not correctly terminated. " +
                "Expected one of [${possibleNextEdges?.joinToString(", ")}] " +
                "to follow the correct last call on this base."

        createFinding(node, defaultMessage)
    }

//    /**
//     * Contains the functionality which is executed if the DFA terminated in an accepting state for
//     * the given [base]. This means that all required statements have been executed for [base] so
//     * far. The [fsm] holds the execution trace found by the analysis.
//     */
//    override fun actionAcceptingTermination(base: String, fsm: DFA, interproceduralFlow: Boolean) {
//        logger.debug("Base $base terminated in an accepting state")
//        logger.debug(
//            fsm.executionTrace.fold("") { r, t -> "$r${t.state}${t.edge} (node: ${t.cpgNode})\n" }
//        )
//    }
}
