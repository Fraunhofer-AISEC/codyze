package de.fraunhofer.aisec.cpg.graph

import de.fraunhofer.aisec.cpg.graph.declarations.Declaration
import de.fraunhofer.aisec.cpg.graph.declarations.FunctionDeclaration
import de.fraunhofer.aisec.cpg.graph.declarations.TranslationUnitDeclaration
import de.fraunhofer.aisec.cpg.graph.declarations.VariableDeclaration
import de.fraunhofer.aisec.cpg.graph.statements.CompoundStatement
import de.fraunhofer.aisec.cpg.graph.statements.DeclarationStatement
import de.fraunhofer.aisec.cpg.graph.statements.Statement
import de.fraunhofer.aisec.cpg.graph.statements.expressions.*
import de.fraunhofer.aisec.cpg.graph.types.Type
import de.fraunhofer.aisec.cpg.graph.types.TypeParser
import de.fraunhofer.aisec.cpg.graph.types.UnknownType

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

fun type(string: String): Type {
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

fun tu(
    name: String? = null,
    init: TranslationUnitDeclaration.() -> Unit
): TranslationUnitDeclaration {
    val node = TranslationUnitDeclaration()
    if (name != null) {
        node.name = name
    }
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

fun <T> literal(value: T, init: Literal<T>.() -> Unit = {}): Literal<T> {
    val node = Literal<T>()
    node.value = value
    node.init()
    return node
}

fun <T : Statement> FunctionDeclaration.initTag(tag: T, init: T.() -> Unit): T {
    tag.init()
    this.body = tag
    return tag
}

fun FunctionDeclaration.body(init: StatementHolder.() -> Unit) = initTag(CompoundStatement(), init)

fun StatementHolder.declare(init: DeclarationStatement.() -> Unit): DeclarationStatement =
    add(DeclarationStatement(), init)

fun HasInitializer.new(init: NewExpression.() -> Unit = {}): NewExpression {
    val node = NewExpression()
    node.init()
    this.initializer = node
    return node
}

fun HasInitializer.construct(
    typeName: String,
    init: ConstructExpression.() -> Unit = {}
): ConstructExpression {
    val node = ConstructExpression()
    node.name = typeName
    node.type = type(typeName)
    node.init()
    this.initializer = node
    return node
}

fun <T> CallExpression.literal(value: T, init: Literal<T>.() -> Unit = {}): Literal<T> {
    val node = Literal<T>()
    node.value = value
    node.init()
    this.addArgument(node)
    return node
}

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
    type: Type = UnknownType.getUnknownType(),
    init: VariableDeclaration.() -> Unit = {}
): VariableDeclaration {
    val node = declare<VariableDeclaration>(name)
    node.init()
    this.addToPropertyEdgeDeclaration(node)
    return node
}
