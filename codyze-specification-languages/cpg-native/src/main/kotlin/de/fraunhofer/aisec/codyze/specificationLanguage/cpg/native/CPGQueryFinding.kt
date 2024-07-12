/*
 * Copyright (c) 2024, Fraunhofer AISEC. All rights reserved.
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
