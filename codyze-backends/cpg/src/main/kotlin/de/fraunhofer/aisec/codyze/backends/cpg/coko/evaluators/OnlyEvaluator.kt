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
import de.fraunhofer.aisec.codyze.backends.cpg.coko.dsl.cpgGetAllNodes
import de.fraunhofer.aisec.codyze.backends.cpg.coko.dsl.cpgGetNodes
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.EvaluationContext
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.Evaluator
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.Finding
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.Op
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.Rule
import kotlin.reflect.full.findAnnotation

context(CokoCpgBackend)
class OnlyEvaluator(val ops: List<Op>) : Evaluator {

    /** Default message if a violation is found */
    private val defaultFailMessage: String by lazy {
        // Try to model what the allowed calls look like with `toString` call of `Op`
        "Only calls to ${ops.joinToString()} allowed."
    }

    /** Default message if node complies with rule */
    private val defaultPassMessage = "Call is in compliance with rule"

    override fun evaluate(context: EvaluationContext): List<CpgFinding> {
        val correctNodes =
            with(this@CokoCpgBackend) { ops.flatMap { it.cpgGetNodes() } }
                .toSet()

        val distinctOps = ops.toSet()
        val allNodes =
            with(this@CokoCpgBackend) { distinctOps.flatMap { it.cpgGetAllNodes() } }
                .toSet()

        // `correctNodes` is a subset of `allNodes`
        // we want to find nodes in `allNodes` that are not contained in `correctNodes` since they are violations
        val violatingNodes = allNodes.minus(correctNodes)

        val ruleAnnotation = context.rule.findAnnotation<Rule>()
        val failMessage = ruleAnnotation?.failMessage?.takeIf { it.isNotEmpty() } ?: defaultFailMessage
        val passMessage = ruleAnnotation?.passMessage?.takeIf { it.isNotEmpty() } ?: defaultPassMessage

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

        for (node in correctNodes) {
            findings.add(
                CpgFinding(
                    message = "Complies with rule: \"${node.code}\". $passMessage",
                    kind = Finding.Kind.Pass,
                    node = node
                )
            )
        }

        return findings
    }
}
