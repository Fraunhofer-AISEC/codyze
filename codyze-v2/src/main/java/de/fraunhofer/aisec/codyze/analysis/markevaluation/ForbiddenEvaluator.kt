package de.fraunhofer.aisec.codyze.analysis.markevaluation

import de.fraunhofer.aisec.codyze.analysis.AnalysisContext
import de.fraunhofer.aisec.codyze.analysis.Finding
import de.fraunhofer.aisec.codyze.analysis.utils.Utils
import de.fraunhofer.aisec.codyze.markmodel.MOp
import de.fraunhofer.aisec.codyze.markmodel.Mark
import de.fraunhofer.aisec.mark.markDsl.Action
import de.fraunhofer.aisec.mark.markDsl.OpStatement
import java.io.File
import org.slf4j.LoggerFactory

class ForbiddenEvaluator(private val markModel: Mark) {
    /**
     * For a call to be forbidden, it needs to:
     *
     * - match any forbidden signature (as call statement in an op) with `*` for arbitrary
     * parameters, `_` for ignoring one parameter type, or
     * - be a reference to a var in the entity to specify a concrete type (no type hierarchy is
     * analyzed!) _and_ is not allowed by any other non-forbidden matching call statement (in _any_
     * op).
     *
     * After this method, findings have been added to [AnalysisContext.findings].
     */
    fun evaluate(ctx: AnalysisContext) {
        for (entity in markModel.entities) {
            for (op in entity.ops) {
                for ((node, value) in op.nodesToStatements) {
                    if (
                        value.stream().noneMatch { call: OpStatement ->
                            "forbidden" == call.forbidden
                        }
                    ) {
                        // only allowed entries
                        continue
                    }
                    var nodeAllowed = false
                    val violating = HashSet<String>()
                    for (call in value) {
                        val callString =
                            call.call.name +
                                "(" +
                                java.lang.String.join(",", MOp.paramsToString(call.call.params)) +
                                ")"
                        if ("forbidden" != call.forbidden) {
                            // there is at least one CallStatement which explicitly allows this
                            // Vertex!
                            log.info(
                                "Node |{}| is allowed, since it matches whitelist entry {}",
                                node.code,
                                callString
                            )
                            nodeAllowed = true
                            break
                        } else {
                            violating.add(callString)
                        }
                    }
                    if (!nodeAllowed) {
                        val region = Utils.getRegionByNode(node)
                        val message =
                            ("Violation against forbidden call(s) " +
                                java.lang.String.join(", ", violating) +
                                " in entity " +
                                entity.name +
                                ". Call was " +
                                node.code)
                        val f =
                            Finding(
                                "FORBIDDEN_" + entity.name,
                                Action.FAIL,
                                message,
                                File(node.file).toURI(),
                                region.startLine,
                                region.endLine,
                                region.startColumn,
                                region.endColumn
                            )

                        ctx.findings.add(f)
                        log.info("Finding: {}", f)
                    }
                }
            }
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(ForbiddenEvaluator::class.java)
    }
}
