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
package de.fraunhofer.aisec.codyze.backends.cpg.coko.evaluators

import de.fraunhofer.aisec.codyze.backends.cpg.coko.CokoCpgBackend
import de.fraunhofer.aisec.codyze.backends.cpg.coko.CpgFinding
import de.fraunhofer.aisec.codyze.backends.cpg.coko.dsl.Result
import de.fraunhofer.aisec.codyze.backends.cpg.coko.dsl.cpgGetAllNodes
import de.fraunhofer.aisec.codyze.backends.cpg.coko.dsl.cpgGetNodes
import de.fraunhofer.aisec.codyze.backends.cpg.coko.dsl.findUsages
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.EvaluationContext
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.Evaluator
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.Finding
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.Op
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.Rule
import de.fraunhofer.aisec.cpg.graph.statements.expressions.CallExpression
import kotlin.reflect.full.findAnnotation

context(CokoCpgBackend)
class OnlyNeverEvaluator(private val ops: List<Op>, private val functionality: Functionality) : Evaluator {

    var interestingNodes = with(this@CokoCpgBackend) {
        ops.flatMap { it.cpgGetNodes().entries }
    }.associate { it.toPair() }

    /** Default message if a violation is found */
    private val defaultFailMessage: String by lazy {
        // Try to model what the allowed calls look like with `toString` call of `Op`
        "${if (functionality == Functionality.NEVER) "No" else "Only"} calls to ${ops.joinToString()} allowed."
    }

    /** Default message if node complies with rule */
    private val defaultPassMessage = "Call is in compliance with rule"

    override fun evaluate(context: EvaluationContext): List<CpgFinding> {
        val (violatingNodes, correctAndOpenNodes) = getNodes()
        val (failMessage, passMessage) = getMessages(context)
        return createFindings(violatingNodes, correctAndOpenNodes, failMessage, passMessage)
    }

    private fun getNodes(): Pair<Set<CallExpression>, Set<CallExpression>> {
        val distinctOps = ops.toSet()
        val allNodes =
            with(this@CokoCpgBackend) { distinctOps.flatMap { it.cpgGetAllNodes() } }
                .filter { it.location != null }
                .toSet()

        // `matchingNodes` is a subset of `allNodes`
        // we want to find nodes in `allNodes` that are not contained in `matchingNodes`
        // since they are contrary Findings
        val matchingNodes = interestingNodes.keys.toSet()
        val differingNodes = allNodes.minus(matchingNodes)

        // define what violations and passes are, depending on selected functionality
        val correctAndOpenNodes = if (functionality == Functionality.NEVER) differingNodes else matchingNodes
        val violatingNodes = if (functionality == Functionality.NEVER) matchingNodes else differingNodes

        return violatingNodes to correctAndOpenNodes
    }

    private fun getMessages(context: EvaluationContext): Pair<String, String> {
        val ruleAnnotation = context.rule.findAnnotation<Rule>()
        val failMessage = ruleAnnotation?.failMessage?.takeIf { it.isNotEmpty() } ?: defaultFailMessage
        val passMessage = ruleAnnotation?.passMessage?.takeIf { it.isNotEmpty() } ?: defaultPassMessage
        return failMessage to passMessage
    }

    fun createFindings(
        violatingNodes: Set<CallExpression>,
        correctAndOpenNodes: Set<CallExpression>,
        failMessage: String,
        passMessage: String
    ): List<CpgFinding> {
        val findings = mutableListOf<CpgFinding>()

        for (node in violatingNodes) {
            findings.add(
                CpgFinding(
                    message = "Violation against rule: \"${node.code}\". $failMessage",
                    kind = Finding.Kind.Fail,
                    node = node,
                    relatedNodes = node.findUsages()
                )
            )
        }

        for (node in correctAndOpenNodes) {
            if (interestingNodes[node] == Result.OPEN) {
                findings.add(
                    CpgFinding(
                        message = "Not enough information to evaluate \"${node.code}\"",
                        kind = Finding.Kind.Open,
                        node = node,
                        relatedNodes = node.findUsages()
                    )
                )
            } else {
                findings.add(
                    CpgFinding(
                        message = "Complies with rule: \"${node.code}\". $passMessage",
                        kind = Finding.Kind.Pass,
                        node = node,
                        relatedNodes = node.findUsages()
                    )
                )
            }
        }
        return findings
    }

    enum class Functionality {
        ONLY,
        NEVER
    }
}
