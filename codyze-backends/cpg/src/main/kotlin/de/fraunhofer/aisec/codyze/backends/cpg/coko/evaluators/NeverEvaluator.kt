/*
 * Copyright (c) 2023, Fraunhofer AISEC. All rights reserved.
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
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.EvaluationContext
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.Evaluator
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.Finding
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.Op
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.Rule
import de.fraunhofer.aisec.cpg.graph.statements.expressions.CallExpression
import kotlin.reflect.full.findAnnotation

context(CokoCpgBackend)
class NeverEvaluator(private val forbiddenOps: List<Op>) : Evaluator {
    var violating = with(this@CokoCpgBackend) {
        forbiddenOps.flatMap { it.cpgGetNodes().entries }.associate { it.toPair() }
    }

    /** Default message if a violation is found */
    private val defaultFailMessage: String by lazy {
        "Calls to ${forbiddenOps.joinToString()} are not allowed."
    }

    /** Default message if the analyzed code complies with rule */
    private val defaultPassMessage by lazy {
        "No calls to ${forbiddenOps.joinToString()} found which is in compliance with rule."
    }

    override fun evaluate(context: EvaluationContext): Collection<Finding> {
        val (violatingNodes, correctAndOpenNodes) = getNodes()
        val (failMessage, passMessage) = getMessages(context)
        return createFindings(violatingNodes, correctAndOpenNodes, failMessage, passMessage)
    }

    private fun getNodes(): Pair<Set<CallExpression>, Set<CallExpression>> {
        val violatingNodes = violating.keys.toSet()

        val distinctOps = forbiddenOps.toSet()
        val allNodes =
            with(this@CokoCpgBackend) { distinctOps.flatMap { it.cpgGetAllNodes() } }
                .toSet()

        // `correctNodes` is a subset of `allNodes`
        // we want to find nodes in `allNodes` that are not contained in `correctNodes` since they are violations
        val correctAndOpenNodes = allNodes.minus(violatingNodes)
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
                    node = node
                )
            )
        }

        for (node in correctAndOpenNodes) {
            if (violating[node] == Result.OPEN) {
                findings.add(
                    CpgFinding(
                        message = "Not enough information to evaluate \"${node.code}\"",
                        kind = Finding.Kind.Open,
                        node = node
                    )
                )
            } else {
                findings.add(
                    CpgFinding(
                        message = "Complies with rule: \"${node.code}\". $passMessage",
                        kind = Finding.Kind.Pass,
                        node = node
                    )
                )
            }
        }

        return findings
    }
}
