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
import de.fraunhofer.aisec.codyze.backends.cpg.coko.dsl.cpgGetNodes
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.EvaluationContext
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.Evaluator
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.Finding
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.Op
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.Rule
import de.fraunhofer.aisec.cpg.graph.*
import de.fraunhofer.aisec.cpg.graph.edge.Properties
import kotlin.reflect.full.findAnnotation

context(CokoCpgBackend)
class PrecedesEvaluator(val prevOp: Op, val thisOp: Op) : Evaluator {

    private val defaultFailMessage: String by lazy {
        "It is not preceded by any of these calls: $prevOp."
    }

    private val defaultPassMessage = ""

    override fun evaluate(context: EvaluationContext): List<CpgFinding> {
        val (unreachableThisNodes, thisNodes) =
            with(this@CokoCpgBackend) { thisOp.cpgGetNodes().toSet() }
                .partition { it.isUnreachable() }

        val prevNodes = with(this@CokoCpgBackend) { prevOp.cpgGetNodes().toSet() }

        val findings = mutableListOf<CpgFinding>()

        // add all unreachable `this` nodes as NotApplicable findings
        findings.addAll(
            unreachableThisNodes.map {
                CpgFinding(
                    message = "Rule is not applicable for \"${it.code}\" because it is unreachable",
                    kind = Finding.Kind.NotApplicable,
                    node = it
                )
            }
        )

        val ruleAnnotation = context.rule.findAnnotation<Rule>()
        val failMessage = ruleAnnotation?.failMessage?.takeIf { it.isNotEmpty() } ?: defaultFailMessage
        val passMessage = ruleAnnotation?.passMessage?.takeIf { it.isNotEmpty() } ?: defaultPassMessage

        for (target in thisNodes) {
            val paths = target.followPrevEOGEdgesUntilHit { prevNodes.contains(it) }

            val newFindings =
                if (paths.fulfilled.isNotEmpty() && paths.failed.isEmpty()) {
                    val availablePrevNodes = paths.fulfilled.mapNotNull { it.firstOrNull() }
                    // All paths starting from `from` end in one of the `that` nodes
                    listOf(
                        CpgFinding(
                            message = "Complies with rule: ${availablePrevNodes.joinToString(
                                prefix = "\"",
                                separator = "\", \"",
                                postfix = "\"",
                                transform = { node -> node.code ?: node.toString() }
                            )} precedes ${target.code}. $passMessage",
                            kind = Finding.Kind.Pass,
                            node = target,
                            relatedNodes = availablePrevNodes
                        )
                    )
                } else {
                    // Some (or all) paths starting from `from` do not end in any of the `that` nodes
                    paths.failed.map { failedPath ->
                        // make a finding for each failed path
                        CpgFinding(
                            message =
                            "Violation against rule in execution path to \"${target.code}\". $failMessage",
                            kind = Finding.Kind.Fail,
                            node = target,
                            // improve: specify paths more precisely
                            // for example one branch passes and one fails skip part in path after branches are combined
                            relatedNodes = listOf(failedPath.first())
                        )
                    }
                }

            findings.addAll(newFindings)
        }

        return findings
    }

    /** Checks if this node is unreachable */
    private fun Node.isUnreachable(): Boolean {
        val prevPaths = this.followPrevEOGEdgesUntilHit {
            it.prevEOGEdges.isNotEmpty() && it.prevEOGEdges.all {
                    edge ->
                edge.getProperty(Properties.UNREACHABLE) == true
            }
        }
        return prevPaths.fulfilled.isNotEmpty()
    }
}
