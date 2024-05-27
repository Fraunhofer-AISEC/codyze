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
import de.fraunhofer.aisec.cpg.graph.followPrevEOGEdgesUntilHit
import de.fraunhofer.aisec.cpg.graph.statements.expressions.CallExpression
import de.fraunhofer.aisec.cpg.graph.statements.expressions.Reference

context(CokoCpgBackend)
class ArgumentEvaluator(val targetCall: Op, val argPos: Int, val originCall: Op) : Evaluator {
    override fun evaluate(context: EvaluationContext): List<CpgFinding> {
        // Get all good calls and the associated variables
        val originCalls = originCall.cpgGetAllNodes()
        val variables = originCalls.mapNotNull {
            it.tryGetVariableDeclaration()
        }
        val findings = mutableListOf<CpgFinding>()
        // Get all target calls using the variable and check whether it is in a good state
        val targetCalls = targetCall.cpgGetAllNodes()
        for (call in targetCalls) {
            val arg: VariableDeclaration? =
                (call.arguments.getOrNull(argPos) as? Reference)?.refersTo as? VariableDeclaration
            if (arg in variables && !arg!!.allowsInvalidPaths(originCalls.toList(), call)) {
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

    /**
     * Tries to resolve which variable is modified by a CallExpression
     * @return The VariableExpression modified by the CallExpression or null
     */
    private fun CallExpression.tryGetVariableDeclaration(): VariableDeclaration? {
        return when (val nextDFG = this.nextDFG.firstOrNull()) {
            is VariableDeclaration -> nextDFG
            is Reference -> nextDFG.refersTo as? VariableDeclaration
            else -> null
        }
    }

    /**
     * This method tries to get all possible CallExpressions that try to override the variable value
     * @return The CallExpressions modifying the variable
     */
    private fun VariableDeclaration.getOverrides(): List<CallExpression> {
        return this.typeObservers.mapNotNull { (it as? Reference)?.prevDFG?.firstOrNull() as? CallExpression }
    }

    /**
     * This method checks whether there are any paths with forbidden values for this variable that end in the target call
     * @param allowedCalls The calls that set the variable to an allowed value
     * @param targetCall The target call using the variable as an argument
     * @return whether there is at least one path that allows an invalid value for the variable to reach the target
     */
    private fun VariableDeclaration.allowsInvalidPaths(allowedCalls: List<CallExpression>, targetCall: CallExpression): Boolean {
        // Get every MemberCall that tries to override our variable, ignoring allowed calls
        val interferingDeclarations = this.getOverrides().toMutableList() - allowedCalls.toSet()
        // Check whether there is a path from any invalid call to our target call that is not overridden by at least one valid call
        val targetToNoise = targetCall.followPrevEOGEdgesUntilHit { interferingDeclarations.contains(it) }.fulfilled
            .filterNot { badPath -> allowedCalls.any { goodCall -> goodCall in badPath } }
        return targetToNoise.isNotEmpty()
    }
}


