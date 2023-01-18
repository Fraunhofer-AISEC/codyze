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
import de.fraunhofer.aisec.codyze.backends.cpg.coko.evaluators.FollowsEvaluator
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

class FollowsEvaluationTest {

    class FooModel {
        fun first() = op {
            definition("Foo.first") {
                signature()
            }
        }

        fun f2() = op {}
    }

    class BarModel {
        fun second() = op {
            definition("Bar.second") {
                signature()
            }
        }
    }

    @Test
    fun `test simple follows`() {
        val fooInstance = FooModel()
        val barInstance = BarModel()

        val backend = CokoCpgBackend(config = createCpgConfiguration(testFile))

        with(backend) {
            val evaluator = FollowsEvaluator(fooInstance.first(), barInstance.second())
            val findings = evaluator.evaluate(
                EvaluationContext(
                    rule = ::dummyRule,
                    parameterMap = ::dummyRule.valueParameters.associateWith { listOf(fooInstance, barInstance) }
                )
            )

            assertTrue("There were no findings which is unexpected") { findings.isNotEmpty() }

            val failFindings = findings.filter { it.kind == Finding.Kind.Fail }
            assertEquals(1, failFindings.size, "Found ${failFindings.size} violation(s) instead of one violation")
        }
    }

    companion object {

        lateinit var testFile: Path

        @BeforeAll
        @JvmStatic
        fun startup() {
            val classLoader = FollowsEvaluationTest::class.java.classLoader

            val testFileResource = classLoader.getResource("FollowsEvaluationTest/SimpleFollows.java")
            assertNotNull(testFileResource)
            testFile = Path(testFileResource.path)
        }
    }
}
