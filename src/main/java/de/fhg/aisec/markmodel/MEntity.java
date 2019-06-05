package de.fhg.aisec.markmodel;

import de.fhg.aisec.mark.markDsl.CallStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class MEntity {

  private String name;
  private String superName = null;
  private String packageName = null;

  @NonNull private final List<MOp> ops = new ArrayList<>();

  @NonNull private final List<MVar> vars = new ArrayList<>();

  @Nullable
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Nullable
  public String getSuper() {
    return superName;
  }

  public void setSuper(String name) {
    this.superName = name;
  }

  @Nullable
  public String getPackageName() {
    return packageName;
  }

  public void setPackageName(String name) {
    this.packageName = name;
  }

  @NonNull
  public List<MOp> getOps() {
    return this.ops;
  }

  @NonNull
  public List<MVar> getVars() {
    return this.vars;
  }

  public String getTypeForVar(String name) {
    Optional<MVar> first = this.vars.stream().filter(v -> v.getName().equals(name)).findFirst();
    return first.map(MVar::getType).orElse(null);
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("entity " + getName());
    if (getSuper() != null) {
      sb.append(" isa " + getSuper());
    }
    sb.append(" {\n");
    for (MVar var : getVars()) {
      if (var.getType() != null) {
        sb.append("\tvar " + var.getName() + " : " + var.getType() + ";\n");
      } else {
        sb.append("\tvar " + var.getName() + ";\n");
      }
    }
    for (MOp op : getOps()) {
      sb.append("\top " + op.getName() + "() {\n");
      sb.append(
          op.getDeclStatements().stream()
              .map(
                  x ->
                      "\t\tdecl "
                          + x.getRes()
                          + " = "
                          + MarkInterpreter.exprToString(x.getDecl())
                          + ";\n")
              .collect(Collectors.joining()));
      for (CallStatement callStatement : op.getCallStatements()) {
        sb.append(
            "\t\tcall "
                + (callStatement.getForbidden() == null ? "" : callStatement.getForbidden() + " ")
                + callStatement.getCall().getName()
                + "("
                + callStatement.getCall().getParams().stream().collect(Collectors.joining(", "))
                + ");\n");
      }
      sb.append("\t}\n");
    }
    sb.append("}");
    return sb.toString();
  }
}
