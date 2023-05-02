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
import de.fraunhofer.aisec.codyze.backends.cpg.coko.CpgFinding
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.CokoBackend
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.EvaluationContext
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.Evaluator
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.Finding
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.*
import org.junit.jupiter.api.assertThrows
import kotlin.io.path.Path
import kotlin.reflect.full.valueParameters
import kotlin.test.Test
import kotlin.test.assertContentEquals
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
        fun init() = op { "Botan.set_key" { signature(Wildcard) } }

        // calling definition explicitly is optional
        fun start(i: Int?) = op { definition("Botan.start") { signature(i) } }
        fun finish() = op { "Botan.finish" { signature(Wildcard) } }
    }

    class OtherImpl {
        fun foo() = op { definition("Botan.foo") { signature(Wildcard) } }
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

    context(CokoBackend)
    private fun createSimpleOrder(testObj: CokoOrderImpl) =
        order(testObj::constructor) {
            - testObj::start
            - testObj::finish
        }

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

            val failFindings = findings.filter { it.kind == Finding.Kind.Fail }
            assertEquals(7, failFindings.size)

            val expectedFailLines = setOf(53, 62, 71, 81, 89, 93, 103)
            val failFindingLines = (failFindings as List<CpgFinding>)
                .mapNotNull { it.node?.location?.region?.startLine }
                .toSet()
            assertEquals(expectedFailLines, failFindingLines)

            val passFindings = findings.filter { it.kind == Finding.Kind.Pass }
            assertEquals(8, findings.filter { it.kind == Finding.Kind.Pass }.size)

            val expectedPassLines = setOf(17, 25, 38, 44, 48, 79, 100)
            val passFindingLines = (passFindings as List<CpgFinding>)
                .mapNotNull { it.node?.location?.region?.startLine }
                .toSet()
            assertEquals(expectedPassLines, passFindingLines)
        }
    }

    context(CokoBackend)
    private fun createSimpleOrderReducedStartNodes(testObj: CokoOrderImpl) =
        order(testObj.constructor(1)) {
            - testObj::start
            - testObj::finish
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
                "Violation against Order: \"p.set_key(key);\". Op \"[Botan.set_key(Wildcard)]\" is not allowed. " +
                    "Expected one of: Botan.start(null)",
                findings.first().message,
            )
        }
    }

    context(CokoBackend)
    private fun createInvalidOrder(testObj: CokoOrderImpl, testObj2: OtherImpl) =
        order(testObj.constructor(1)) {
            - testObj::start
            - testObj2::foo
        }

    @Test
    fun `test order expression with too many bases`() {
        // mocking doesn't work here. We need an actual backend instance
        val sourceFile = getPath("SimpleOrder.java")
        val backend = CokoCpgBackend(config = createCpgConfiguration(sourceFile))

        with(backend) {
            val instance = CokoOrderImpl()
            val instance2 = OtherImpl()
            val orderEvaluator = createInvalidOrder(instance, instance2)
            val findings = orderEvaluator
                .evaluate(
                    EvaluationContext(
                        rule = ::dummyFunction,
                        parameterMap = ::dummyFunction.valueParameters.associateWith { instance }
                    )
                )
            assertEquals(0, findings.size)
        }
    }

    context(CokoBackend)
    private fun createEmptyOrder(testObj: CokoOrderImpl) =
        order(testObj::constructor) { }

    @Test
    fun `test empty order expression`() {
        // mocking doesn't work here. We need an actual backend instance
        val sourceFile = getPath("SimpleOrder.java")
        val backend = CokoCpgBackend(config = createCpgConfiguration(sourceFile))

        with(backend) {
            val instance = CokoOrderImpl()
            val orderEvaluator = createEmptyOrder(instance)

            assertThrows<IllegalArgumentException> {
                orderEvaluator
                    .evaluate(
                        EvaluationContext(
                            rule = ::dummyFunction,
                            parameterMap = ::dummyFunction.valueParameters.associateWith { instance }
                        )
                    )
            }
        }
    }

    context(CokoBackend)
    private fun createOrderWithUserDefinedOps(testObj: CokoOrderImpl) =
        order(testObj::constructor) {
            - testObj.start(1)
            - testObj.finish()
        }

    @Test
    fun `test order with used defined ops`() {
        val sourceFile = getPath("OtherSimpleOrder.java")
        val backend = CokoCpgBackend(config = createCpgConfiguration(sourceFile))

        with(backend) {
            val instance = CokoOrderImpl()
            val orderEvaluator = createOrderWithUserDefinedOps(instance)

            val findings = orderEvaluator
                .evaluate(
                    EvaluationContext(
                        rule = ::dummyFunction,
                        parameterMap = ::dummyFunction.valueParameters.associateWith { instance }
                    )
                )
            assertEquals(5, findings.size, "There were ${findings.size} finding(s) instead of 5 findings.")

            val passFindings = findings.filter { it.kind == Finding.Kind.Pass }
            val failFindings = findings.filter { it.kind == Finding.Kind.Fail }
            assertEquals(
                2,
                passFindings.size,
                "There were ${passFindings.size} Pass findings instead of 2."
            )

            assertEquals(3, failFindings.size, "There were ${failFindings.size} Fail findings instead of 3.")

            val passCpgFinding = passFindings.filterIsInstance<CpgFinding>()
            val failCpgFinding = failFindings.filterIsInstance<CpgFinding>()

            assertContentEquals(passFindings, passCpgFinding, "Not all Pass findings were CpgFindings.")
            assertContentEquals(failFindings, failCpgFinding, "Not all Fail findings were CpgFindings.")

            val expectedPassLines = listOf(15, 23)
            val actualPassLines = passCpgFinding.mapNotNull { it.node?.location?.region?.startLine }
            assertContentEquals(expectedPassLines, actualPassLines)

            val expectedFailLines = listOf(28, 37, 44)
            val actualFailLines = failCpgFinding.mapNotNull { it.node?.location?.region?.startLine }
            assertContentEquals(expectedFailLines, actualFailLines)
        }
    }
}
