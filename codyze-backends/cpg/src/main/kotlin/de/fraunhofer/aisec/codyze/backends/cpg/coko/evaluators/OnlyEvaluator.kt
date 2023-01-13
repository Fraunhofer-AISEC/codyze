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
import de.fraunhofer.aisec.codyze.backends.cpg.coko.dsl.getAllNodes
import de.fraunhofer.aisec.codyze.backends.cpg.coko.dsl.getNodes
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.EvaluationContext
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.Evaluator
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.Op

context(CokoCpgBackend)
@Suppress("UnusedPrivateMember") // TODO: remove once this returns actual findings
class OnlyEvaluator(val ops: List<Op>) : Evaluator {
    override fun evaluate(context: EvaluationContext): List<CpgFinding> {
        val nodes =
            with(this@CokoCpgBackend) { ops.map { it.getNodes() } }
                .flatten()
                .toSet()

        val distinctOps = ops.toSet()
        val allNodes =
            with(this@CokoCpgBackend) { distinctOps.map { it.getAllNodes() } }
                .flatten()
                .toSet()

        return listOf(CpgFinding("TODO"))
    }
}
