package de.fraunhofer.aisec.markmodel;

import de.fraunhofer.aisec.crymlin.utils.Pair;
import de.fraunhofer.aisec.mark.markDsl.RuleStatement;
import de.fraunhofer.aisec.markmodel.fsm.FSM;
import java.util.HashMap;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.NonNull;

public class MRule {

  @NonNull private String name;
  private RuleStatement statement;
  private FSM fsm = null;
  private String errorMessage;
  /**
   * stores Entity-alias to Pair(Name of Entity, EntityReference). The EntityReference can be NULL
   * if the entity is not available/parsed. E.g. for the rule rule UseOfBotan_CipherMode { using
   * Order as cm ensure order cm.start(), cm.finish() onfail WrongUseOfBotan_CipherMode } this would
   * store: cm -> Pair(Order, Reference to the Order-Entity)
   *
   * <p>todo maybe we should not allow rules with NULL references here, as they cannot be evaluated
   * anyway
   */
  private HashMap<String, Pair<String, MEntity>> entityReferences;

  public MRule(@NonNull String name) {
    this.name = name;
  }

  @NonNull
  public String getName() {
    return name;
  }

  public void setName(@NonNull String name) {
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
    sb.append("rule ").append(getName()).append(" {");
    if (!statement.getEntities().isEmpty()) {
      sb.append("\n\tusing ")
          .append(
              statement.getEntities().stream()
                  .map(entity -> entity.getE().getName() + " as " + entity.getN())
                  .collect(Collectors.joining(", \n\t\t")));
    }
    if (statement.getCond() != null) {
      sb.append("\n\twhen ").append(MarkInterpreter.exprToString(statement.getCond().getExp()));
    }
    sb.append("\n\tensure\n\t\t")
        .append(MarkInterpreter.exprToString(statement.getEnsure().getExp()))
        .append("\n\tonfail ")
        .append(statement.getMsg())
        .append("\n}");
    return sb.toString();
  }

  public void setFSM(FSM fsm) {
    this.fsm = fsm;
  }

  public FSM getFSM() {
    return fsm;
  }

  public void setErrorMessage(String msg) {
    this.errorMessage = msg;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setEntityReferences(HashMap<String, Pair<String, MEntity>> entityReferences) {
    this.entityReferences = entityReferences;
  }

  public HashMap<String, Pair<String, MEntity>> getEntityReferences() {
    return entityReferences;
  }
}
