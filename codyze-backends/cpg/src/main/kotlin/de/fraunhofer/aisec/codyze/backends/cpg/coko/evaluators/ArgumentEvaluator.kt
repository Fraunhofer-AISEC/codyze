/*
 * Copyright (c) 2024, Fraunhofer AISEC. All rights reserved.
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
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.EvaluationContext
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.Evaluator
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.Finding
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.Op
import de.fraunhofer.aisec.cpg.graph.declarations.VariableDeclaration
import de.fraunhofer.aisec.cpg.graph.statements.expressions.CallExpression
import de.fraunhofer.aisec.cpg.graph.statements.expressions.Reference

context(CokoCpgBackend)
class ArgumentEvaluator(val targetCall: Op, val argPos: Int, val originCall: Op) : Evaluator {
    override fun evaluate(context: EvaluationContext): List<CpgFinding> {
        val originCalls = originCall.cpgGetAllNodes()
        val variables = originCalls.mapNotNull {
            it.tryGetVariableDeclaration()
        }

        val findings = mutableListOf<CpgFinding>()
        val targetCalls = targetCall.cpgGetAllNodes()
        for (call in targetCalls) {
            val arg: VariableDeclaration? =
                (call.arguments.getOrNull(argPos) as? Reference)?.refersTo as? VariableDeclaration
            // TODO: fix check: variable MUST directly lead to arg without overwrites/alternatives
            if (arg in variables) {
                findings.add(
                    CpgFinding(
                        message = "Complies with rule: " +
                            "arg $argPos of \"${call.code}\" stems from a call to \"$originCall\"",
                        kind = Finding.Kind.Pass,
                        node = call,
                        relatedNodes = listOfNotNull(originCalls.firstOrNull { it.tryGetVariableDeclaration() == arg })
                    )
                )
            } else {
                findings.add(
                    CpgFinding(
                        message = "Violation against rule: " +
                            "arg $argPos of \"${call.code}\" does not stem from a call to \"$originCall\"",
                        kind = Finding.Kind.Fail,
                        node = call,
                        relatedNodes = listOf()
                    )
                )
            }
        }

        return findings
    }

    private fun CallExpression.tryGetVariableDeclaration(): VariableDeclaration? {
        return when (val nextDFG = this.nextDFG.firstOrNull()) {
            is VariableDeclaration -> nextDFG
            is Reference -> nextDFG.refersTo as? VariableDeclaration
            else -> null
        }
    }
}
