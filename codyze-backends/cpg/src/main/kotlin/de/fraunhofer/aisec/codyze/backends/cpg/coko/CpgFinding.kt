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
package de.fraunhofer.aisec.codyze.backends.cpg.coko

import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.CokoRule
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.Finding
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.Rule
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.toResultLevel
import de.fraunhofer.aisec.cpg.graph.Node
import io.github.detekt.sarif4k.*
import io.github.detekt.sarif4k.Result
import java.nio.file.Path
import kotlin.io.path.toPath
import kotlin.reflect.full.findAnnotation

/**
 * Returns a `Region` object from a node's startLine, endLine, startColumn, endColumn property.
 *
 * Note that these are not the exact property values but start at 0 rather than by 1.
 * If these properties do not exist, returns -1.
 *
 * @param n the node
 *
 * @return the region
 */
val Node.sarifRegion: Region
    get() {
        val startLine = location?.region?.startLine?.minus(1)?.toLong()
        val endLine = location?.region?.endLine?.minus(1)?.toLong()
        val startColumn = location?.region?.startColumn?.minus(1)?.toLong()
        val endColumn = location?.region?.endColumn?.minus(1)?.toLong()
        return Region(
            startLine = startLine,
            startColumn = startColumn,
            endLine = endLine,
            endColumn = endColumn,
            sourceLanguage = language?.let { it::class.simpleName }
        )
    }

fun Node.getSarifLocation(artifacts: Map<Path, Artifact>?) =
    Location(
        physicalLocation = PhysicalLocation(
            artifactLocation = ArtifactLocation(
                index = (
                    artifacts?.keys?.indexOf(
                        location?.artifactLocation?.uri?.toPath()
                    )?.toLong()
                    ),
                uri = location?.artifactLocation?.uri.toString()
            ),
            region = sarifRegion
        )
    )

data class CpgFinding(
    override val message: String,
    val node: Node? = null,
    val relatedNodes: Nodes? = null,
    val kind: Kind = Kind.Fail
) : Finding {
    override fun toSarif(rule: CokoRule, rules: List<CokoRule>, artifacts: Map<Path, Artifact>?) =
        Result(
            message = Message(text = message),
            kind = kind.resultKind,
            level = if (kind == Kind.Fail) {
                rule.findAnnotation<Rule>()?.severity?.toResultLevel()
            } else {
                Level.None
            },
            ruleIndex = rules.indexOf(rule).toLong(),
            locations = node?.let { listOf(node.getSarifLocation(artifacts)) },
            relatedLocations = relatedNodes?.map { node ->
                node.getSarifLocation(artifacts)
            }
        )

    enum class Kind(val resultKind: ResultKind) {
        Fail(ResultKind.Fail),
        Informational(ResultKind.Informational),
        NotApplicable(ResultKind.NotApplicable),
        Open(ResultKind.Open),
        Pass(ResultKind.Pass),
        Review(ResultKind.Review);
    }
}
