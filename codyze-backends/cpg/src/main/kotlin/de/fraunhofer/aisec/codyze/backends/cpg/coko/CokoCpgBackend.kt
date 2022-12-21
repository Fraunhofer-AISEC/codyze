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

import de.fraunhofer.aisec.codyze.backends.cpg.CPGBackend
import de.fraunhofer.aisec.codyze.backends.cpg.CPGConfiguration
import de.fraunhofer.aisec.codyze.backends.cpg.coko.dsl.*
import de.fraunhofer.aisec.codyze.backends.cpg.coko.evaluators.FollowsEvaluator
import de.fraunhofer.aisec.codyze.backends.cpg.coko.evaluators.OnlyEvaluator
import de.fraunhofer.aisec.codyze.backends.cpg.coko.evaluators.OrderEvaluator
import de.fraunhofer.aisec.codyze.core.VersionProvider
import de.fraunhofer.aisec.codyze.core.backend.BackendConfiguration
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.CokoBackendWithSarifOutput
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.Op
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.Order
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.ordering.OrderToken
import de.fraunhofer.aisec.cpg.graph.Node
import io.github.detekt.sarif4k.Artifact
import io.github.detekt.sarif4k.ArtifactLocation
import io.github.detekt.sarif4k.ToolComponent
import kotlin.io.path.absolutePathString

typealias Nodes = Collection<Node>

/**
 * The CPG backend for Coko.
 */
class CokoCpgBackend(config: BackendConfiguration) :
    CPGBackend(config = config as CPGConfiguration), CokoBackendWithSarifOutput {
    private val cpgConfiguration = config as CPGConfiguration

    override val toolInfo = ToolComponent(
        name = "CPG Coko Backend",
        product = "Codyze",
        organization = "Fraunhofer AISEC",
        semanticVersion = VersionProvider.getVersion("cpg"),
        downloadURI = "https://github.com/Fraunhofer-AISEC/codyze/releases",
        informationURI = "https://www.codyze.io",
        language = "en-US",
        isComprehensive = false
    )
    override val artifacts = cpgConfiguration.source.associateWith {
        Artifact(location = ArtifactLocation(uri = it.absolutePathString()))
    }

    /** For each of the nodes in [this], there is a path to at least one of the nodes in [that]. */
    override infix fun Op.followedBy(that: Op) = FollowsEvaluator(ifOp = this, thenOp = that)

    /* Ensures the order of nodes as specified in the user configured [Order] object */
    override fun order(baseNodes: OrderToken?, block: Order.() -> Unit) =
        OrderEvaluator(
            baseNodes = baseNodes?.call()?.getAllNodes(),
            order = Order().apply(block)
        ) // TODO: use getNodes here instead?

    /**
     * Ensures that all calls to the [ops] have arguments that fit the parameters specified in [ops]
     */
    override fun only(vararg ops: Op) = OnlyEvaluator(ops.toList())
}
