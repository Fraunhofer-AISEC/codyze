package de.fraunhofer.aisec.crymlin.utils;

import de.fraunhofer.aisec.crymlin.dsl.CrymlinTraversalSource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

public class CrymlinQueryWrapper {

  public static HashSet<Vertex> getCalls(
      CrymlinTraversalSource crymlinTraversal,
      String functionName,
      String baseType,
      ArrayList<String> parameter) {
    HashSet<Vertex> ret = new HashSet<>();
    List<Vertex> vertices = crymlinTraversal.calls(functionName, baseType).toList();

    for (Vertex v : vertices) {

      boolean parameters_match = true;
      if (parameter.size() > 0 && parameter.get(0).equals("*")) {
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
            if (!parameter.get(argumentIndex).equals(arg.value("type").toString())) {
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
}
