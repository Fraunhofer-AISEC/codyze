package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_dsl

import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.Action
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.Project
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.Task
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_dsl.host.CPGEvaluator
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_dsl.host.CokoExecutor
import io.mockk.*
import java.nio.file.Path
import kotlin.io.path.writeText
import kotlin.script.experimental.api.valueOrThrow
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class CokoScriptHostTest {

    // test from groddler
    @Test
    fun `it works`() {

        val task = mockk<Task>()
        val project = mockk<Project>()
        val cpgEvaluator = mockk<CPGEvaluator>()

        every { task.dependsOn(any()) } just Runs
        every { task.perform(any()) } just Runs

        every { project.task(any(), captureLambda()) } answers
            {
                // force task configuration block to run
                lambda<Action<Task>>().invoke(task)
            }

        val result =
            CokoExecutor.eval(
                """
                    plugins { id("kotlin") } 
                    kotlin { isAwesome = true }
                    task("make an apple pie") {
                        dependsOn("invent the universe")
                        perform { println("🥧") }
                    }
                """,
                project,
                cpgEvaluator
            )
        result.valueOrThrow()

        verifySequence {
            project.task("make an apple pie", any())
            task.dependsOn("invent the universe")
            task.perform(any())
        }
    }

    @Test
    fun `test basic type creation`() {
        val project = mockk<Project>()
        val cpgEvaluator = mockk<CPGEvaluator>()

        val result =
            CokoExecutor.eval(
                """
                    interface Logging {
                        fun log(message: String, varargs: Any)
                    }
                """.trimIndent(),
                project,
                cpgEvaluator
            )
        result.valueOrThrow()
    }

    @Test
    fun `test default imports`() {
        val project = mockk<Project>()
        val cpgEvaluator = mockk<CPGEvaluator>()

        val result =
            CokoExecutor.eval(
                """
                    // Concept is a default import
                    interface Logging {
                        fun log(message: String, varargs: Any) = Unit
                    }
                """.trimIndent(),
                project,
                cpgEvaluator
            )
        result.valueOrThrow()
    }

    @Test
    fun `test implicit receivers`() {
        val project = mockk<Project>()
        val cpgEvaluator = mockk<CPGEvaluator>()

        val result =
            CokoExecutor.eval(
                """
                    // call is a method of an implicit receiver
                    class Logging {
                        fun log(message: String, varargs: Any) = call("logging.info(...)")
                    }
                """.trimIndent(),
                project,
                cpgEvaluator
            )
        result.valueOrThrow()
    }
    @Test
    fun `test import annotation`(@TempDir tempDir: Path) {
        val modelDefinitionFile = tempDir.resolve("model.codyze.kts")
        modelDefinitionFile.writeText(
            """
                interface Logging {
                    fun log(message: String, varargs: Any)
                }
            """.trimIndent()
        )

        val project = mockk<Project>()
        val cpgEvaluator = mockk<CPGEvaluator>()

        val result =
            CokoExecutor.eval(
                """
                    @file:Import("${modelDefinitionFile.toAbsolutePath()}")
    
                    class PythonLogging: Logging {
                        override fun log(message: String, varargs: Any) = Unit
                    }
                """.trimIndent(),
                project,
                cpgEvaluator
            )

        result.valueOrThrow()
    }

    @Test
    fun `test default imports, import annotation and implicit receivers at once`(
        @TempDir tempDir: Path
    ) {
        val modelDefinitionFile = tempDir.resolve("model.codyze.kts")
        modelDefinitionFile.writeText(
            """
                interface Logging {
                    fun log(message: String, varargs: Any): Any
                }
            """.trimIndent()
        )

        val project = mockk<Project>()
        val cpgEvaluator = mockk<CPGEvaluator>()

        val result =
            CokoExecutor.eval(
                """
                    @file:Import("${modelDefinitionFile.toAbsolutePath()}")
    
                    class PythonLogging: Logging {
                        override fun log(message: String, varargs: Any) = call("test") `is` Called
                    }
                """.trimIndent(),
                project,
                cpgEvaluator
            )

        result.valueOrThrow()
    }

    @Test
    fun `test super simple rule evaluation`() {
        val project = mockk<Project>()
        val cpgEvaluator = mockk<CPGEvaluator>()

        val result =
            CokoExecutor.eval(
                """
                    interface Logging {
                        fun log(message: String, varargs: Any)
                    }

                    class LoggingImpl : Logging {
                        override fun log(message: String, varargs: Any) = call("logging.info(*)")
                    }
                    
                    @Rule
                    fun `log is called`(log: Logging) {
                        call(log::log) `is` Called
                        // provide cpgWrapper as implicit receiver
                        // expands to cpgEvaluator.call("logging.info(*)") `is` Called
                    }
                """.trimIndent(),
                project,
                cpgEvaluator
            )

        result.valueOrThrow()
    }
}
