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
package de.fraunhofer.aisec.codyze.specificationLanguages.coko.dsl

import de.fraunhofer.aisec.codyze.backends.cpg.CPGConfiguration
import de.fraunhofer.aisec.codyze.backends.cpg.coko.CokoCpgBackend
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.CokoRule
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.Evaluator
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.Severity
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.toResultLevel
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.dsl.host.CokoExecutor
import de.fraunhofer.aisec.cpg.passes.EdgeCachePass
import de.fraunhofer.aisec.cpg.passes.UnreachableEOGPass
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.io.path.Path
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.KTypeParameter
import kotlin.reflect.KVisibility

class CokoSarifBuilderTest {

    private val cpgConfiguration =
        CPGConfiguration(
            source = emptyList(),
            useUnityBuild = false,
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
            passes = listOf(UnreachableEOGPass::class, EdgeCachePass::class),
        )

    val cokoRulewithoutRuleAnnotation = object : CokoRule {
        override val annotations: List<Annotation>
            get() = emptyList()
        override val isAbstract: Boolean
            get() = TODO("Not yet implemented")
        override val isExternal: Boolean
            get() = TODO("Not yet implemented")
        override val isFinal: Boolean
            get() = TODO("Not yet implemented")
        override val isInfix: Boolean
            get() = TODO("Not yet implemented")
        override val isInline: Boolean
            get() = TODO("Not yet implemented")
        override val isOpen: Boolean
            get() = TODO("Not yet implemented")
        override val isOperator: Boolean
            get() = TODO("Not yet implemented")
        override val isSuspend: Boolean
            get() = TODO("Not yet implemented")
        override val name: String
            get() = "norule"
        override val parameters: List<KParameter>
            get() = TODO("Not yet implemented")
        override val returnType: KType
            get() = TODO("Not yet implemented")
        override val typeParameters: List<KTypeParameter>
            get() = TODO("Not yet implemented")
        override val visibility: KVisibility?
            get() = TODO("Not yet implemented")

        override fun call(vararg args: Any?): Evaluator {
            TODO("Not yet implemented")
        }

        override fun callBy(args: Map<KParameter, Any?>): Evaluator {
            TODO("Not yet implemented")
        }
    }

    @Test
    fun `test empty rules list causes empty reportingDescriptors list`() {
        val backend = CokoCpgBackend(cpgConfiguration)
        val csb = CokoSarifBuilder(rules = emptyList(), backend = backend)

        assertTrue(csb.reportingDescriptors.isEmpty())
    }

    @Test
    fun `test spec without rule annotation`() {
        val backend = CokoCpgBackend(cpgConfiguration)
        val csb = CokoSarifBuilder(rules = listOf(cokoRulewithoutRuleAnnotation), backend = backend)

        val reportingDescriptor = csb.reportingDescriptors.first()
        assertNotNull(reportingDescriptor)
        assertNull(reportingDescriptor.shortDescription)
        assertNull(reportingDescriptor.fullDescription)
        assertNull(reportingDescriptor.defaultConfiguration)
        assertNull(reportingDescriptor.help)
        assertNull(reportingDescriptor.properties)
    }

    @Test
    fun `test rule with default shortDescription`() {
        val specFiles = listOfNotNull(
            CokoSarifBuilderTest::class.java.classLoader
                .getResource("sarif/ruledefaults.codyze.kts")
        ).map { Path(it.path) }

        val backend = CokoCpgBackend(cpgConfiguration)
        val specEvaluator = CokoExecutor.compileScriptsIntoSpecEvaluator(backend = backend, specFiles = specFiles)
        val csb = CokoSarifBuilder(rules = specEvaluator.rules, backend = backend)

        val shortDescription = csb.reportingDescriptors.first().shortDescription
        assertNotNull(shortDescription)
        assertTrue(shortDescription?.text!!.isEmpty())
    }

    @Test
    fun `test rule with some shortDescription`() {
        val specFiles = listOfNotNull(
            CokoSarifBuilderTest::class.java.classLoader
                .getResource("sarif/ruleshortdescription.codyze.kts")
        ).map { Path(it.path) }

        val backend = CokoCpgBackend(cpgConfiguration)
        val specEvaluator = CokoExecutor.compileScriptsIntoSpecEvaluator(backend = backend, specFiles = specFiles)
        val csb = CokoSarifBuilder(rules = specEvaluator.rules, backend = backend)

        val shortDescription = csb.reportingDescriptors.first().shortDescription
        assertNotNull(shortDescription)
        assertEquals(shortDescription?.text, "test")
    }

    @Test
    fun `test rule with default description`() {
        val specFiles = listOfNotNull(
            CokoSarifBuilderTest::class.java.classLoader
                .getResource("sarif/ruledefaults.codyze.kts")
        ).map { Path(it.path) }

        val backend = CokoCpgBackend(cpgConfiguration)
        val specEvaluator = CokoExecutor.compileScriptsIntoSpecEvaluator(backend = backend, specFiles = specFiles)
        val csb = CokoSarifBuilder(rules = specEvaluator.rules, backend = backend)

        val fullDescription = csb.reportingDescriptors.first().fullDescription
        assertNotNull(fullDescription)
        assertTrue(fullDescription?.text!!.isEmpty())
    }

    @Test
    fun `test rule with some description`() {
        val specFiles = listOfNotNull(
            CokoSarifBuilderTest::class.java.classLoader
                .getResource("sarif/rulefulldescription.codyze.kts")
        ).map { Path(it.path) }

        val backend = CokoCpgBackend(cpgConfiguration)
        val specEvaluator = CokoExecutor.compileScriptsIntoSpecEvaluator(backend = backend, specFiles = specFiles)
        val csb = CokoSarifBuilder(rules = specEvaluator.rules, backend = backend)

        val fullDescription = csb.reportingDescriptors.first().fullDescription
        assertNotNull(fullDescription)
        assertEquals(fullDescription?.text, "some description")
    }

    @Test
    fun `test rule with default severity`() {
        val specFiles = listOfNotNull(
            CokoSarifBuilderTest::class.java.classLoader
                .getResource("sarif/ruledefaults.codyze.kts")
        ).map { Path(it.path) }

        val backend = CokoCpgBackend(cpgConfiguration)
        val specEvaluator = CokoExecutor.compileScriptsIntoSpecEvaluator(backend = backend, specFiles = specFiles)
        val csb = CokoSarifBuilder(rules = specEvaluator.rules, backend = backend)

        val defaultConfiguration = csb.reportingDescriptors.first().defaultConfiguration
        assertNotNull(defaultConfiguration)

        val level = defaultConfiguration?.level
        assertNotNull(level)
        assertTrue(level == Severity.WARNING.toResultLevel())
    }

    @Test
    fun `test rule with some severity`() {
        val specFiles = listOfNotNull(
            CokoSarifBuilderTest::class.java.classLoader
                .getResource("sarif/ruleseverity.codyze.kts")
        ).map { Path(it.path) }

        val backend = CokoCpgBackend(cpgConfiguration)
        val specEvaluator = CokoExecutor.compileScriptsIntoSpecEvaluator(backend = backend, specFiles = specFiles)
        val csb = CokoSarifBuilder(rules = specEvaluator.rules, backend = backend)

        val defaultConfiguration = csb.reportingDescriptors.first().defaultConfiguration
        assertNotNull(defaultConfiguration)

        val level = defaultConfiguration?.level
        assertNotNull(level)
        assertTrue(level != Severity.WARNING.toResultLevel())
    }

    @Test
    fun `test rule with default help`() {
        val specFiles = listOfNotNull(
            CokoSarifBuilderTest::class.java.classLoader
                .getResource("sarif/ruledefaults.codyze.kts")
        ).map { Path(it.path) }

        val backend = CokoCpgBackend(cpgConfiguration)
        val specEvaluator = CokoExecutor.compileScriptsIntoSpecEvaluator(backend = backend, specFiles = specFiles)
        val csb = CokoSarifBuilder(rules = specEvaluator.rules, backend = backend)

        val help = csb.reportingDescriptors.first().help
        assertNotNull(help)
        assertTrue(help?.text!!.isEmpty())
    }

    @Test
    fun `test rule with some help`() {
        val specFiles = listOfNotNull(
            CokoSarifBuilderTest::class.java.classLoader
                .getResource("sarif/rulehelp.codyze.kts")
        ).map { Path(it.path) }

        val backend = CokoCpgBackend(cpgConfiguration)
        val specEvaluator = CokoExecutor.compileScriptsIntoSpecEvaluator(backend = backend, specFiles = specFiles)
        val csb = CokoSarifBuilder(rules = specEvaluator.rules, backend = backend)

        val help = csb.reportingDescriptors.first().help
        assertNotNull(help)
        assertEquals(help?.text, "some help")
    }

    @Test
    fun `test rule with default empty tags`() {
        val specFiles = listOfNotNull(
            CokoSarifBuilderTest::class.java.classLoader
                .getResource("sarif/ruledefaults.codyze.kts")
        ).map { Path(it.path) }

        val backend = CokoCpgBackend(cpgConfiguration)
        val specEvaluator = CokoExecutor.compileScriptsIntoSpecEvaluator(backend = backend, specFiles = specFiles)
        val csb = CokoSarifBuilder(rules = specEvaluator.rules, backend = backend)

        val alternative = csb.reportingDescriptors.first().properties?.tags?.let { assertTrue(it.isEmpty()) }
        assertNotNull(alternative)
    }

    @Test
    fun `test rules with some tags`() {
        val specFiles = listOfNotNull(
            CokoSarifBuilderTest::class.java.classLoader
                .getResource("sarif/ruletags.codyze.kts")
        ).map { Path(it.path) }

        val backend = CokoCpgBackend(cpgConfiguration)
        val specEvaluator = CokoExecutor.compileScriptsIntoSpecEvaluator(backend = backend, specFiles = specFiles)
        val csb = CokoSarifBuilder(rules = specEvaluator.rules, backend = backend)

        val alternative = csb.reportingDescriptors.first().properties?.tags?.let { assertTrue(it.isNotEmpty()) }
        assertNotNull(alternative)
    }
}
