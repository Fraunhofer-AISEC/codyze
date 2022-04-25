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

fun mul(left: Expression, right: Expression, op: String): MultiplicationExpression {
    val expression = MultiplicationExpressionImpl()
    expression.left = left
    expression.right = right
    expression.op = op
    return expression
}

fun operand(name: String): Operand {
    val expression = OperandImpl()
    expression.operand = name
    return expression
}

fun lit(value: String): Literal {
    val expression = StringLiteralImpl()
    expression.value = value
    return expression
}

fun lit(value: Int): Literal {
    val expression = IntegerLiteralImpl()
    expression.value = "0x${Integer.toHexString(value)}"
    return expression
}
