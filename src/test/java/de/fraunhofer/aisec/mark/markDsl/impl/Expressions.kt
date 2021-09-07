package de.fraunhofer.aisec.mark.markDsl.impl

import de.fraunhofer.aisec.mark.markDsl.*

fun EnsureStatement.comparison(
    left: Expression,
    right: Expression,
    op: String
): ComparisonExpression {
    val expression = ComparisonExpressionImpl()
    expression.left = left
    expression.right = right
    expression.op = op
    this.exp = expression
    return expression
}

fun operand(name: String): Operand {
    val expression = OperandImpl()
    expression.operand = name
    return expression
}

fun lit(value: String): Literal {
    val expression = LiteralImpl()
    expression.value = value
    return expression
}
