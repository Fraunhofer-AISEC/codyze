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
import de.fraunhofer.aisec.codyze.backends.cpg.coko.dsl.cpgGetNodes
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.EvaluationContext
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.Evaluator
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.Finding
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.Op
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.Rule
import kotlin.reflect.full.findAnnotation

context(CokoCpgBackend)
class NeverEvaluator(val forbiddenOps: List<Op>) : Evaluator {

    /** Default message if a violation is found */
    private val defaultFailMessage: String by lazy {
        "Calls to ${forbiddenOps.joinToString()} are not allowed."
    }

    /** Default message if the analyzed code complies with rule */
    private val defaultPassMessage by lazy {
        "No calls to ${forbiddenOps.joinToString()} found which is in compliance with rule."
    }

    override fun evaluate(context: EvaluationContext): Collection<Finding> {
        val ruleAnnotation = context.rule.findAnnotation<Rule>()
        val failMessage = ruleAnnotation?.failMessage?.takeIf { it.isNotEmpty() } ?: defaultFailMessage
        val passMessage = ruleAnnotation?.passMessage?.takeIf { it.isNotEmpty() } ?: defaultPassMessage

        val findings = mutableListOf<CpgFinding>()

        for (op in forbiddenOps) {
            val nodes = op.cpgGetNodes()

            if (nodes.isNotEmpty()) {
                // This means there are calls to the forbidden op, so Fail findings are added
                for (node in nodes) {
                    if (node.value == Result.OPEN) {
                        findings.add(
                            CpgFinding(
                                message = "Not enough information to evaluate \"${node.key.code}\"",
                                kind = Finding.Kind.Open,
                                node = node.key
                            )
                        )
                    }
                    findings.add(
                        CpgFinding(
                            message = "Violation against rule: \"${node.key.code}\". $failMessage",
                            kind = Finding.Kind.Fail,
                            node = node.key
                        )
                    )
                }
            }
        }

        // If there are no findings, there were no violations, so a Pass finding is added
        if (findings.isEmpty()) {
            findings.add(
                CpgFinding(
                    message = passMessage,
                    kind = Finding.Kind.Pass,
                )
            )
        }
        return findings
    }
}
