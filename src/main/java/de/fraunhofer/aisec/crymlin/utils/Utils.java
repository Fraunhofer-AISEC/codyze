package de.fraunhofer.aisec.crymlin.utils;

import de.fraunhofer.aisec.cpg.graph.MethodDeclaration;
import de.fraunhofer.aisec.cpg.graph.RecordDeclaration;

public class Utils {

  /**
   * Returns the fully qualified signature of a method within a record declaration (e.g., a Java class).
   * 
   * @param r
   * @param m
   * @return
   */
  public static String toFullyQualifiedSignature(RecordDeclaration r, MethodDeclaration m) {
	  return r.getName() + "." + m.getSignature();
  }
}
