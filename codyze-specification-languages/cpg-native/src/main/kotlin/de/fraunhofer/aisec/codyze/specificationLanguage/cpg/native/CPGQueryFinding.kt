package de.fraunhofer.aisec.codyze.specificationLanguage.cpg.native

import de.fraunhofer.aisec.codyze.backends.cpg.coko.getSarifLocation
import de.fraunhofer.aisec.codyze.specificationLanguage.cpg.native.queries.CPGQuery
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.Finding
import de.fraunhofer.aisec.cpg.graph.Node
import io.github.detekt.sarif4k.Artifact
import io.github.detekt.sarif4k.Level
import io.github.detekt.sarif4k.Message
import io.github.detekt.sarif4k.Result
import java.nio.file.Path

/**
 * An implementation of a [Finding] specifically for native queries.
 */
data class CpgQueryFinding(
    val message: String,
    val kind: Finding.Kind = Finding.Kind.Fail,
    val node: Node? = null,
    val relatedNodes: Collection<Node>? = null,
) {
    fun toSarif(query: CPGQuery, queries: List<CPGQuery>, artifacts: Map<Path, Artifact>?) =
        Result(
            message = Message(text = message),
            kind = kind.resultKind,
            level = if (kind == Finding.Kind.Fail) {
                query.level
            } else {
                Level.None
            },
            ruleIndex = queries.indexOf(query).toLong(),
            locations = node?.let { listOf(node.getSarifLocation(artifacts)) },
            relatedLocations = relatedNodes?.map { node ->
                node.getSarifLocation(artifacts)
            }
        )
}
