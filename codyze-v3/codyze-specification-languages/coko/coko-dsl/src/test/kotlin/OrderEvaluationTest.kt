// package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_dsl
//
// import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.ordering.DFA
// import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.ordering.NFA
// import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.ordering.Order
// import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_dsl.host.CokoExecutor
// import io.mockk.mockk
// import java.nio.file.Path
// import kotlin.io.path.writeText
// import kotlin.test.Test
// import org.junit.jupiter.api.BeforeAll
// import org.junit.jupiter.api.io.TempDir
//
// class OrderEvaluationTest {
//
//    private inline fun orderExpressionToDfa(block: Order.() -> Unit): DFA {
//        val order = Order().apply(block)
//        return order.toNfa().toDfa()
//    }
//
//    @Test
//    /**
//     * Tests a simple sequence order.
//     *
//     * The NFA can be converted to .dot format using: [NFA.toDotString].
//     */
//    fun `test simple sequence order`() {
//        val implementationFile = tempDir.resolve("implementation.codyze.kts")
//        implementationFile.writeText(
//            """
//                @file:Import("${modelDefinitionFile.toAbsolutePath()}")
//
//                class TestImpl: TestConcept {
//                    override fun log(message: String) { }
//                }
//            """.trimIndent(),
//        )
//
//        val specEvaluator =
//            CokoExecutor.compileScriptsIntoSpecEvaluator(
//                mockk(),
//                listOf(modelDefinitionFile, implementationFile),
//            )
//    }
//
//    companion object {
//        @field:TempDir lateinit var tempDir: Path
//
//        @BeforeAll
//        @JvmStatic
//        internal fun `create test concept and implementation`(): Unit {
//            val baseScript = tempDir.resolve("base.codyze.kts")
//            baseScript.writeText(
//                """
//                    interface TestConcept {
//                        fun create()
//                        fun init()
//                        fun start()
//                        fun process()
//                        fun finish()
//                        fun reset()
//                    }
//
//                    class TestImpl : TestConcept {
//                        override fun init() = {}
//                        override fun create() = {}
//                        override fun start() = {}
//                        override fun process() = {}
//                        override fun finish() = {}
//                        override fun reset() = {}
//                    }
//                """.trimIndent()
//            )
//        }
//    }
// }
