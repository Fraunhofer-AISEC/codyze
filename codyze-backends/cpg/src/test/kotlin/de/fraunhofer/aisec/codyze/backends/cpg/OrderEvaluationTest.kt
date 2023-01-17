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
package de.fraunhofer.aisec.codyze.backends.cpg

import de.fraunhofer.aisec.codyze.backends.cpg.coko.CokoCpgBackend
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.CokoBackend
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.EvaluationContext
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.Evaluator
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.Finding
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.*
import de.fraunhofer.aisec.cpg.passes.EdgeCachePass
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.reflect.full.valueParameters
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests the order evaluation starting from a coko order expression.
 *
 * If this test fails, make sure that the following tests work first as the functionality they test
 * is needed for this test:
 * - [NfaDfaConstructionTest]
 */
class OrderEvaluationTest {
    class CokoOrderImpl {
        fun constructor(value: Int?) = constructor("Botan") { signature(value) }
        fun init() = op { definition("Botan.set_key") { signature(Wildcard) } }
        fun start() = op { definition("Botan.start") { signature(Wildcard) } }
        fun finish() = op { definition("Botan.finish") { signature(Wildcard) } }
    }

    context(CokoBackend)
    private fun createSimpleOrder(testObj: CokoOrderImpl) =
        order(testObj::constructor) {
            +testObj::start
            +testObj::finish
        }

    context(CokoBackend)
    private fun createSimpleOrderReducedStartNodes(testObj: CokoOrderImpl) =
        order(testObj::constructor.use { testObj.constructor(1) }) {
            +testObj::start
            +testObj::finish
        }

    // function with the same signature as the 'rule' [createSimpleDfa] because the kotlin compiler
    // crashes
    // when trying to create a function reference to [createSimpleDfa] using: '::createSimpleOrder'
    // (probably because
    // of the context receiver)
    // this is needed as an argument to [evaluateOrder]
    @Suppress("UnusedPrivateMember")
    private fun dummyFunction(testObj: CokoOrderImpl): Evaluator = TODO()

    private val basePath = Path("src", "test", "resources", "OrderEvaluationTest")

    private fun getPath(sourceFileName: String) = basePath.resolve(sourceFileName).toAbsolutePath()

    private fun createCpgConfiguration(vararg sourceFile: Path) =
        CPGConfiguration(
            source = listOf(*sourceFile),
            useUnityBuild = false,
            typeSystemActiveInFrontend = true,
            debugParser = false,
            disableCleanup = false,
            codeInNodes = true,
            matchCommentsToNodes = false,
            processAnnotations = false,
            failOnError = false,
            useParallelFrontends = false,
            defaultPasses = true,
            additionalLanguages = setOf(),
            symbols = mapOf(),
            includeBlocklist = listOf(),
            includePaths = listOf(),
            includeAllowlist = listOf(),
            loadIncludes = false,
            passes = listOf(EdgeCachePass()),
        )

    @Test
    fun `test simple order expression for java`() {
        // mocking doesn't work here. We need an actual backend instance
        val sourceFile = getPath("SimpleOrder.java")
        val backend = CokoCpgBackend(config = createCpgConfiguration(sourceFile))

        with(backend) {
            val instance = CokoOrderImpl()
            val orderEvaluator = createSimpleOrder(instance)
            val findings = orderEvaluator
                .evaluate(
                    EvaluationContext(
                        rule = ::dummyFunction,
                        parameterMap = ::dummyFunction.valueParameters.associateWith { instance }
                    )
                )
            assertEquals(7, findings.filter { it.kind == Finding.Kind.Fail }.size)
        }
    }

    @Test
    fun `test simple order expression for java with reduced start nodes`() {
        // mocking doesn't work here. We need an actual backend instance
        val sourceFile = getPath("SimpleOrder.java")
        val backend = CokoCpgBackend(config = createCpgConfiguration(sourceFile))

        with(backend) {
            val instance = CokoOrderImpl()
            val orderEvaluator = createSimpleOrderReducedStartNodes(instance)
            val findings = orderEvaluator
                .evaluate(
                    EvaluationContext(
                        rule = ::dummyFunction,
                        parameterMap = ::dummyFunction.valueParameters.associateWith { instance }
                    )
                )
            assertEquals(1, findings.size)
            assertEquals(
                "Violation against Order: \"p.set_key(key);\". Op \"[init]\" is not allowed. " +
                    "Expected one of: CokoOrderImpl.start",
                findings.first().message,
            )
        }
    }
}
