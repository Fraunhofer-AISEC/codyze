package de.fraunhofer.aisec.codyzeBackends.cpg.coko.evaluators

import de.fraunhofer.aisec.codyze.specificationLanguages.coko.cokoCore.EvaluationResult
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.cokoCore.Evaluator
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.cokoCore.dsl.Order
import kotlin.reflect.KFunction

context(de.fraunhofer.aisec.codyze_backends.testing.coko.CokoTestingBackend)

class OrderEvaluator(val order: Order) : Evaluator {
    override fun evaluate(rule: KFunction<*>) = EvaluationResult(false)
}
