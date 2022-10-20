//import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.Project
//import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.dsl.*
//import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.ordering.Order
//import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.ordering.OrderFragment
//import de.fraunhofer.aisec.codyze.specification_languages.coko.coko_core.ordering.evaluateOrder
//import de.fraunhofer.aisec.codyze_core.config.Configuration
//import de.fraunhofer.aisec.cpg.TranslationManager
//import de.fraunhofer.aisec.cpg.analysis.fsm.DFA
//import java.nio.file.Path
//import kotlin.io.path.Path
//import kotlin.reflect.KFunction
//import kotlin.test.Test
//
///**
// * Tests the order evaluation starting from a coko order expression.
// *
// * If this test fails, make sure that the following tests work first as the functionality they test
// * is needed for this test:
// * - [NfaDfaConstructionTest]
// */
//class OrderEvaluationTest {
//    context(Project)
//    class CokoOrderImpl {
//        fun getOrderNodes() = op {  }
//        fun init() = op { +definition("Botan.set_key") { +signature(Wildcard) } }
//        fun start() = op { +definition("Botan.start") { +signature(Wildcard) } }
//        fun finish() = op { +definition("Botan.finish") { +signature(Wildcard) } }
//    }
//
//    private inline fun orderExpressionToDfa(block: Order.() -> Unit): DFA {
//        val order = Order().apply(block)
//        return order.toNfa().toDfa()
//    }
//
//    context(Project)
//    private fun createSimpleDfa(testObj: CokoOrderImpl) = orderExpressionToDfa {
//        +testObj::start.use { testObj.start() }
//        +testObj::finish
//    }
//
//    // function with the same signature as the 'rule' [createSimpleDfa] because the kotlin compiler crashes
//    // when trying to create a function reference to [createSimpleDfa] using: '::createSimpleDfa' (probably because
//    // of the context receiver)
//    // this is needed as an argument to [evaluateOrder]
//    private fun dummyFunction(testObj: CokoOrderImpl) {}
//
//    private val basePath = Path("src", "test", "resources", "OrderEvaluationTest")
//
//    private fun getPath(sourceFileName: String) = basePath.resolve(sourceFileName).toAbsolutePath()
//
//    private fun createCpgConfiguration(vararg sourceFile: Path) =
//        CPGConfiguration(
//            source = listOf(*sourceFile),
//            useUnityBuild = false,
//            typeSystemActiveInFrontend = true,
//            debugParser = false,
//            disableCleanup = false,
//            codeInNodes = true,
//            matchCommentsToNodes = false,
//            processAnnotations = false,
//            failOnError = false,
//            useParallelFrontends = false,
//            defaultPasses = true,
//            additionalLanguages = setOf(),
//            symbols = mapOf(),
//            includeBlacklist = listOf(),
//            includePaths = listOf(),
//            includeWhitelist = listOf(),
//            loadIncludes = false,
//            passes = listOf()
//        )
//
//    @Test
//    fun `test simple order expression for java`() {
//        // mocking doesn't work here. We need an actual Project instance
//        val sourceFile = getPath("order.java")
//        val translationManager =
//            TranslationManager.builder()
//                .config(config = createCpgConfiguration(sourceFile).toTranslationConfiguration())
//                .build() // Initialize the CPG, based on the given Configuration
//        val cpg = translationManager.analyze().get()
//        val project = Project(cpg)
//        //cpg.functions[0].variables[0].initializer.type.typeName
//        with(project) {
//            // this is roughly what [SpecEvaluator.evaluate] does
//            val dfa = createSimpleDfa(CokoOrderImpl())
//            val orderEvaluationResult = evaluateOrder(dfa = dfa, ::dummyFunction)
//            // assertFalse(orderEvaluationResult)
//        }
//    }
//}
