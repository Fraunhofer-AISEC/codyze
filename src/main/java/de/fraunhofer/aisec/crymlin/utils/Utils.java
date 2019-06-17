package de.fraunhofer.aisec.crymlin.utils;

import de.fraunhofer.aisec.cpg.graph.MethodDeclaration;
import de.fraunhofer.aisec.cpg.graph.RecordDeclaration;

public class Utils {

  /**
   * Returns the fully qualified signature of a method within a record declaration (e.g., a Java
   * class).
   *
   * @param r
   * @param m
   * @return
   */
  public static String toFullyQualifiedSignature(RecordDeclaration r, MethodDeclaration m) {
    return r.getName() + "." + m.getSignature();
  }

  /**
   * Return a unified type String (i.e. changeing cpp-type-separators to Java-type-separators)
   *
   * @param name
   * @return a type string which is separated via "."
   */
  public static String unifyType(String name) {
    if (name == null) {
      return null;
    }
    return name.replaceAll("::", "\\.");
  }

  public static String extractMethodName(String opName) {
    if (opName.contains("::")) {
      opName = opName.substring(opName.lastIndexOf("::") + 2);
    } else if (opName.contains("->")) {
      opName = opName.substring(opName.lastIndexOf("->") + 2);
    }
    return opName;
  }

  public static String extractType(String opName) {
    if (opName.contains("::")) {
      opName = opName.substring(0, opName.lastIndexOf("::"));
    } else if (opName.contains("->")) {
      opName = opName.substring(0, opName.lastIndexOf("->"));
    }
    return opName;
  }
}
