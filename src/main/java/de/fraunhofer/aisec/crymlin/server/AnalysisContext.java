package de.fraunhofer.aisec.crymlin.server;

import de.fraunhofer.aisec.crymlin.structures.Method;
import java.util.HashMap;
import java.util.Map;

public class AnalysisContext {

  /** Map of method signatures to {@code Method}s. */
  public final Map<String, Method> methods = new HashMap<>();
}
