package de.fraunhofer.aisec.markmodel;

import de.fraunhofer.aisec.mark.markDsl.*;

import java.util.stream.Collectors;

/**
 * Static helper methods for evaluating MARK expressions.
 */
public class ExpressionHelper {

	public static String exprToString(Expression expr) {
		if (expr == null) {
			return " null ";
		}

		if (expr instanceof LogicalOrExpression) {
			return exprToString(((LogicalOrExpression) expr).getLeft()) + " || " + exprToString(((LogicalOrExpression) expr).getRight());
		} else if (expr instanceof LogicalAndExpression) {
			return exprToString(((LogicalAndExpression) expr).getLeft()) + " && " + exprToString(((LogicalAndExpression) expr).getRight());
		} else if (expr instanceof ComparisonExpression) {
			ComparisonExpression compExpr = (ComparisonExpression) expr;
			return exprToString(compExpr.getLeft()) + " " + compExpr.getOp() + " " + exprToString(compExpr.getRight());
		} else if (expr instanceof FunctionCallExpression) {
			FunctionCallExpression fExpr = (FunctionCallExpression) expr;
			String name = fExpr.getName();
			return name + "(" + fExpr.getArgs().stream().map(ExpressionHelper::argToString).collect(Collectors.joining(", ")) + ")";
		} else if (expr instanceof LiteralListExpression) {
			return "[ " + ((LiteralListExpression) expr).getValues().stream().map(Literal::getValue).collect(Collectors.joining(", ")) + " ]";
		} else if (expr instanceof RepetitionExpression) {
			RepetitionExpression inner = (RepetitionExpression) expr;
			// todo @FW do we want this optimization () can be omitted if inner is no sequence
			if (inner.getExpr() instanceof SequenceExpression) {
				return "(" + exprToString(inner.getExpr()) + ")" + inner.getOp();
			} else {
				return exprToString(inner.getExpr()) + inner.getOp();
			}
		} else if (expr instanceof Operand) {
			return ((Operand) expr).getOperand();
		} else if (expr instanceof Literal) {
			return ((Literal) expr).getValue();
		} else if (expr instanceof SequenceExpression) {
			SequenceExpression seq = ((SequenceExpression) expr);
			return exprToString(seq.getLeft()) + seq.getOp() + " " + exprToString(seq.getRight());
		} else if (expr instanceof Terminal) {
			Terminal inner = (Terminal) expr;
			return inner.getEntity() + "." + inner.getOp() + "()";
		} else if (expr instanceof OrderExpression) {
			OrderExpression order = (OrderExpression) expr;
			SequenceExpression seq = (SequenceExpression) order.getExp();
			return "order " + exprToString(seq);
		}
		return "UNKNOWN EXPRESSION TYPE: " + expr.getClass();
	}

	public static String argToString(Argument arg) {
		return exprToString((Expression) arg); // Every Argument is also an Expression
	}
}
