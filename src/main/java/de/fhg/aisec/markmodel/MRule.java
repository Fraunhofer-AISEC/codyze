package de.fhg.aisec.markmodel;

import de.fhg.aisec.mark.markDsl.*;
import de.fhg.aisec.mark.markDsl.impl.*;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.Nullable;

public class MRule {

  private String name;
  private RuleStatement statement;

  @Nullable
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setStatement(RuleStatement stmt) {
    // This is an ECore RuleStatement.
    this.statement = stmt;
  }

  public RuleStatement getStatement() {
    return statement;
  }

  // TODO this and the following function might be obsolete once the statement is parsed
  // as objects with nice toString() functions. unclear yet if this will be the case
  private String argToString(Argument arg) {
    if (arg instanceof OperandImpl) {
      OperandImpl inner = (OperandImpl) arg;
      return inner.getOperand();
    } else if (arg instanceof LiteralImpl) {
      LiteralImpl inner = (LiteralImpl) arg;
      return inner.getValue();
    } else if (arg instanceof Expression) {
      return exprToString((Expression) arg);
    } else {
      return "UNKNOWN ARGUMENT TYPE " + arg.getClass();
    }
  }

  private String exprToString(Expression exp) {
    if (exp instanceof ComparisonExpressionImpl) {
      ComparisonExpressionImpl inner = (ComparisonExpressionImpl) exp;
      return exprToString(inner.getLeft())
          + " "
          + inner.getOp()
          + " "
          + exprToString(inner.getRight());
    } else if (exp instanceof LogicalAndExpressionImpl) {
      LogicalAndExpressionImpl inner = (LogicalAndExpressionImpl) exp;
      return exprToString(inner.getLeft())
          + " "
          + inner.getOp()
          + " "
          + exprToString(inner.getRight());
    } else if (exp instanceof SequenceExpressionImpl) {
      SequenceExpressionImpl inner = (SequenceExpressionImpl) exp;
      return exprToString(inner.getLeft()) + inner.getOp() + " " + exprToString(inner.getRight());
    } else if (exp instanceof RepetitionExpressionImpl) {
      RepetitionExpressionImpl inner = (RepetitionExpressionImpl) exp;
      return "(" + exprToString(inner.getExpr()) + ")" + inner.getOp();
    } else if (exp instanceof TerminalImpl) {
      TerminalImpl inner = (TerminalImpl) exp;
      return inner.getEntity() + "." + inner.getOp();
    } else if (exp instanceof FunctionCallExpressionImpl) {
      FunctionCallExpressionImpl inner = (FunctionCallExpressionImpl) exp;
      String args =
          inner.getArgs().stream().map(x -> argToString(x)).collect(Collectors.joining(", "));
      return inner.getName() + "(" + args + ")";
    } else if (exp instanceof LiteralListExpressionImpl) {
      LiteralListExpressionImpl inner = (LiteralListExpressionImpl) exp;
      String list =
          inner.getValues().stream().map(x -> x.getValue()).collect(Collectors.joining(", "));
      return "[ " + list + " ]";
    } else if (exp instanceof OperandImpl) {
      OperandImpl inner = (OperandImpl) exp;
      return inner.getOperand();
    } else if (exp instanceof OrderExpressionImpl) {
      OrderExpressionImpl inner = (OrderExpressionImpl) exp;
      return "order " + exprToString(inner.getExp());
    } else {
      return "UNKNOWN EXPRESSION TYPE: " + exp.getClass();
    }
  }

  // https://javapapers.com/java/java-string-vs-stringbuilder-vs-stringbuffer-concatenation-performance-micro-benchmark/
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("rule " + getName() + " {\n");
    if (!statement.getEntities().isEmpty()) {
      sb.append(
          "\tfor"
              + statement.getEntities().stream()
                  .map(entity -> "\n\t\t" + entity.getE().getName() + " as " + entity.getN())
                  .collect(Collectors.joining(", ")));
    }
    sb.append(
        "\n\tensure\n\t\t"
            + exprToString(statement.getEnsure().getExp())
            + "\n\tonfail "
            + statement.getMsg()
            + "\n}");
    return sb.toString();
  }
}
