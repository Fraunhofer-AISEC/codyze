package de.fraunhofer.aisec.codyze.backends.testing.coko

import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.EvaluationContext
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.EvaluationResult
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.Evaluator
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.Op
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.Order
import mu.KotlinLogging

private val logger = KotlinLogging.logger { }

sealed interface TestingEvaluator: Evaluator

class OrderEvaluator(val order: Order) : TestingEvaluator {
    override fun evaluate(context: EvaluationContext): EvaluationResult {
        logger.info { "Mocking an order evaluation" }
        return EvaluationResult(true)
    }
}

class FollowsEvaluator(val ifOp: Op, val thenOp: Op) : TestingEvaluator {
    override fun evaluate(context: EvaluationContext) : EvaluationResult {
        logger.info { "Mocking a follows evaluation" }
        return EvaluationResult(true)
    }
}

class OnlyEvaluator(val ops: List<Op>) : TestingEvaluator {
    override fun evaluate(context: EvaluationContext): EvaluationResult {
        logger.info { "Mocking an only evaluation" }
        return EvaluationResult(true)
    }
}