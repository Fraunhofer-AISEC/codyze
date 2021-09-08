package de.fraunhofer.aisec.mark.markDsl.impl

import de.fraunhofer.aisec.mark.markDsl.*
import org.eclipse.emf.common.util.BasicEList

fun mark(init: MarkModel.() -> Unit): MarkModel {
    val mark = MarkModelImpl()
    mark.init()
    return mark
}

fun MarkModel.rule(name: String, init: RuleDeclaration.() -> Unit): RuleDeclaration {
    val rule = RuleDeclarationImpl()
    rule.name = name
    rule.init()
    this.rule += rule
    return rule
}

fun RuleDeclaration.statement(init: RuleStatement.() -> Unit): RuleStatement {
    val statement = RuleStatementImpl()
    statement.entities = BasicEList()
    statement.init()

    this.stmt = statement
    return statement
}

infix fun EntityDeclaration.`as`(name: String): AliasedEntityExpression {
    val expression = AliasedEntityExpressionImpl()
    expression.e = this
    expression.n = name
    return expression
}

fun RuleStatement.using(
    declaration: EntityDeclaration,
    name: String,
    init: AliasedEntityExpressionImpl.() -> Unit = {}
): AliasedEntityExpression {
    val expression = AliasedEntityExpressionImpl()
    expression.e = declaration
    expression.n = name
    expression.init()
    this.entities += expression
    return expression
}

fun RuleStatement.ensure(
    expression: Expression? = null,
    init: EnsureStatement.() -> Unit = {}
): EnsureStatement {
    val statement = EnsureStatementImpl()
    statement.exp = expression
    statement.init()
    this.ensure = statement
    return statement
}
