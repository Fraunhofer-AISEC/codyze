package de.fraunhofer.aisec.codyze.backends.cpg.coko.evaluators

import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.EvaluationResult
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.EvaluationContext
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.Evaluator
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.dsl.Op
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.query.executionPath
import kotlin.reflect.KFunction

class FollowsEvaluator(val ifOp: Op, val thenOp: Op): Evaluator {
    context(EvaluationContext)
    override fun evaluate(rule: KFunction<*>): EvaluationResult {
        val evaluator = {
            val thisNodes =  with(this@EvaluationContext.backend) { ifOp.getNodes() }.filterIsInstance<Node>()
            val thatNodes = with(this@EvaluationContext.backend) { thenOp.getNodes() }.filterIsInstance<Node>()
            thisNodes.all { from -> thatNodes.any { to -> executionPath(from, to).value } }
        }
        return EvaluationResult(evaluator())
    }
}