/*
 * Copyright (c) 2023, Fraunhofer AISEC. All rights reserved.
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
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.EvaluationContext
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.Finding
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.definition
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.op
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.signature
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.nio.file.Path
import kotlin.io.path.*
import kotlin.reflect.full.valueParameters
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class NeverEvaluationTest {

    @Suppress("UNUSED")
    class FooModel {
        fun first(i: Any) = op {
            definition("Foo.first") {
                signature(i)
            }
        }
    }

    @Test
    fun `test never with violation`() {
        val fooInstance = FooModel()

        val backend = CokoCpgBackend(config = createCpgConfiguration(violationFile))

        with(backend) {
            // Evaluator does not allow calls to `first` with -1 or a number between 1230 and 1240
            val evaluator = never(
                fooInstance.first(-1),
                fooInstance.first(1230..1240)
            )
            val findings = evaluator.evaluate(
                EvaluationContext(
                    rule = ::dummyRule,
                    parameterMap = ::dummyRule.valueParameters.associateWith { fooInstance }
                )
            )

            assertTrue("There were no findings which is unexpected") { findings.isNotEmpty() }

            val failFindings = findings.filter { it.kind == Finding.Kind.Fail }
            assertEquals(2, failFindings.size, "Found ${failFindings.size} violation(s) instead of two violations")
        }
    }

    @Test
    fun `test never with no violations`() {
        val fooInstance = FooModel()

        val backend = CokoCpgBackend(config = createCpgConfiguration(passFile))

        with(backend) {
            // Evaluator does not allow calls to `first` with -1 or a number between 1230 and 1240
            val evaluator = never(
                fooInstance.first(-1),
                fooInstance.first(1230..1240)
            )
            val findings = evaluator.evaluate(
                EvaluationContext(
                    rule = ::dummyRule,
                    parameterMap = ::dummyRule.valueParameters.associateWith { fooInstance }
                )
            )

            assertTrue("There were no findings which is unexpected") { findings.isNotEmpty() }

            assertTrue("Not all findings are passes which is unexpected: ${findings.joinToString()}") {
                findings.all { it.kind == Finding.Kind.Pass }
            }

            assertEquals(4, findings.size, "Found ${findings.size} finding(s) instead of one pass finding")
        }
    }

    companion object {

        lateinit var violationFile: Path
        lateinit var passFile: Path

        @BeforeAll
        @JvmStatic
        fun startup() {
            val classLoader = OnlyEvaluationTest::class.java.classLoader

            val violationFileResource = classLoader.getResource("NeverEvaluationTest/NeverViolation.java")
            assertNotNull(violationFileResource)
            violationFile = violationFileResource.toURI().toPath()

            val passFileResource = classLoader.getResource("NeverEvaluationTest/NeverPass.java")
            assertNotNull(passFileResource)
            passFile = passFileResource.toURI().toPath()
        }
    }
}
