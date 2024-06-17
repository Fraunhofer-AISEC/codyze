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
import de.fraunhofer.aisec.codyze.backends.cpg.coko.evaluators.*
import de.fraunhofer.aisec.codyze.core.VersionProvider
import de.fraunhofer.aisec.codyze.core.backend.BackendConfiguration
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.CokoBackend
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.WheneverEvaluator
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.Condition
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.Op
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.Order
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.modelling.ConditionComponent
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.ordering.OrderToken
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.ordering.getOp
import de.fraunhofer.aisec.cpg.graph.Node
import io.github.detekt.sarif4k.Artifact
import io.github.detekt.sarif4k.ArtifactLocation
import io.github.detekt.sarif4k.ToolComponent
import kotlin.io.path.absolutePathString
import kotlin.reflect.KFunction

typealias Nodes = Collection<Node>

/**
 * The CPG backend for Coko.
 */
class CokoCpgBackend(config: BackendConfiguration) :
    CPGBackend(config = config as CPGConfiguration), CokoBackend {
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
    override infix fun Op.followedBy(that: Op): FollowsEvaluator = FollowsEvaluator(ifOp = this, thenOp = that)

    /*
     * Ensures the order of nodes as specified in the user configured [Order] object.
     * The order evaluation starts at the given [baseNodes].
     */
    override fun order(baseNodes: OrderToken, block: Order.() -> Unit): OrderEvaluator =
        OrderEvaluator(
            baseNodes = baseNodes.getOp().cpgGetAllNodes(),
            order = Order().apply(block)
        )

    /*
     * Ensures the order of nodes as specified in the user configured [Order] object.
     * The order evaluation starts at the given [baseNodes].
     */
    override fun order(baseNodes: Op, block: Order.() -> Unit): OrderEvaluator =
        OrderEvaluator(
            baseNodes = baseNodes.cpgGetNodes(),
            order = Order().apply(block)
        )

    /** Verifies that the argument at [argPos] of [targetOp] stems from a call to [originOp] */
    override fun argumentOrigin(targetOp: KFunction<Op>, argPos: Int, originOp: KFunction<Op>): ArgumentEvaluator =
        ArgumentEvaluator(
            targetCall = targetOp.getOp(),
            argPos = argPos,
            originCall = originOp.getOp()
        )

    /**
     * Ensures that all calls to the [ops] have arguments that fit the parameters specified in [ops]
     */
    override fun only(vararg ops: Op): OnlyEvaluator = OnlyEvaluator(ops.toList())
    override fun never(vararg ops: Op): NeverEvaluator = NeverEvaluator(ops.toList())
    override fun whenever(
        premise: Condition.() -> ConditionComponent,
        assertionBlock: WheneverEvaluator.() -> Unit
    ): WheneverEvaluator = CpgWheneverEvaluator(Condition().premise()).apply(assertionBlock)

    override fun whenever(
        premise: ConditionComponent,
        assertionBlock: WheneverEvaluator.() -> Unit
    ): WheneverEvaluator = CpgWheneverEvaluator(premise).apply(assertionBlock)
}
