package de.fraunhofer.aisec.mark.markDsl.impl

import de.fraunhofer.aisec.mark.markDsl.*

fun comparison(left: Expression, right: Expression, op: String): ComparisonExpression {
    val expression = ComparisonExpressionImpl()
    expression.left = left
    expression.right = right
    expression.op = op
    return expression
}

fun mul(left: Expression, right: Expression, op: String): MultiplicationExpression {
    val expression = MultiplicationExpressionImpl()
    expression.left = left
    expression.right = right
    expression.op = op
    return expression
}

fun unary(exp: Expression, op: String): UnaryExpression {
    val expression = UnaryExpressionImpl()
    expression.exp = exp
    expression.op = op
    return expression
}

fun logicalAnd(left: Expression, right: Expression, op: String): LogicalAndExpression {
    val expression = LogicalAndExpressionImpl()
    expression.left = left
    expression.right = right
    expression.op = op
    return expression
}

fun logicalOr(left: Expression, right: Expression, op: String): LogicalOrExpression {
    val expression = LogicalOrExpressionImpl()
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

fun lit(value: Boolean): Literal {
    val expression = BooleanLiteralImpl()
    expression.value = value.toString()
    return expression
}

fun litList(vararg values: Literal): LiteralListExpression {
    val expression = LiteralListExpressionImpl()
    expression.getValues().addAll(values)
    return expression
}

fun funcCall(name: String, vararg args: Argument): FunctionCallExpression {
    val expression = FunctionCallExpressionImpl()
    expression.getArgs().addAll(args)
    expression.name = name
    return expression
}
