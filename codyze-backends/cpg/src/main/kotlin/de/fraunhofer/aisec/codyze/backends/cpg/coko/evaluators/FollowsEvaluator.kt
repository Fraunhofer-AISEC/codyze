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
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.EvaluationContext
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.EvaluationResult
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.Evaluator
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.dsl.Op
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.query.executionPath

context(CokoCpgBackend)

class FollowsEvaluator(val ifOp: Op, val thenOp: Op) : Evaluator {
    override fun evaluate(context: EvaluationContext): EvaluationResult {
        val evaluator = {
            val thisNodes = with(this@CokoCpgBackend) { ifOp.getNodes() }.filterIsInstance<Node>()
            val thatNodes = with(this@CokoCpgBackend) { thenOp.getNodes() }.filterIsInstance<Node>()
            thisNodes.all { from -> thatNodes.any { to -> executionPath(from, to).value } }
        }
        return EvaluationResult(evaluator())
    }
}
