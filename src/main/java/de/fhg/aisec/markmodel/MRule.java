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

  /**
   * Order-Statement to FSM
   *
   * <p>Possible classes of the order construct: Terminal SequenceExpression RepetitionExpression
   * (mit ?, *, +)
   *
   * <p>Start with a "empty" FSM with only StartNode and EndNode
   *
   * <p>prevPointer = [&StartNode]
   *
   * <p>For each Terminal Add node, connect each last node (= each Node in prevPointer) to the
   * current node, return current node as only new prevPointer
   *
   * <p>For each Exp in SequenceExpression: call algo recursively, update (=overwrite)
   * prevPointer-List after each algo-call
   *
   * <p>For RepetitionExpression For + algo(inner) use * - part below once For ? algo(inner) the
   * resulting prevPointer-List needs to be added to the outer prevPointer List For * algo(inner),
   * BUT: the last node of the inner construct needs to point to the first node of the inner
   * construct the resulting prevPointer-List needs to be added to the outer prevPointer List
   */

  // TODO this and the following function might be obsolete once the statement is parsed
  // as objects with nice toString() functions. unclear yet if this will be the case

  // https://javapapers.com/java/java-string-vs-stringbuilder-vs-stringbuffer-concatenation-performance-micro-benchmark/
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("rule " + getName() + " {");
    if (!statement.getEntities().isEmpty()) {
      sb.append(
          "\n\tfor "
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
