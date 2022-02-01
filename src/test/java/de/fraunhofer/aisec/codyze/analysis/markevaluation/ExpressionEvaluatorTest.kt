package de.fraunhofer.aisec.codyze.analysis.markevaluation

import de.fraunhofer.aisec.codyze.analysis.AnalysisContext
import de.fraunhofer.aisec.codyze.analysis.ServerConfiguration
import de.fraunhofer.aisec.codyze.analysis.resolution.ConstantValue
import de.fraunhofer.aisec.codyze.crymlin.AbstractTest
import de.fraunhofer.aisec.codyze.markmodel.Mark
import de.fraunhofer.aisec.codyze.markmodel.MarkModelLoader
import de.fraunhofer.aisec.cpg.ExperimentalGraph
import de.fraunhofer.aisec.cpg.TranslationConfiguration
import de.fraunhofer.aisec.cpg.TranslationManager
import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.frontends.LanguageFrontend
import de.fraunhofer.aisec.cpg.graph.*
import de.fraunhofer.aisec.cpg.graph.declarations.TranslationUnitDeclaration
import de.fraunhofer.aisec.cpg.graph.declarations.VariableDeclaration
import de.fraunhofer.aisec.cpg.graph.statements.expressions.ConstructExpression
import de.fraunhofer.aisec.cpg.passes.EdgeCachePass
import de.fraunhofer.aisec.cpg.passes.EvaluationOrderGraphPass
import de.fraunhofer.aisec.cpg.passes.IdentifierPass
import de.fraunhofer.aisec.cpg.passes.Pass
import de.fraunhofer.aisec.cpg.passes.scopes.ScopeManager
import de.fraunhofer.aisec.cpg.sarif.PhysicalLocation
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
        val evaluator = Evaluator(mark, ServerConfiguration.builder().build())

        // Let's provide the list of entities and nodes, this is normally done by
        // findInstancesForEntities. In this case, we simply point it towards our variable
        // declaration
        val entities = listOf(listOf(Pair("a", a)))

        val markContextHolder = evaluator.createMarkContext(entities)
        assertEquals(1, markContextHolder.allContexts.size)

        val context = markContextHolder.getContext(0)
        assertNotNull(context)

        val resultCtx = AnalysisContext(File(""), graph)

        // these are the "converted" Mark classes, because for some reason the Evaluator does not
        // directly use the xtext classes
        val convertedMark = MarkModelLoader().load(mapOf("" to model))
        assertNotNull(convertedMark)

        val convertedRule = convertedMark.rules.first()
        assertNotNull(convertedRule)

        val convertedEntity = convertedMark.getEntity("MyClass")
        assertNotNull(convertedEntity)

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
        assertEquals(1, result.size)

        val ctx = result[0]
        assertNotNull(ctx)

        assertEquals(ConstantValue.of(true), ctx)
    }

    companion object {
        lateinit var graph: Graph
        lateinit var model: MarkModel
        lateinit var mark: Mark

        lateinit var c: ConstructExpression
        lateinit var a: VariableDeclaration

        @BeforeAll
        @JvmStatic
        fun setup() {
            val result = TranslationResult(TranslationManager.builder().build())
            val tu = tu {
                function("main") {
                    body {
                        declare {
                            a =
                                variable("a", type("MyClass")) {
                                    new { c = construct("MyClass") { literal(1) } }
                                }
                        }
                        // assign(lhs = ref("a", a), rhs = literal(1))
                    }
                }
            }
            result.addTranslationUnit(tu)

            // we need to run some passes, otherwise some edges will not be present
            var pass: Pass = EdgeCachePass()
            pass.accept(result)

            pass = IdentifierPass()
            pass.accept(result)

            pass = EvaluationOrderGraphPass()
            pass.lang =
                object :
                    LanguageFrontend(
                        TranslationConfiguration.builder().build(),
                        ScopeManager(),
                        ""
                    ) {
                    override fun parse(file: File?): TranslationUnitDeclaration {
                        return TranslationUnitDeclaration()
                    }

                    override fun <T : Any?> getCodeFromRawNode(astNode: T): String? {
                        return null
                    }

                    override fun <T : Any?> getLocationFromRawNode(astNode: T): PhysicalLocation? {
                        return null
                    }

                    override fun <S : Any?, T : Any?> setComment(s: S, ctx: T) {}
                }
            pass.accept(result)

            graph = result.graph

            var myEntity: EntityDeclaration? = null
            var myRule: RuleDeclaration? = null

            model =
                mark {
                    myEntity =
                        entity("MyClass") {
                            variable("field")
                            op("init") { stmt { call("MyClass") { param("field") } } }
                        }
                    myRule =
                        rule("myRule") {
                            statement {
                                using(myEntity!!, "a")
                                ensure {
                                    comparison(left = operand("a.field"), op = "==", right = lit(1))
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
