package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_dsl

import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.Action
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.Project
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.Task
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_dsl.host.CokoExecutor
import io.mockk.*
import kotlin.script.experimental.api.valueOrThrow
import org.junit.jupiter.api.Test

class CokoScriptHostTest {

    @Test
    fun `it works`() {

        val task = mockk<Task>()
        val project = mockk<Project>()

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
                project
            )
        result.valueOrThrow()

        verifySequence {
            project.task("make an apple pie", any())
            task.dependsOn("invent the universe")
            task.perform(any())
        }
    }

//    @Test
//    fun `test basic type creation`() {
//        val project = mockk<Project>()
//
//        val result =
//            CokoExecutor.eval("""
//                interface Logging: Concept {
//                    fun log(message: String, varargs: Any)
//                }
//
//                interface ObjectRelationalMapper: Concept {
//                    fun insert(`object`: Any)
//                }
//
//                interface UserContext: Concept {
//                    val user: Any
//                }
//            """.trimIndent(),
//                project
//            )
//        result.valueOrThrow()
//    }
//
//    @Test
//    fun `test import annotation`() {
//        val project = mockk<Project>()
//
//        val result =
//            CokoExecutor.eval("""
//                @file:Import("model.codyze.kts")
//
//                class PythonLogging: Logging {
//                    override fun log(message: String, varargs: Any) = call("logging.info(...)")
//                }
//            """.trimIndent(),
//                project
//            )
//
//        result.valueOrThrow()
//    }
}
