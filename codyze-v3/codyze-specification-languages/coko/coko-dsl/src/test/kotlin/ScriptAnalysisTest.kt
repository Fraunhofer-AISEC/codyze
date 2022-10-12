package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_dsl

import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.dsl.Rule
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.dsl.Wildcard
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_dsl.host.CokoExecutor
import io.mockk.mockk
import java.nio.file.Path
import kotlin.io.path.writeText
import kotlin.reflect.full.createType
import kotlin.reflect.full.findAnnotation
import kotlin.script.experimental.api.valueOrThrow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.jupiter.api.io.TempDir

class ScriptAnalysisTest {
    @Test
    fun `test basic type creation`(@TempDir tempDir: Path) {
        val modelDefinitionFile = tempDir.resolve("model.codyze.kts")
        modelDefinitionFile.writeText(
            """
                interface TestInterface {
                    fun log(message: String)
                }
            """.trimIndent()
        )
        val specEvaluator =
            CokoExecutor.compileScriptsIntoSpecEvaluator(mockk(), listOf(modelDefinitionFile))
        assertTrue(specEvaluator.types.size == 1)
        assertTrue(specEvaluator.types[0].first.simpleName == "TestInterface")
    }

    @Test
    fun `test default imports`(@TempDir tempDir: Path) {
        val modelDefinitionFile = tempDir.resolve("model.codyze.kts")
        modelDefinitionFile.writeText(
            """
                // Wildcard is a default import
                interface TestInterface {
                    fun log(message: String) = Wildcard
                }
            """.trimIndent()
        )
        val specEvaluator =
            CokoExecutor.compileScriptsIntoSpecEvaluator(mockk(), listOf(modelDefinitionFile))
        assertTrue(specEvaluator.types.size == 1)
        assertEquals(
            specEvaluator.types[0].first.members.first().returnType,
            Wildcard::class.createType()
        )
    }

    @Test
    fun `test implicit receivers`(@TempDir tempDir: Path) {
        val modelDefinitionFile = tempDir.resolve("model.codyze.kts")
        modelDefinitionFile.writeText(
            """
                // callFqn & flowsTo is a method of an implicit receiver
                class TestImpl {
                    fun log(message: String) = 
                        callFqn("logging.info") {
                            message flowsTo arguments[0]
                        }
                }
            """.trimIndent()
        )
        val specEvaluator =
            CokoExecutor.compileScriptsIntoSpecEvaluator(mockk(), listOf(modelDefinitionFile))
        assertTrue(specEvaluator.implementations.size == 1)
    }

    @Test
    fun `test import annotation`(@TempDir tempDir: Path) {
        val modelDefinitionFile = tempDir.resolve("model.codyze.kts")
        modelDefinitionFile.writeText(
            """
                interface TestConcept {
                    fun log(message: String)
                }
            """.trimIndent()
        )

        val result =
            CokoExecutor.eval(
                """
                    @file:Import("${modelDefinitionFile.toAbsolutePath()}")

                    class TestImpl: TestConcept {
                        override fun log(message: String) { }
                    }
                """.trimIndent(),
                mockk(),
            )

        result.valueOrThrow()
    }

    @Test
    fun `test multiple spec files`(@TempDir tempDir: Path) {
        val modelDefinitionFile = tempDir.resolve("model.codyze.kts")
        modelDefinitionFile.writeText(
            """
                interface TestConcept {
                    fun log(message: String)
                }
            """.trimIndent()
        )

        val implementationFile = tempDir.resolve("implementation.codyze.kts")
        implementationFile.writeText(
            """
                @file:Import("${modelDefinitionFile.toAbsolutePath()}")
    
                class TestImpl: TestConcept {
                    override fun log(message: String) { }
                }
            """.trimIndent(),
        )

        val specEvaluator =
            CokoExecutor.compileScriptsIntoSpecEvaluator(
                mockk(),
                listOf(modelDefinitionFile, implementationFile),
            )

        assertTrue(specEvaluator.types.size == 1)
        assertTrue(specEvaluator.implementations.size == 1)
    }

    @Test
    fun `test rule annotation`(@TempDir tempDir: Path) {
        val modelDefinitionFile = tempDir.resolve("model.codyze.kts")
        modelDefinitionFile.writeText(
            """
                @Rule("Some description")
                fun `this is a test rule`() { }
                
                fun notARule() { }
            """.trimIndent()
        )
        val specEvaluator =
            CokoExecutor.compileScriptsIntoSpecEvaluator(mockk(), listOf(modelDefinitionFile))

        assertTrue(specEvaluator.rules.size == 1)
        assertEquals(
            specEvaluator.rules.single().first.findAnnotation<Rule>()!!.description,
            "Some description"
        )
    }

    @Test
    fun `test default imports, implicit receivers, import annotation and rule annotation at once`(
        @TempDir tempDir: Path
    ) {
        val modelDefinitionFile = tempDir.resolve("model.codyze.kts")
        modelDefinitionFile.writeText(
            """
                interface TestConcept {
                    fun log(message: String): Nodes
                }
            """.trimIndent()
        )

        val implementationFile = tempDir.resolve("implementation.codyze.kts")
        implementationFile.writeText(
            """
                @file:Import("${modelDefinitionFile.toAbsolutePath()}")
    
                class TestImpl: TestConcept {
                    override fun log(message: String) = variable(message)
                }
                
                @Rule("Some description")
                fun `some rule`(impl: TestConcept) = impl.log("first") follows impl.log("second")
            """.trimIndent(),
        )

        val specEvaluator =
            CokoExecutor.compileScriptsIntoSpecEvaluator(
                mockk(),
                listOf(modelDefinitionFile, implementationFile),
            )

        assertTrue(specEvaluator.rules.size == 1)
        assertEquals(
            specEvaluator.rules.single().first.findAnnotation<Rule>()!!.description,
            "Some description"
        )
        assertTrue(specEvaluator.types.size == 1)
        assertTrue(specEvaluator.implementations.size == 1)
    }
}
