package de.fraunhofer.aisec.mark.markDsl.impl

import de.fraunhofer.aisec.mark.markDsl.ComparisonExpression
import de.fraunhofer.aisec.mark.markDsl.Expression
import de.fraunhofer.aisec.mark.markDsl.Literal
import de.fraunhofer.aisec.mark.markDsl.Operand

fun comparison(left: Expression, right: Expression, op: String): ComparisonExpression {
    val expression = ComparisonExpressionImpl()
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
    val expression = LiteralImpl()
    expression.value = value
    return expression
}
