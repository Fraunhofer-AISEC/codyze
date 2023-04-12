package de.fraunhofer.aisec.codyze.backends.cpg.coko.evaluators

import de.fraunhofer.aisec.codyze.backends.cpg.coko.CokoCpgBackend
import de.fraunhofer.aisec.codyze.backends.cpg.coko.CpgFinding
import de.fraunhofer.aisec.codyze.backends.cpg.coko.dsl.cpgGetNodes
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.EvaluationContext
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.Evaluator
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.Finding
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.Op
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.Rule
import kotlin.reflect.full.findAnnotation

context(CokoCpgBackend)
class NeverEvaluator(val forbiddenOps: List<Op>) : Evaluator {

    /** Default message if a violation is found */
    private val defaultFailMessage: String by lazy {
        "Calls to ${forbiddenOps.joinToString()} are not allowed."
    }

    /** Default message if node complies with rule */
    private val defaultPassMessage = "No calls to ${forbiddenOps.joinToString()} found which is in compliance with rule."

    override fun evaluate(context: EvaluationContext): Collection<Finding> {
        val ruleAnnotation = context.rule.findAnnotation<Rule>()
        val failMessage = ruleAnnotation?.failMessage?.takeIf { it.isNotEmpty() } ?: defaultFailMessage
        val passMessage = ruleAnnotation?.passMessage?.takeIf { it.isNotEmpty() } ?: defaultPassMessage

        val findings = mutableListOf<CpgFinding>()

        for (op in forbiddenOps) {
            val nodes = op.cpgGetNodes()

            if (nodes.isNotEmpty()) {
                // This means there are calls to the forbidden op, so Fail findings are added
                for (node in nodes) {
                    findings.add(
                        CpgFinding(
                            message = "Violation against rule: \"${node.code}\". $failMessage",
                            kind = Finding.Kind.Fail,
                            node = node
                        )
                    )
                }
            }
        }

        // If there are no findings, there were no violations, so a Pass finding is added
        if (findings.isEmpty()) {
            findings.add(
                CpgFinding(
                    message = passMessage,
                    kind = Finding.Kind.Pass,
                )
            )
        }
        return findings
    }
}
