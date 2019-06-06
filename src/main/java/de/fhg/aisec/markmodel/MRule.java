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

  // https://javapapers.com/java/java-string-vs-stringbuilder-vs-stringbuffer-concatenation-performance-micro-benchmark/
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("rule " + getName() + " {");
    if (!statement.getEntities().isEmpty()) {
      sb.append(
          "\n\tusing "
              + statement.getEntities().stream()
                  .map(entity -> entity.getE().getName() + " as " + entity.getN())
                  .collect(Collectors.joining(", \n\t\t")));
    }
    if (statement.getCond() != null) {
      sb.append("\n\twhen " + MarkInterpreter.exprToString(statement.getCond().getExp()));
    }
    sb.append(
        "\n\tensure\n\t\t"
            + MarkInterpreter.exprToString(statement.getEnsure().getExp())
            + "\n\tonfail "
            + statement.getMsg()
            + "\n}");
    return sb.toString();
  }
}
