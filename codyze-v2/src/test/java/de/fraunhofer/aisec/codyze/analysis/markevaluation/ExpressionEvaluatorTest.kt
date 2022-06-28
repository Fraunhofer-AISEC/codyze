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
        assertNotNull(convertedMark.rules)

        val convertedEntity = convertedMark.getEntity("MyClass")
        assertNotNull(convertedEntity)

        for (convertedRule in convertedMark.rules) {
            assertNotNull(convertedRule)

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

            assertEquals(
                ConstantValue.of(true),
                ctx,
                "${convertedRule.name} was not evaluated to true"
            )
        }
    }

    @Test
    fun nullExpressionTest() {
        val evaluator = Evaluator(mark, ServerConfiguration.builder().build())

        // Let's provide the list of entities and nodes, this is normally done by
        // findInstancesForEntities. In this case, we simply point it towards our variable
        // declaration
        val entities = listOf(listOf(Pair("a", a)))

        val markContextHolder = evaluator.createMarkContext(entities)
        assertEquals(1, markContextHolder.allContexts.size)

        val eval =
            ExpressionEvaluator(
                graph,
                null,
                null,
                null,
                ServerConfiguration.builder().build(),
                markContextHolder
            )

        val result = eval.evaluateExpression(null)
        assertEquals(markContextHolder.generateNullResult(), result)
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
            var eqRule: RuleDeclaration? = null
            var multRule: RuleDeclaration? = null
            var divRule: RuleDeclaration? = null
            var modRule: RuleDeclaration? = null
            var leftShiftRule: RuleDeclaration? = null
            var rightShiftRule: RuleDeclaration? = null
            var bitwiseAndRule: RuleDeclaration? = null
            var bitwiseOrRule: RuleDeclaration? = null
            var plusSignRule: RuleDeclaration? = null
            var minusSignRule: RuleDeclaration? = null
            var logicalNotRule: RuleDeclaration? = null
            var bitwiseComplementRule: RuleDeclaration? = null

            model = mark {
                myEntity =
                    entity("MyClass") {
                        variable("field")
                        op("init") { stmt { call("MyClass") { param("field") } } }
                    }
                eqRule =
                    rule("eqRule") {
                        statement {
                            using(myEntity!!, "a")
                            ensure {
                                // comparison is: a == 1
                                exp =
                                    comparison(left = operand("a.field"), op = "==", right = lit(1))
                            }
                        }
                    }
                multRule =
                    rule("multRule") {
                        statement {
                            using(myEntity!!, "a")
                            ensure {
                                // comparison is: a == 1 * 1
                                exp =
                                    comparison(
                                        left = operand("a.field"),
                                        op = "==",
                                        right = mul(left = lit(1), op = "*", right = lit(1))
                                    )
                            }
                        }
                    }
                divRule =
                    rule("divRule") {
                        statement {
                            using(myEntity!!, "a")
                            ensure {
                                // comparison is: a == 2 / 2
                                exp =
                                    comparison(
                                        left = operand("a.field"),
                                        op = "==",
                                        right = mul(left = lit(2), op = "/", right = lit(2))
                                    )
                            }
                        }
                    }
                modRule =
                    rule("modRule") {
                        statement {
                            using(myEntity!!, "a")
                            ensure {
                                // comparison is: a == 19 % 6
                                exp =
                                    comparison(
                                        left = operand("a.field"),
                                        op = "==",
                                        right = mul(left = lit(19), op = "%", right = lit(6))
                                    )
                            }
                        }
                    }
                leftShiftRule =
                    rule("leftShiftRule") {
                        statement {
                            ensure {
                                // comparison is: 32 == 2 << 4
                                exp =
                                    comparison(
                                        left = lit(32),
                                        op = "==",
                                        right = mul(left = lit(2), op = "<<", right = lit(4))
                                    )
                            }
                        }
                    }
                rightShiftRule =
                    rule("rightShiftRule") {
                        statement {
                            ensure {
                                // comparison is: 4 == 32 >> 3
                                exp =
                                    comparison(
                                        left = lit(4),
                                        op = "==",
                                        right = mul(left = lit(32), op = ">>", right = lit(3))
                                    )
                            }
                        }
                    }
                bitwiseAndRule =
                    rule("bitwiseAndRule") {
                        statement {
                            ensure {
                                // comparison is: 2 == 11 & 6 (0b10 == 0b1011 & 0b110)
                                exp =
                                    comparison(
                                        left = lit(2),
                                        op = "==",
                                        right = mul(left = lit(11), op = "&", right = lit(6))
                                    )
                            }
                        }
                    }

                bitwiseOrRule =
                    rule("bitwiseOrRule") {
                        statement {
                            ensure {
                                // comparison is: 5 == 7 & 2 (0b101 == 0b111 & 0b10)
                                exp =
                                    comparison(
                                        left = lit(5),
                                        op = "==",
                                        right = mul(left = lit(7), op = "&^", right = lit(2))
                                    )
                            }
                        }
                    }
                plusSignRule =
                    rule("plusSignRule") {
                        statement {
                            ensure {
                                // comparison is: 5 == +5
                                exp =
                                    comparison(
                                        left = lit(5),
                                        op = "==",
                                        right = unary(exp = lit(5), op = "+")
                                    )
                            }
                        }
                    }
                minusSignRule =
                    rule("minusSignRule") {
                        statement {
                            ensure {
                                // comparison is: 5 != -(5)
                                exp =
                                    comparison(
                                        left = lit(5),
                                        op = "!=",
                                        right = unary(exp = lit(5), op = "-")
                                    )
                            }
                        }
                    }
                logicalNotRule =
                    rule("logicalNotRule") {
                        statement {
                            ensure {
                                // comparison is: true == !false
                                exp =
                                    comparison(
                                        left = lit(true),
                                        op = "==",
                                        right = unary(exp = lit(false), op = "!")
                                    )
                            }
                        }
                    }
            }

            assertNotNull(eqRule)
            assertNotNull(multRule)
            assertNotNull(divRule)
            assertNotNull(modRule)
            assertNotNull(leftShiftRule)
            assertNotNull(rightShiftRule)
            assertNotNull(bitwiseAndRule)
            assertNotNull(bitwiseOrRule)
            assertNotNull(plusSignRule)
            assertNotNull(minusSignRule)
            assertNotNull(logicalNotRule)
            assertNotNull(myEntity)

            mark = MarkModelLoader().load(mapOf("" to model))
        }
    }
}
