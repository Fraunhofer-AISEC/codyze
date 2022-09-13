package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_dsl

import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.Project
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_dsl.host.CokoExecutor
import io.mockk.*
import java.nio.file.Path
import kotlin.io.path.writeText
import kotlin.script.experimental.api.valueOrThrow
import kotlin.test.Ignore
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class CokoScriptHostTest {
    @Test
    fun `test basic type creation`() {
        val project = mockk<Project>()

        val result =
            CokoExecutor.eval(
                """
                    interface TestInterface {
                        fun test(param1: String, varargs: Any)
                    }
                """.trimIndent(),
                project,
            )
        result.valueOrThrow()
    }

    @Test
    fun `test default imports`() {
        val project = mockk<Project>()

        val result =
            CokoExecutor.eval(
                """
                    // Called is a default import
                    interface TestInterface {
                        fun test(param1: String, varargs: Any) = Wildcard
                    }
                """.trimIndent(),
                project,
            )
        result.valueOrThrow()
    }

    @Test
    fun `test implicit receivers`() {
        val project = mockk<Project>()

        val result =
            CokoExecutor.eval(
                """
                    // call is a method of an implicit receiver
                    class Logging {
                        fun log(message: String, vararg args: Any) = 
                            callFqn("logging.info") {
                                message flowsTo arguments[0] && args.all { it flowsTo arguments }
                            }
                    }
                """.trimIndent(),
                project,
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

        val result =
            CokoExecutor.eval(
                """
                    @file:Import("${modelDefinitionFile.toAbsolutePath()}")

                    class PythonLogging: Logging {
                        override fun log(message: String, varargs: Any) = Unit
                    }
                """.trimIndent(),
                project,
            )

        result.valueOrThrow()
    }

    @Test
    @Ignore
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

        val result =
            CokoExecutor.eval(
                """
                    @file:Import("${modelDefinitionFile.toAbsolutePath()}")

                    class PythonLogging: Logging {
                        override fun log(message: String, varargs: Any) = variable("test") follows Wildcard
                    }
                """.trimIndent(),
                project,
            )

        result.valueOrThrow()
    }

    //    @Test
    //    fun `test super simple rule evaluation`() {
    //        val project = mockk<Project>()
    //        val cpgEvaluator = mockk<CPGEvaluator>()
    //
    //        val result =
    //            CokoExecutor.eval(
    //                """
    //                    interface Logging {
    //                        fun log(message: String, varargs: Any)
    //                    }
    //
    //                    class LoggingImpl : Logging {
    //                        override fun log(message: String, varargs: Any) =
    // call("logging.info(*)")
    //                    }
    //
    //                    @Rule
    //                    fun `log is called`(log: Logging) {
    //                        log::log `is` Called
    //                        // provide cpgWrapper as implicit receiver
    //                        // expands to cpgEvaluator.call("logging.info(*)") `is` Called
    //                    }
    //                """.trimIndent(),
    //                project,
    //                cpgEvaluator
    //            )
    //
    //        result.valueOrThrow()
    //    }
}
