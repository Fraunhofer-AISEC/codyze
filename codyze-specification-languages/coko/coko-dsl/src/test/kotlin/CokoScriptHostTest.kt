package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_dsl

import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_dsl.host.CokoExecutor
import io.mockk.mockk
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.writeText
import kotlin.script.experimental.api.valueOrThrow
import kotlin.test.Test

/** Tests whether the basic functionality of [CokoScript] works. */
class CokoScriptHostTest {

    @Test
    fun `test basic type creation`() =
        assertAll({
            CokoExecutor.eval(
                """
                interface TestInterface {
                    fun log(message: String)
                }
                """.trimIndent(),
                backend = mockk()
            )
                .valueOrThrow()
        })

    @Test
    fun `test default imports`() =
        assertAll({
            CokoExecutor.eval(
                """
                // Wildcard is a default import
                interface TestInterface {
                    fun log(message: String) = Wildcard
                }
                """.trimIndent(),
                mockk()
            )
                .valueOrThrow()
        })

    @Test
    fun `test implicit receivers`() =
        assertAll({
            CokoExecutor.eval(
                """
                // op & definition is a method of an implicit receiver
                class TestImpl {
                    fun log(message: String) = 
                        op {
                            definition("") {}
                        }
                }
                """.trimIndent(),
                mockk()
            )
                .valueOrThrow()
        })

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

        assertAll({
            CokoExecutor.eval(
                """
                    @file:Import("${modelDefinitionFile.toAbsolutePath()}")

                    class TestImpl: TestConcept {
                        override fun log(message: String) { }
                    }
                """.trimIndent(),
                mockk(),
            )
                .valueOrThrow()
        })
    }
}
