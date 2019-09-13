package de.fraunhofer.aisec.crymlin.utils;

import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.has;

import de.fraunhofer.aisec.cpg.graph.BinaryOperator;
import de.fraunhofer.aisec.cpg.graph.VariableDeclaration;
import de.fraunhofer.aisec.crymlin.dsl.CrymlinTraversalSource;
import de.fraunhofer.aisec.markmodel.Constants;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import javax.validation.constraints.Null;
import org.apache.tinkerpop.gremlin.neo4j.process.traversal.LabelP;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.checkerframework.checker.nullness.qual.NonNull;

public class CrymlinQueryWrapper {

  // do not instantiate
  private CrymlinQueryWrapper() {}

  /**
   * @param crymlinTraversal
   * @param fqnClassName fully qualified name w/o function name itself
   * @param functionName name of the function
   * @param type type of the object used to call the function (i.e. method); should be name of the
   *     MARK entity
   * @param parameterTypes list of parameter types; must appear in order (i.e. index 0 = type of
   *     first parameter, etc.); currently, types must be precise (i.e. with qualifiers, pointer,
   *     reference)
   * @return
   */
  public static Set<Vertex> getCalls(
      @NonNull CrymlinTraversalSource crymlinTraversal,
      @NonNull String fqnClassName,
      @NonNull String functionName,
      @Null String type,
      @NonNull List<String> parameterTypes) {

    Set<Vertex> ret = new HashSet<>();

    // reconstruct what type of call we're expecting
    boolean isMethod = type == null || fqnClassName.endsWith(type);

    if (isMethod) {
      // it's a method call on an instances
      ret.addAll(crymlinTraversal.calls(functionName, type).toList());
    }

    // it's a function OR a static method call -> name == fqnClassName.functionName
    ret.addAll(crymlinTraversal.calls(fqnClassName + "." + functionName).toList());

    // FIXME we're not setting the default (i.e. global) namespace
    if (fqnClassName.length() == 0) {
      ret.addAll(crymlinTraversal.calls(functionName).toList());
    }

    // now, ret contains possible candidates --> need to filter out calls where params don't match
    ret.removeIf(
        (v) -> {
          Iterator<Edge> referencedArguments = v.edges(Direction.OUT, "ARGUMENTS");

          if ((parameterTypes.size() == 0) && referencedArguments.hasNext()) {
            // expecting no arguments but got at least one -> remove
            return true;
          }

          if ((parameterTypes.size() != 0) && !referencedArguments.hasNext()) {
            // expecting some parameters but got no arguments -> remove
            return true;
          }

          while (referencedArguments.hasNext()) {
            // check each argument against parameter type
            Vertex argument = referencedArguments.next().inVertex();
            long argumentIndex = argument.value("argumentIndex");

            if (argumentIndex >= parameterTypes.size()) {
              // last given parameter type must be "..." or remove
              return !Constants.ELLIPSIS.equals(parameterTypes.get(parameterTypes.size() - 1));
            } else {
              // remove if types don't match
              String paramType = parameterTypes.get((int) argumentIndex);
              if (!(Constants.UNDERSCORE.equals(paramType)
                  || Constants.ELLIPSIS.equals(paramType))) {
                // it's not a single type wild card -> types must match
                // TODO improve type matching
                // currently, we check for perfect match but we may need to be more fuzzy e.g.
                // ignore
                // type qualifier (e.g. const) or strip reference types
                // FIXME match expects fully-qualified type literal; in namespace 'std',
                // 'std::string' becomes just 'string'
                // FIXME string literals in C++ have type 'const char[{some integer}]' instead of
                // 'std::string'
                if (paramType.equals("std.::string")) {
                  String argValue = argument.<String>property("type").orElse("");

                  if (paramType.equals(argValue)
                      || Pattern.matches("const char\\s*\\[\\d*\\]", argValue)) {
                    // it matches C++ string types
                    return false;
                  }
                } else if (!paramType.equals(argument.value("type"))) {
                  // types don't match -> remove
                  return true;
                }
              }
            }
          }
          return false;
        });

    return ret;
  }

  public static List<Vertex> lhsVariableOfAssignment(CrymlinTraversalSource crymlin, long id) {
    return crymlin
        .byID(id)
        .in("RHS")
        .where(
            has(T.label, LabelP.of(BinaryOperator.class.getSimpleName()))
                .and()
                .has("operatorCode", "="))
        .out("LHS")
        .has(T.label, LabelP.of(VariableDeclaration.class.getSimpleName()))
        .toList();
  }
}
