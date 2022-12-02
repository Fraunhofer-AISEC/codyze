package de.fraunhofer.aisec.codyzeBackends.cpg.coko.evaluators

import de.fraunhofer.aisec.codyze.specificationLanguages.coko.cokoCore.EvaluationResult
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.cokoCore.Evaluator
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.cokoCore.dsl.Op
import de.fraunhofer.aisec.codyzeBackends.testing.coko.CokoTestingBackend
import kotlin.reflect.KFunction

context(CokoTestingBackend)

class FollowsEvaluator(val ifOp: Op, val thenOp: Op) : Evaluator {
    override fun evaluate(rule: KFunction<*>) = EvaluationResult(false)
}
