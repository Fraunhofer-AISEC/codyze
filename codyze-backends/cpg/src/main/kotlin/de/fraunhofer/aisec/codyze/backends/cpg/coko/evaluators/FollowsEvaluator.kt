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
import de.fraunhofer.aisec.codyze.backends.cpg.coko.dsl.getNodes
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.EvaluationContext
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.Evaluator
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.Finding
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.Op
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.Rule
import de.fraunhofer.aisec.cpg.query.executionPath
import kotlin.reflect.full.findAnnotation

context(CokoCpgBackend)
class FollowsEvaluator(val ifOp: Op, val thenOp: Op) : Evaluator {

    private val defaultFailMessage: String by lazy {
        "It is not followed by any of these calls: $thenOp"
    }

    private val defaultPassMessage = ""

    override fun evaluate(context: EvaluationContext): List<CpgFinding> {
        val thisNodes = with(this@CokoCpgBackend) { ifOp.getNodes() }
        val thatNodes = with(this@CokoCpgBackend) { thenOp.getNodes() }

        val findings = mutableListOf<CpgFinding>()

        val ruleAnnotation = context.rule.findAnnotation<Rule>()
        val failMessage = ruleAnnotation?.failMessage ?: defaultFailMessage
        val passMessage = ruleAnnotation?.passMessage ?: defaultPassMessage

        for (from in thisNodes) {
            val reachableThatNodes = thatNodes.filter { to -> executionPath(from, to).value }
            val finding = if (reachableThatNodes.isEmpty()) {
                CpgFinding(
                    message = "Violation against rule: \"${from.code}\". $failMessage",
                    kind = Finding.Kind.Fail,
                    node = from
                )
            } else {
                CpgFinding(
                    message = "Complies with rule: \"${from.code}\" is followed by ${reachableThatNodes.joinToString(
                        prefix = "\"",
                        separator = "\", \"",
                        postfix = "\""
                    )}. $passMessage",
                    kind = Finding.Kind.Pass,
                    node = from,
                    relatedNodes = reachableThatNodes
                )
            }

            findings.add(finding)
        }

        return findings
    }
}
