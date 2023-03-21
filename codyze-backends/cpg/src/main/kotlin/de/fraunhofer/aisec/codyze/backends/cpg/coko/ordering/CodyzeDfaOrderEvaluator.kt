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

import de.fraunhofer.aisec.codyze.backends.cpg.coko.CpgFinding
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.EvaluationContext
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.Finding
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.Rule
import de.fraunhofer.aisec.cpg.analysis.fsm.DFA
import de.fraunhofer.aisec.cpg.analysis.fsm.DFAOrderEvaluator
import de.fraunhofer.aisec.cpg.analysis.fsm.Edge
import de.fraunhofer.aisec.cpg.graph.Node
import mu.KotlinLogging
import kotlin.reflect.full.findAnnotation

val logger = KotlinLogging.logger {}

/**
 * Codyze-specific implementation of the [DFAOrderEvaluator]. Its main purpose is to collect the
 * findings in case of violations to the order.
 */
@Suppress("LongParameterList")
class CodyzeDfaOrderEvaluator(
    val context: EvaluationContext,
    dfa: DFA,
    consideredBases: Set<Node>,
    nodeToRelevantMethod: Map<Node, Set<String>>,
    thisPositionOfNode: Map<Node, Int> = mapOf(), // for non-object oriented languages
    consideredResetNodes: Set<Node>,
    eliminateUnreachableCode: Boolean = true
) : DFAOrderEvaluator(
    dfa,
    consideredBases,
    nodeToRelevantMethod,
    consideredResetNodes,
    thisPositionOfNode,
    eliminateUnreachableCode
) {
    val findings: MutableSet<CpgFinding> = mutableSetOf()
    private val userDefinedFailMessage = context.rule.findAnnotation<Rule>()?.failMessage
        .takeIf { it?.isEmpty() == false }
    private val userDefinedPassMessage = context.rule.findAnnotation<Rule>()?.passMessage
        .takeIf { it?.isEmpty() == false }

    @Suppress("UnsafeCallOnNullableType")
    private fun getPossibleNextEdges(edges: Set<Edge>?) = edges?.map { it.op }?.sorted()

    /**
     * Collects a finding if the [node] makes an operation which violates the desired order.
     */
    override fun actionMissingTransitionForNode(node: Node, fsm: DFA, interproceduralFlow: Boolean) {
        val possibleNextEdges = getPossibleNextEdges(fsm.currentState?.outgoingEdges)

        var defaultMessage =
            "\"${node.code}\". Op \"${nodeToRelevantMethod[node]}\" is not allowed. " +
                "Expected one of: " + possibleNextEdges?.joinToString(", ")

        if (possibleNextEdges?.isEmpty() == true && fsm.currentState?.isAcceptingState == true) {
            defaultMessage = "\"${node.code}\". " +
                "Op \"${nodeToRelevantMethod[node]}\" is not allowed. No other calls are allowed on this base."
        }

        val message = userDefinedFailMessage ?: defaultMessage
        findings.add(
            CpgFinding(
                kind = Finding.Kind.Fail,
                node = node,
                message = "Violation against Order: $message",
                relatedNodes = fsm.executionTrace.map { it.cpgNode }.filter { it != node }
            )
        )
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

        val defaultMessage = "Base $baseDeclName is not correctly terminated. " +
            "Expected one of [${possibleNextEdges?.joinToString(", ")}] " +
            "to follow the correct last call on this base."

        val message = userDefinedFailMessage ?: defaultMessage

        findings.add(
            CpgFinding(
                kind = Finding.Kind.Fail,
                node = node,
                message = "Violation against Order: $message",
                relatedNodes = fsm.executionTrace.map { it.cpgNode }.filter { it != node }
            )
        )
    }

    /**
     * Contains the functionality which is executed if the DFA terminated in an accepting state for
     * the given [base]. This means that all required statements have been executed for [base] so
     * far. The [fsm] holds the execution trace found by the analysis.
     */
    override fun actionAcceptingTermination(base: String, fsm: DFA, interproceduralFlow: Boolean) {
        val baseDeclName = base.split("|")[1].split(".").first()
        val node = fsm.executionTrace.last().cpgNode

        val defaultMessage = "$baseDeclName is used correctly."
        val message = userDefinedPassMessage ?: defaultMessage

        findings.add(
            CpgFinding(
                kind = Finding.Kind.Pass,
                node = node,
                message = "Order validated: $message",
                relatedNodes = fsm.executionTrace.map { it.cpgNode }.filter { it != node }
            )
        )
    }
}
