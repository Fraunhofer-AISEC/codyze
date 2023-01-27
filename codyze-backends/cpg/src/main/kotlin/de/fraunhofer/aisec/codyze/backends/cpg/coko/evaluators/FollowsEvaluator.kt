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
import de.fraunhofer.aisec.cpg.graph.followNextEOGEdgesUntilHit
import kotlin.reflect.full.findAnnotation

context(CokoCpgBackend)
class FollowsEvaluator(val ifOp: Op, val thenOp: Op) : Evaluator {

    private val defaultFailMessage: String by lazy {
        "It is not followed by any of these calls: $thenOp."
    }

    private val defaultPassMessage = ""

    override fun evaluate(context: EvaluationContext): List<CpgFinding> {
        val thisNodes = with(this@CokoCpgBackend) { ifOp.cpgGetNodes() }
        val thatNodes = with(this@CokoCpgBackend) { thenOp.cpgGetNodes().toSet() }

        val findings = mutableListOf<CpgFinding>()

        val ruleAnnotation = context.rule.findAnnotation<Rule>()
        val failMessage = ruleAnnotation?.failMessage?.takeIf { it.isNotEmpty() } ?: defaultFailMessage
        val passMessage = ruleAnnotation?.passMessage?.takeIf { it.isNotEmpty() } ?: defaultPassMessage

        for (from in thisNodes) {
            val paths = from.followNextEOGEdgesUntilHit { thatNodes.contains(it) }

            val newFindings =
                if (paths.fulfilled.isNotEmpty() && paths.failed.isEmpty()) {
                    val reachableThatNodes = paths.fulfilled.mapNotNull { it.lastOrNull() }
                    // All paths starting from `from` end in one of the `that` nodes
                    listOf(
                        CpgFinding(
                            message = "Complies with rule: \"${from.code}\" is followed by ${
                            reachableThatNodes.joinToString(
                                prefix = "\"",
                                separator = "\", \"",
                                postfix = "\"",
                                transform = { node -> node.code ?: node.toString() }
                            )}. $passMessage",
                            kind = Finding.Kind.Pass,
                            node = from,
                            relatedNodes = reachableThatNodes
                        )
                    )
                } else {
                    // Some (or all) paths starting from `from` do not end in any of the `that` nodes
                    paths.failed.map { failedPath ->
                        // make a finding for each failed path
                        CpgFinding(
                            message =
                            "Violation against rule in execution path from \"${from.code}\". $failMessage",
                            kind = Finding.Kind.Fail,
                            node = from,
                            // improve: specify paths more precisely
                            // for example one branch passes and one fails skip part in path after branches are combined
                            relatedNodes = failedPath
                        )
                    }
                }

            findings.addAll(newFindings)
        }

        return findings
    }
}
