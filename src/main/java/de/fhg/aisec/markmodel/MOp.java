package de.fhg.aisec.markmodel;

import de.fhg.aisec.mark.markDsl.CallStatement;
import de.fhg.aisec.mark.markDsl.DeclarationStatement;
import de.fhg.aisec.mark.markDsl.ImportStatement;
import java.util.ArrayList;
import java.util.List;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class MOp {

  private String name;
  @NonNull private List<MOpCallStmt> statements = new ArrayList<>();
  @NonNull private List<CallStatement> callStatements = new ArrayList<>();
  @NonNull private List<ImportStatement> importStatements = new ArrayList<>();
  @NonNull private List<DeclarationStatement> declStatements = new ArrayList<>();

  @Nullable
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @NonNull
  public List<MOpCallStmt> getStatements() {
    return this.statements;
  }

  @NonNull
  public List<CallStatement> getCallStatements() {
    return this.callStatements;
  }

  @NonNull
  public List<ImportStatement> getImportStatements() {
    return this.importStatements;
  }

  @NonNull
  public List<DeclarationStatement> getDeclStatements() {
    return this.declStatements;
  }
}
