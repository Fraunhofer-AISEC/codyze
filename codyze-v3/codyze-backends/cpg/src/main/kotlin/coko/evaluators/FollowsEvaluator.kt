package de.fraunhofer.aisec.codyze_backends.cpg.coko.evaluators

import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.EvaluationResult
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.Evaluator
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.dsl.Op
import de.fraunhofer.aisec.codyze_backends.cpg.coko.CokoCpgBackend
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.query.executionPath
import kotlin.reflect.KFunction

context(CokoCpgBackend)

class FollowsEvaluator(val ifOp: Op, val thenOp: Op) : Evaluator {
    override fun evaluate(rule: KFunction<*>): EvaluationResult {
        val evaluator = {
            val thisNodes = with(this@CokoCpgBackend) { ifOp.getNodes() }.filterIsInstance<Node>()
            val thatNodes = with(this@CokoCpgBackend) { thenOp.getNodes() }.filterIsInstance<Node>()
            thisNodes.all { from -> thatNodes.any { to -> executionPath(from, to).value } }
        }
        return EvaluationResult(evaluator())
    }
}
