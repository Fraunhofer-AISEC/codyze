package de.fhg.aisec.markmodel;

import java.util.ArrayList;
import java.util.List;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class MOp {

  private String name;
  private List<String> parameters = new ArrayList<>();
  private List<MOpCallStmt> statements = new ArrayList<>();

  @Nullable
  public String getName() {
    return name;
  }

  public void setParameters(List<String> parameters) {
    this.parameters = parameters;
  }

  public void setStatements(List<MOpCallStmt> statements) {
    this.statements = statements;
  }

  public void setName(String name) {
    this.name = name;    
  }

  @NonNull
  public List<String> getParameters() {
    return this.parameters;
  }

  @NonNull
  public List<MOpCallStmt> getStatements() {
    return this.statements;
  }

}
