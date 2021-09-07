package de.fraunhofer.aisec.codyze.crymlin

import de.fraunhofer.aisec.cpg.ExperimentalGraph
import de.fraunhofer.aisec.cpg.graph.DeclarationHolder
import de.fraunhofer.aisec.cpg.graph.HasType
import de.fraunhofer.aisec.cpg.graph.NodeBuilder.*
import de.fraunhofer.aisec.cpg.graph.StatementHolder
import de.fraunhofer.aisec.cpg.graph.declarations.Declaration
import de.fraunhofer.aisec.cpg.graph.declarations.FunctionDeclaration
import de.fraunhofer.aisec.cpg.graph.declarations.VariableDeclaration
import de.fraunhofer.aisec.cpg.graph.statements.CompoundStatement
import de.fraunhofer.aisec.cpg.graph.statements.DeclarationStatement
import de.fraunhofer.aisec.cpg.graph.statements.Statement
import de.fraunhofer.aisec.cpg.graph.statements.expressions.BinaryOperator
import de.fraunhofer.aisec.cpg.graph.statements.expressions.DeclaredReferenceExpression
import de.fraunhofer.aisec.cpg.graph.statements.expressions.Expression
import de.fraunhofer.aisec.cpg.graph.statements.expressions.Literal
import de.fraunhofer.aisec.cpg.graph.types.Type
import de.fraunhofer.aisec.cpg.graph.types.TypeParser
import de.fraunhofer.aisec.cpg.graph.types.UnknownType
import org.junit.jupiter.api.Test

class ExpressionEvaluatorTest : AbstractTest() {

    @OptIn(ExperimentalGraph::class)
    @Test
    fun test() {
        var a: VariableDeclaration? = null

        val main =
            function("main") {
                body {
                    declare { a = variable("a") }
                    assign(lhs = ref("a", a), rhs = literal(1))
                }
            }

        println(main)
    }
}

private operator fun DeclaredReferenceExpression.plus(rhs: Expression): BinaryOperator {
    val node = BinaryOperator()
    node.operatorCode = "+"
    node.lhs = this
    node.rhs = rhs

    return node
}

private inline fun <reified T : Declaration> declare(
    name: String,
    type: Type = UnknownType.getUnknownType()
): T {
    val decl = T::class::constructors.get().first().call()
    decl.name = name

    if (decl is HasType) {
        decl.type = type
    }

    return decl
}

fun typeOf(string: String): Type {
    return TypeParser.createFrom(string, false)
}

@DslMarker annotation class GraphMarker

fun DeclarationHolder.function(
    name: String,
    init: FunctionDeclaration.() -> Unit
): FunctionDeclaration {
    val node = declare<FunctionDeclaration>(name)
    node.init()
    this.addDeclaration(node)
    return node
}

fun function(name: String, init: FunctionDeclaration.() -> Unit): FunctionDeclaration {
    val node = FunctionDeclaration()
    node.name = name
    node.init()
    return node
}

fun ref(
    name: String,
    refersTo: Declaration? = null,
    init: DeclaredReferenceExpression.() -> Unit = {}
): DeclaredReferenceExpression {
    val node = DeclaredReferenceExpression()
    node.name = name
    node.refersTo = refersTo
    node.init()
    return node
}

fun <T> literal(value: T, init: DeclarationStatement.() -> Unit = {}): Literal<T> {
    val node = Literal<T>()
    node.value = value
    return node
}

fun <T : Statement> FunctionDeclaration.initTag(tag: T, init: T.() -> Unit): T {
    tag.init()
    this.body = tag
    return tag
}

fun FunctionDeclaration.body(init: StatementHolder.() -> Unit) = initTag(CompoundStatement(), init)

fun StatementHolder.declare(init: DeclarationStatement.() -> Unit) =
    add(DeclarationStatement(), init)

fun StatementHolder.binaryOp(
    lhs: Expression,
    rhs: Expression,
    op: String,
    init: BinaryOperator.() -> Unit = {}
): BinaryOperator {
    val node = BinaryOperator()
    node.operatorCode = op
    node.lhs = lhs
    node.rhs = rhs
    add(node, init)
    return node
}

fun StatementHolder.assign(lhs: Expression, rhs: Expression, init: BinaryOperator.() -> Unit = {}) =
    binaryOp(lhs, rhs, "=", init)

private fun <T : Statement> StatementHolder.add(tag: T, init: T.() -> Unit = {}): T {
    tag.init()
    this.addStatement(tag)
    return tag
}

fun DeclarationStatement.variable(
    name: String,
    init: VariableDeclaration.() -> Unit = {}
): VariableDeclaration {
    val node = declare<VariableDeclaration>(name)
    node.init()
    this.addToPropertyEdgeDeclaration(node)
    return node
}
