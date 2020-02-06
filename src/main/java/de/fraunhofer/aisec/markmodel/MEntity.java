
package de.fraunhofer.aisec.markmodel;

import de.fraunhofer.aisec.mark.markDsl.OpStatement;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.*;

public class MEntity {

	@NonNull
	private final List<MOp> ops = new ArrayList<>();
	@NonNull
	private final List<MVar> vars = new ArrayList<>();
	private String name;
	private String superName = null;
	@Nullable
	private String packageName = null;
	private HashMap<String, String> parsedVars = null;

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof MEntity)) {
			return false;
		}
		MEntity other = (MEntity) obj;
		return Objects.equals(packageName, other.packageName)
				&& Objects.equals(superName, other.superName)
				&& Objects.equals(name, other.name);
	}

	@Override
	public int hashCode() {
		int ret = 0;
		if (name != null) {
			ret += name.hashCode();
		}
		if (superName != null) {
			ret += superName.hashCode();
		}
		if (packageName != null) {
			ret += packageName.hashCode();
		}
		return ret;
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

	public void setPackageName(@Nullable String name) {
		this.packageName = name;
	}

	@NonNull
	public List<MOp> getOps() {
		return this.ops;
	}

	@Nullable
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
			sb.append("\top " + op.getName() + " {\n");
			for (OpStatement callStatement : op.getStatements()) {
				sb.append(
					"\t\t"
							+ (callStatement.getForbidden() == null ? "" : callStatement.getForbidden() + " ")
							+ (callStatement.getVar() == null ? "" : callStatement.getVar() + " = ")
							+ callStatement.getCall().getName()
							+ "("
							+ String.join(", ", MOp.paramsToString(callStatement.getCall().getParams()))
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
}
