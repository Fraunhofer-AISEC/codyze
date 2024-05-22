package de.fraunhofer.aisec.codyze.backends.cpg


import de.fraunhofer.aisec.codyze.backends.cpg.coko.CokoCpgBackend
import de.fraunhofer.aisec.codyze.backends.cpg.coko.CpgFinding
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.EvaluationContext
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.Finding
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.definition
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.op
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.dsl.signature
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.scopes.FunctionScope
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.nio.file.Path
import kotlin.io.path.toPath
import kotlin.reflect.full.valueParameters
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ArgumentEvaluationTest {
    @Suppress("UNUSED")
    class FooModel {
        fun strong() = op {
            definition("Foo.strong") {
                signature()
            }
        }

        fun weak() = op {
            definition("Foo.weak") {
                signature()
            }
        }
    }

    class BarModel {
        fun critical(foundation: Any?) = op {
            definition("Bar.critical") {
                signature(foundation)
            }
        }
    }

    @Test
    fun `test simple argument pass`() {
        val okFindings = ArgumentEvaluationTest.findings.filter { it.kind == Finding.Kind.Pass }
        for (finding in okFindings) {
            // pass finding has to be in function that has "ok" in its name
            assertTrue("Found PASS finding that was from function ${finding.node?.getFunction()} -> false negative") {
                finding.node?.getFunction()?.contains(Regex(".*ok.*", RegexOption.IGNORE_CASE)) == true
            }
        }
    }

    @Test
    fun `test simple argument fail`() {
        val failFindings = ArgumentEvaluationTest.findings.filter { it.kind == Finding.Kind.Fail }
        for (finding in failFindings) {
            // fail finding should not be in function that has "ok" in its name
            assertFalse("Found FAIL finding that was from function ${finding.node?.getFunction()} -> false positive") {
                finding.node?.getFunction()?.contains(Regex(".*ok.*", RegexOption.IGNORE_CASE)) == true
            }

            // fail finding should not be in function that has "noFinding" in its name
            assertFalse("Found FAIL finding that was from function ${finding.node?.getFunction()} -> false positive") {
                finding.node?.getFunction()?.contains(Regex(".*noFinding.*", RegexOption.IGNORE_CASE)) == true
            }
        }
    }

    @Test
    fun `test simple argument not applicable`() {
        val notApplicableFindings = ArgumentEvaluationTest.findings.filter { it.kind == Finding.Kind.NotApplicable }
        for (finding in notApplicableFindings) {
            // notApplicable finding has to be in function that has "notApplicable" in its name
            assertTrue(
                "Found NotApplicable finding that was from function ${finding.node?.getFunction()} -> false negative"
            ) {
                finding.node?.getFunction()?.contains(Regex(".*notApplicable.*", RegexOption.IGNORE_CASE)) == true
            }
        }
    }

    private fun Node.getFunction(): String? {
        var scope = this.scope
        while (scope != null) {
            if (scope is FunctionScope) {
                return scope.astNode?.name?.localName
            }
            scope = scope.parent
        }
        return null
    }

    companion object {

        private lateinit var testFile: Path
        lateinit var findings: List<CpgFinding>

        @BeforeAll
        @JvmStatic
        fun startup() {
            val classLoader = ArgumentEvaluationTest::class.java.classLoader

            val testFileResource = classLoader.getResource("ArgumentEvaluationTest/SimpleArgument.java")
            assertNotNull(testFileResource)
            testFile = testFileResource.toURI().toPath()

            val fooInstance = ArgumentEvaluationTest.FooModel()
            val barInstance = ArgumentEvaluationTest.BarModel()

            val backend = CokoCpgBackend(config = createCpgConfiguration(testFile))

            with(backend) {
                val evaluator = argumentOrigin(barInstance::critical, 0, fooInstance::strong)
                findings = evaluator.evaluate(
                    EvaluationContext(
                        rule = ::dummyRule,
                        parameterMap = ::dummyRule.valueParameters.associateWith { listOf(fooInstance, barInstance) }
                    )
                )
            }
            assertTrue("There were no findings which is unexpected") { findings.isNotEmpty() }
        }
    }
}