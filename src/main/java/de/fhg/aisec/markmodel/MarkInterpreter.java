package de.fhg.aisec.markmodel;

import de.fhg.aisec.mark.markDsl.Argument;
import de.fhg.aisec.mark.markDsl.ComparisonExpression;
import de.fhg.aisec.mark.markDsl.Expression;
import de.fhg.aisec.mark.markDsl.FunctionCallExpression;
import de.fhg.aisec.mark.markDsl.Literal;
import de.fhg.aisec.mark.markDsl.LiteralListExpression;
import de.fhg.aisec.mark.markDsl.LogicalAndExpression;
import de.fhg.aisec.mark.markDsl.LogicalOrExpression;
import de.fhg.aisec.mark.markDsl.Operand;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.NonNull;

public class MarkInterpreter {
  @NonNull private Mark markModel;

  public MarkInterpreter(@NonNull Mark markModel) {
    this.markModel = markModel;
  }

  public String exprToString(Expression expr) {
    if (expr == null) {
      return "";
    }

    if (expr instanceof LogicalOrExpression) {
      return exprToString(((LogicalOrExpression) expr).getLeft())
          + " || "
          + exprToString(((LogicalOrExpression) expr).getRight());
    } else if (expr instanceof LogicalAndExpression) {
      return exprToString(((LogicalOrExpression) expr).getLeft())
          + " && "
          + exprToString(((LogicalOrExpression) expr).getRight());
    } else if (expr instanceof ComparisonExpression) {
      ComparisonExpression compExpr = (ComparisonExpression) expr;
      return exprToString(compExpr.getLeft())
          + " "
          + compExpr.getOp()
          + " "
          + exprToString(compExpr.getRight());
    } else if (expr instanceof FunctionCallExpression) {
      FunctionCallExpression fExpr = (FunctionCallExpression) expr;
      String name = fExpr.getName();
      return name
          + "("
          + String.join(
              ",",
              fExpr.getArgs().stream()
                  .map(arg -> argToString(arg))
                  .collect(Collectors.<String>toList()))
          + ")";
    } else if (expr instanceof LiteralListExpression) {
      return "["
          + String.join(
              ",",
              ((LiteralListExpression) expr)
                  .getValues().stream().map(val -> val.getValue()).collect(Collectors.toList()))
          + "]";
    } else if (expr instanceof Operand) {
      return ((Operand) expr).getOperand();
    } else if (expr instanceof Literal) {
      return ((Literal) expr).getValue();
    }
    return expr.toString();
  }

  private String argToString(Argument arg) {
    return exprToString((Expression) arg); // Every Argument is also an Expression
  }
}
