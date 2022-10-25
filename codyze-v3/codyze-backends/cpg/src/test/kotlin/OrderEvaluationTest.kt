package de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.order

import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.CokoBackend
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.dsl.*
import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.ordering.Order
import de.fraunhofer.aisec.codyze_backends.cpg.CPGConfiguration
import de.fraunhofer.aisec.codyze_backends.cpg.TypestateMode
import de.fraunhofer.aisec.codyze_backends.cpg.coko.CokoCpgBackend
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.test.Test
import kotlin.test.assertFalse

/**
 * Tests the order evaluation starting from a coko order expression.
 *
 * If this test fails, make sure that the following tests work first as the functionality they test
 * is needed for this test:
 * - [NfaDfaConstructionTest]
 */
class OrderEvaluationTest {
    class CokoOrderImpl {
        fun getOrderNodes() = op {  }
        fun init() = op { +definition("Botan.set_key") { +signature(Wildcard) } }
        fun start() = op { +definition("Botan.start") { +signature(Wildcard) } }
        fun finish() = op { +definition("Botan.finish") { +signature(Wildcard) } }
    }

    private inline fun orderExpressionToOrder(block: Order.() -> Unit) = Order().apply(block)

    context(CokoBackend)
    private fun createSimpleOrder(testObj: CokoOrderImpl) = orderExpressionToOrder {
        +testObj::start.use { testObj.start() }
        +testObj::finish
    }

    // function with the same signature as the 'rule' [createSimpleDfa] because the kotlin compiler crashes
    // when trying to create a function reference to [createSimpleDfa] using: '::createSimpleOrder' (probably because
    // of the context receiver)
    // this is needed as an argument to [evaluateOrder]
    private fun dummyFunction(testObj: CokoOrderImpl): Order = TODO()

    private val basePath = Path("src", "test", "resources", "OrderEvaluationTest")

    private fun getPath(sourceFileName: String) = basePath.resolve(sourceFileName).toAbsolutePath()

    private fun createCpgConfiguration(vararg sourceFile: Path) =
        CPGConfiguration(
            source = listOf(*sourceFile),
            useUnityBuild = false,
            typeSystemActiveInFrontend = true,
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
            includeBlacklist = listOf(),
            includePaths = listOf(),
            includeWhitelist = listOf(),
            loadIncludes = false,
            passes = listOf(),
            typestate = TypestateMode.DFA
        )

    @Test
    fun `test simple order expression for java`() {
        // mocking doesn't work here. We need an actual Project instance
        val sourceFile = getPath("order.java")
        val backend = CokoCpgBackend(config = createCpgConfiguration(sourceFile))
        backend.initialize()  // Initialize the CPG, based on the given Configuration

        with(backend) {
            val order = createSimpleOrder(CokoOrderImpl())
            val orderEvaluator = evaluateOrder(order)
            assertFalse { orderEvaluator.evaluate(rule = ::dummyFunction).ruleEvaluationOutcome }
        }
    }
}
