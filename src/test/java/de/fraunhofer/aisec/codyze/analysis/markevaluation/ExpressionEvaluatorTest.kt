package de.fraunhofer.aisec.codyze.analysis.markevaluation

import de.fraunhofer.aisec.codyze.analysis.AnalysisContext
import de.fraunhofer.aisec.codyze.analysis.MarkContextHolder
import de.fraunhofer.aisec.codyze.analysis.ServerConfiguration
import de.fraunhofer.aisec.codyze.analysis.passes.EdgeCachePass
import de.fraunhofer.aisec.codyze.crymlin.AbstractTest
import de.fraunhofer.aisec.codyze.markmodel.Mark
import de.fraunhofer.aisec.codyze.markmodel.MarkModelLoader
import de.fraunhofer.aisec.cpg.ExperimentalGraph
import de.fraunhofer.aisec.cpg.TranslationManager
import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.*
import de.fraunhofer.aisec.cpg.graph.declarations.VariableDeclaration
import de.fraunhofer.aisec.cpg.graph.statements.expressions.ConstructExpression
import de.fraunhofer.aisec.mark.markDsl.EntityDeclaration
import de.fraunhofer.aisec.mark.markDsl.MarkModel
import de.fraunhofer.aisec.mark.markDsl.RuleDeclaration
import de.fraunhofer.aisec.mark.markDsl.impl.*
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

@OptIn(ExperimentalGraph::class)
class ExpressionEvaluatorTest : AbstractTest() {

    @Test
    fun testFindInstancesForEntities() {
        val evaluator = Evaluator(mark, ServerConfiguration.builder().build())

        // assignCallsToOps needs to be called first. otherwise, the matching from rule to nodes is
        // empty
        evaluator.assignCallsToOps(graph)

        // this should populate the ops map
        val entity = mark.getEntity("MyClass")
        val op = entity.getOp("init")
        assertNotNull(op)

        val stmt = op.statements.firstOrNull()
        assertNotNull(stmt)

        val nodes = op.statementsToNodes[stmt]
        assertNotNull(nodes)
        // we should hopefully find our construct expression
        assertTrue(nodes.any { it is ConstructExpression && it.type.name == "MyClass" })

        val instances = evaluator.findInstancesForEntities(mark.rules.first())

        // there should be 1 instance / node pair
        assertEquals(1, instances.size)

        val pair = instances.firstOrNull()?.firstOrNull()
        assertNotNull(pair)

        assertEquals("a", pair.first)

        val node = pair.second
        assertTrue(node is VariableDeclaration)
        assertEquals("a", node.name)

        assertNotNull(instances)
    }

    @Test
    fun test() {
        val markContextHolder = MarkContextHolder()
        markContextHolder.allContexts[0] =
            null // add a dummy, so that we get exactly one result back for this context

        val resultCtx = AnalysisContext(File(""), graph)
        val convertedMark = MarkModelLoader().load(mapOf("" to model))
        val convertedRule = convertedMark.rules.first()
        val convertedEntity = convertedMark.getEntity("MyClass")
        val eval =
            ExpressionEvaluator(
                graph,
                convertedMark,
                convertedRule,
                resultCtx,
                ServerConfiguration.builder().build(),
                markContextHolder
            )

        val result = eval.evaluateExpression(convertedRule.statement.ensure.exp)
        println(result)
    }

    companion object {
        lateinit var graph: Graph
        lateinit var model: MarkModel
        lateinit var mark: Mark

        @BeforeAll
        @JvmStatic
        fun setup() {
            var a: VariableDeclaration? = null

            val result = TranslationResult(TranslationManager.builder().build())
            val tu = tu {
                function("main") {
                    body {
                        declare {
                            a = variable("a", type("MyClass")) { new { construct("MyClass") } }
                        }
                        assign(lhs = ref("a", a), rhs = literal(1))
                    }
                }
            }
            result.addTranslationUnit(tu)

            val pass = EdgeCachePass()
            pass.accept(result)

            graph = result.graph

            var myEntity: EntityDeclaration? = null
            var myRule: RuleDeclaration? = null

            model =
                mark {
                    myEntity = entity("MyClass") { op("init") { stmt { call("MyClass") } } }
                    myRule =
                        rule("myRule") {
                            statement {
                                using(myEntity!!, "a")
                                ensure {
                                    comparison(left = operand("a"), op = "==", right = lit("1"))
                                }
                            }
                        }
                }

            assertNotNull(myRule)
            assertNotNull(myEntity)

            mark = MarkModelLoader().load(mapOf("" to model))
        }
    }
}
