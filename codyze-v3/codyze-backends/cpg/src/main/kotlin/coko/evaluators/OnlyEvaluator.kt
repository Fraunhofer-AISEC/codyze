package de.fraunhofer.aisec.codyze_backends.cpg.coko.evaluators

import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.EvaluationResult
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.Evaluator
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.dsl.Op
import de.fraunhofer.aisec.cpg.graph.Node
import kotlin.reflect.KFunction

context(de.fraunhofer.aisec.codyze_backends.cpg.coko.CokoCpgBackend)

class OnlyEvaluator(val ops: List<Op>): Evaluator {
    override fun evaluate(rule: KFunction<*>): EvaluationResult {
        val nodes = with(this@CokoCpgBackend) { ops.map { it.getNodes() } }.flatten().filterIsInstance<Node>().toSet()

        val distinctOps = ops.toSet()
        val allNodes = with(this@CokoCpgBackend) { distinctOps.map { it.getAllNodes() } }.flatten().filterIsInstance<Node>().toSet()

        return EvaluationResult(nodes == allNodes)
    }
}