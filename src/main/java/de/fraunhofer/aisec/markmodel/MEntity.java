package de.fraunhofer.aisec.markmodel;

import de.fraunhofer.aisec.mark.markDsl.OpStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class MEntity {

  private String name;
  private String superName = null;
  private String packageName = null;

  private HashMap<String, String> parsedVars = null;

  @NonNull private final List<MOp> ops = new ArrayList<>();

  @NonNull private final List<MVar> vars = new ArrayList<>();

  public boolean equals(Object obj) {
    if (!(obj instanceof MEntity)) {
      return false;
    }
    MEntity other = (MEntity) obj;
    return Objects.equals(packageName, other.packageName)
        && Objects.equals(superName, other.superName)
        && Objects.equals(name, other.name);
  }

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

  public MOp getOp(@NonNull String op) {
    return ops.stream().filter(x -> op.equals(x.getName())).findAny().orElse(null);
  }

  @NonNull
  public List<MVar> getVars() {
    return this.vars;
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
      for (OpStatement callStatement : op.getStatements()) {
        sb.append(
            "\t\t"
                + (callStatement.getForbidden() == null ? "" : callStatement.getForbidden() + " ")
                + (callStatement.getVar() == null ? "" : callStatement.getVar() + " = ")
                + callStatement.getCall().getName()
                + "("
                + String.join(", ", callStatement.getCall().getParams())
                + ");\n");
      }
      sb.append("\t}\n");
    }
    sb.append("}");
    return sb.toString();
  }

  public void parseVars() {
    if (parsedVars == null) {
      parsedVars = new HashMap<>();
    }
    for (MVar var : vars) {
      parsedVars.put(var.getName(), var.getType());
    }
  }

  public String getTypeForVar(String name) {
    if (parsedVars == null) { // do a real search
      Optional<MVar> first = this.vars.stream().filter(v -> v.getName().equals(name)).findFirst();
      return first.map(MVar::getType).orElse(null);
    } else { // we prepared this already
      return parsedVars.get(name);
    }
  }
}
