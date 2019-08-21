package de.fraunhofer.aisec.crymlin.utils;

import de.fraunhofer.aisec.crymlin.dsl.CrymlinTraversalSource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

public class CrymlinQueryWrapper {

  // do not instantiate
  private CrymlinQueryWrapper() {}

  public static HashSet<Vertex> getCalls(
      CrymlinTraversalSource crymlinTraversal,
      String functionName,
      String baseType,
      ArrayList<String> parameter) {
    HashSet<Vertex> ret = new HashSet<>();
    // unify base type
    baseType = Utils.unifyType(baseType);
    List<Vertex> vertices = crymlinTraversal.calls(functionName, baseType).toList();

    for (Vertex v : vertices) {

      boolean parameters_match = true;
      if (!parameter.isEmpty() && parameter.get(0).equals("*")) {
        // ALL FUNCTIONS WITH THIS BASE TYPE AND NAME MATCH, PARAMETERS ARE IGNORED
      } else {
        boolean[] checkedParameters = new boolean[parameter.size()]; // defaults to false

        Iterator<Edge> arguments = v.edges(Direction.OUT, "ARGUMENTS");
        while (arguments.hasNext()) {
          Vertex arg = arguments.next().inVertex();
          int argumentIndex = Integer.parseInt(arg.value("argumentIndex").toString());
          // argumentIndex starts at 0!
          if (argumentIndex >= parameter.size()) {
            // argumentlength mismatch
            parameters_match = false;
            break;
          }
          checkedParameters[argumentIndex] =
              true; // this parameter is now checked. If it does not match we bail out early

          if (parameter.get(argumentIndex).equals("_")) {
            // skip matching
          } else {
            // either the param in the mark file directly matches, or it has to have a
            // corresponding var which indicates the type
            if (!Utils.unifyType(parameter.get(argumentIndex))
                .equals(Utils.unifyType(arg.value("type").toString()))) {
              parameters_match = false;
              break;
            }
          }
        }
        if (parameters_match) {
          // now check if all parameters were validated
          for (int i = 0; i < parameter.size(); i++) {
            if (!checkedParameters[i]) {
              parameters_match = false;
              break;
            }
          }
        }
      }
      if (parameters_match) { // if all of them were checked
        ret.add(v);
      }
    }
    return ret;
  }

  public static Set<Vertex> getCppCalls(
      CrymlinTraversalSource crymlinTraversal,
      String fqn,
      String functionName,
      String type,
      List<String> parameterTypes) {

    Set<Vertex> ret = new HashSet<>();

    // reconstruct what type of call we're expecting
    boolean isMethod = fqn.endsWith(type);

    if (isMethod) {
      // it's a method call on an instances
      ret.addAll(crymlinTraversal.calls(functionName, type).toList());
    }

    // it's a function OR a static method call -> name == fqn::functionName
    ret.addAll(crymlinTraversal.calls(fqn + "::" + functionName).toList());

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
            long argumentIndex = argument.<Long>property("argumentIndex").value();

            if (argumentIndex >= parameterTypes.size()) {
              // last given parameter type must be "*" or remove
              return !"*".equals(parameterTypes.get(parameterTypes.size() - 1));
            } else {
              // remove if types don't match
              String paramType = parameterTypes.get((int) argumentIndex);
              if (!("_".equals(paramType) || "*".equals(paramType))) {
                // it's not a single type wild card -> types must match
                // TODO improve type matching
                // currently, we check for perfect match but we may need to be more fuzzy e.g. ignore
                // type qualifier (e.g. const) or strip reference types
                if (!paramType.equals(argument.<String>property("type").value())) {
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
}
