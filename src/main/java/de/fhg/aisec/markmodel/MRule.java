package de.fhg.aisec.markmodel;

import org.checkerframework.checker.nullness.qual.Nullable;

import de.fhg.aisec.mark.markDsl.RuleStatement;

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

}
